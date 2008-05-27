/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.scheduling;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.sonatype.nexus.scheduling.util.DefaultCallableTask;
import org.sonatype.nexus.scheduling.util.DefaultRunnableTask;

/**
 * A simple facade to ScheduledThreadPoolExecutor as Plexus component.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultScheduler
    extends AbstractLogEnabled
    implements Scheduler, Contextualizable, Startable
{
    private PlexusContainer plexusContainer;

    private PlexusThreadFactory plexusThreadFactory;

    private ScheduledThreadPoolExecutor scheduledExecutorService;

    public void contextualize( Context context )
        throws ContextException
    {
        plexusContainer = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    public void start()
        throws StartingException
    {
        plexusThreadFactory = new PlexusThreadFactory( plexusContainer );

        scheduledExecutorService = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(
            10,
            plexusThreadFactory );
    }

    public void stop()
        throws StoppingException
    {
        getScheduledExecutorService().shutdown();

        try
        {
            boolean stopped = getScheduledExecutorService().awaitTermination( 15, TimeUnit.SECONDS );

            if ( !stopped )
            {
                List<Runnable> queueds = getScheduledExecutorService().shutdownNow();

                getLogger().warn( "Scheduler shut down with " + queueds.size() + " tasks queued." );
            }
        }
        catch ( InterruptedException e )
        {
            getLogger().info( "Termination interrupted", e );
        }
    }

    public PlexusThreadFactory getPlexusThreadFactory()
    {
        return plexusThreadFactory;
    }

    public ScheduledThreadPoolExecutor getScheduledExecutorService()
    {
        return scheduledExecutorService;
    }

    public SubmittedTask submit( Runnable runnable )
    {
        DefaultRunnableTask drt = new DefaultRunnableTask( runnable, null, getScheduledExecutorService() );

        drt.start();

        return drt;
    }

    public ScheduledTask schedule( Runnable runnable, ScheduleIterator iterator )
    {
        DefaultRunnableTask drt = new DefaultRunnableTask( runnable, iterator, getScheduledExecutorService() );

        drt.start();

        return drt;
    }

    public <T> SubmittedCallableTask<T> submit( Callable<T> callable )
    {
        DefaultCallableTask<T> dct = new DefaultCallableTask<T>( callable, null, getScheduledExecutorService() );

        dct.start();

        return dct;
    }

    public <T> ScheduledCallableTask<T> schedule( Callable<T> callable, ScheduleIterator iterator )
    {
        DefaultCallableTask<T> dct = new DefaultCallableTask<T>( callable, iterator, getScheduledExecutorService() );

        dct.start();

        return dct;
    }

}
