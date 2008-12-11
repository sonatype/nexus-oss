/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.scheduling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.feeds.SystemProcess;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskExecutionException;
import org.sonatype.scheduling.TaskState;

public abstract class AbstractNexusTask<T>
    extends AbstractLogEnabled
    implements NexusTask<T>, Contextualizable
{
    public static final long A_DAY = 24L * 60L * 60L * 1000L;

    private PlexusContainer plexusContainer;

    private Map<String, String> parameters;

    private SystemProcess prc;

    private Nexus nexus = null;

    // override if you have a task that needs to hide itself
    public boolean isExposed()
    {
        return true;
    }

    // TODO: finish this thread!
    public RepositoryTaskActivityDescriptor getTaskActivityDescriptor()
    {
        return null;
    }

    public void contextualize( Context ctx )
        throws ContextException
    {
        this.plexusContainer = (PlexusContainer) ctx.get( PlexusConstants.PLEXUS_KEY );
    }

    protected Nexus getNexus()
    {
        if ( nexus == null )
        {
            try
            {
                nexus = (Nexus) plexusContainer.lookup( Nexus.class );
            }
            catch ( ComponentLookupException e )
            {
                throw new IllegalStateException( "Cannot fetch Nexus from container!", e );
            }
        }

        return nexus;
    }

    public void addParameter( String key, String value )
    {
        getParameters().put( key, value );
    }

    public String getParameter( String key )
    {
        return getParameters().get( key );
    }

    public Map<String, String> getParameters()
    {
        if ( parameters == null )
        {
            parameters = new HashMap<String, String>();
        }

        return parameters;
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

        beforeRun();

        T result = null;

        try
        {
            result = doRun();

            getNexus().systemProcessFinished( prc );

            afterRun();

            return result;
        }
        catch ( Throwable e )
        {
            getNexus().systemProcessBroken( prc, e );

            if ( Exception.class.isAssignableFrom( e.getClass() ) )
            {
                // this is an exception, pass it further
                throw (Exception) e;
            }
            else
            {
                // this is a Throwable or Error instance, pack it into an exception and rethrow
                throw new TaskExecutionException( e );
            }
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
