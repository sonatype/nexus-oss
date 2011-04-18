/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.scheduling;

import java.util.List;
import java.util.Map;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.error.reporting.ErrorReportRequest;
import org.sonatype.nexus.error.reporting.ErrorReportingManager;
import org.sonatype.nexus.feeds.SystemProcess;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.scheduling.AbstractSchedulerTask;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskState;

public abstract class AbstractNexusTask<T>
    extends AbstractSchedulerTask<T>
    implements NexusTask<T>
{
    public static final long A_DAY = 24L * 60L * 60L * 1000L;

    @Requirement
    private PlexusContainer plexusContainer;

    @Requirement
    private ErrorReportingManager errorManager;

    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    // DO NOT ADD @REQ here, since you will introduce a cycle
    // Look below, nexus is looked up "lazily"
    private Nexus nexus = null;

    private SystemProcess prc;

    @Deprecated
    protected AbstractNexusTask()
    {
        this( null );
    }

    protected AbstractNexusTask( final String name )
    {
        if ( name == null || name.trim().length() == 0 )
        {
            TaskUtils.setName( this, getClass().getSimpleName() );
        }
        else
        {
            TaskUtils.setName( this, name );
        }
    }

    // TODO: finish this thread!
    public RepositoryTaskActivityDescriptor getTaskActivityDescriptor()
    {
        return null;
    }

    public boolean isExposed()
    {
        // override to hide it
        return true;
    }

    protected PlexusContainer getPlexusContainer()
    {
        return plexusContainer;
    }

    protected Nexus getNexus()
    {
        if ( nexus == null )
        {
            try
            {
                nexus = getPlexusContainer().lookup( Nexus.class );
            }
            catch ( ComponentLookupException e )
            {
                throw new IllegalStateException( "Cannot fetch Nexus from container!", e );
            }
        }

        return nexus;
    }

    /**
     * {@inheritDoc}
     */
    public String getId()
    {
        return getParameter( ID_KEY );
    }

    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return getParameter( NAME_KEY );
    }

    /**
     * {@inheritDoc}
     */
    public boolean shouldSendAlertEmail()
    {
        final String alertEmail = getAlertEmail();
        return alertEmail != null && alertEmail.trim().length() > 0;
    }

    /**
     * {@inheritDoc}
     */
    public String getAlertEmail()
    {
        return getParameter( ALERT_EMAIL_KEY );
    }

    public boolean allowConcurrentSubmission( Map<String, List<ScheduledTask<?>>> activeTasks )
    {
        // concurrent execution will stop us if needed, but user may freely submit
        return true;
    }

    public boolean allowConcurrentExecution( Map<String, List<ScheduledTask<?>>> activeTasks )
    {
        // most basic check: simply not allowing multiple execution of instances of this class
        // override if needed
        if ( activeTasks.containsKey( this.getClass().getSimpleName() ) )
        {
            for ( ScheduledTask<?> task : activeTasks.get( this.getClass().getSimpleName() ) )
            {
                if ( TaskState.RUNNING.equals( task.getTaskState() ) )
                {
                    return false;
                }
            }
            return true;
        }
        else
        {
            return true;
        }
    }

    public final T call()
        throws Exception
    {
        prc = getNexus().systemProcessStarted( getAction(), getMessage() );

        T result = null;

        // Subject subject = this.securitySystem.runAs( new SimplePrincipalCollection("admin", "") );
        // TODO: do the above instead
        Subject subject = new TaskSecuritySubject();
        ThreadContext.bind( subject );
        try
        {
            beforeRun();

            result = doRun();

            getNexus().systemProcessFinished( prc, getMessage() );

            afterRun();

            return result;
        }
        catch ( Exception e )
        {
            // TODO: make feed recorder make less noise on TaskInterruptedException
            getNexus().systemProcessBroken( prc, e );

            // notify that there was a failure
            applicationEventMulticaster.notifyEventListeners( new NexusTaskFailureEvent<T>( this, e ) );

            if ( errorManager.isEnabled() )
            {
                ErrorReportRequest request = new ErrorReportRequest();
                request.setThrowable( e );
                request.getContext().put( "taskClass", getClass().getName() );
                request.getContext().putAll( getParameters() );

                errorManager.handleError( request );
            }

            throw e;
        }
        finally
        {
            subject.logout();
        }
    }

    protected void beforeRun()
    {
        // override if needed
    }

    protected abstract T doRun()
        throws Exception;

    protected void afterRun()
    {
        // override if needed
    }

    protected abstract String getAction();

    protected abstract String getMessage();

}
