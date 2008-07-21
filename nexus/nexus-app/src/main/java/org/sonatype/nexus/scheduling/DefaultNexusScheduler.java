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
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.Scheduler;
import org.sonatype.scheduling.SchedulerTask;
import org.sonatype.scheduling.schedules.Schedule;

/**
 * The Nexus scheduler.
 * 
 * @author cstamas
 * @plexus.component
 */
public class DefaultNexusScheduler
    extends AbstractLogEnabled
    implements NexusScheduler, Contextualizable
{
    /**
     * The scheduler.
     * 
     * @plexus.requirement
     */
    private Scheduler scheduler;

    /** For task lookups */
    private PlexusContainer plexusContainer;

    public void contextualize( Context ctx )
        throws ContextException
    {
        plexusContainer = (PlexusContainer) ctx.get( PlexusConstants.PLEXUS_KEY );
    }

    protected PlexusContainer getPlexusContainer()
    {
        return plexusContainer;
    }

    public <T> ScheduledTask<T> submit( String name, SchedulerTask<T> nexusTask )
        throws RejectedExecutionException,
            NullPointerException
    {
        if ( nexusTask.allowConcurrentSubmission( scheduler.getActiveTasks() ) )
        {
            return scheduler.submit( name, nexusTask, nexusTask.getParameters() );
        }
        else
        {
            throw new RejectedExecutionException( "Task of this type is already submitted!" );
        }
    }

    public <T> ScheduledTask<T> schedule( String name, SchedulerTask<T> nexusTask, Schedule schedule )
        throws RejectedExecutionException,
            NullPointerException
    {
        if ( nexusTask.allowConcurrentSubmission( scheduler.getActiveTasks() ) )
        {
            return scheduler.schedule( name, nexusTask, schedule, nexusTask.getParameters() );
        }
        else
        {
            throw new RejectedExecutionException( "Task of this type is already scheduled!" );
        }
    }

    public <T> ScheduledTask<T> updateSchedule( ScheduledTask<T> task )
        throws RejectedExecutionException,
            NullPointerException
    {
        if ( task != null )
        {
            scheduler.updateSchedule( task );
        }

        return task;
    }

    public Map<Class<?>, List<ScheduledTask<?>>> getAllTasks()
    {
        return scheduler.getAllTasks();
    }

    public Map<Class<?>, List<ScheduledTask<?>>> getActiveTasks()
    {
        return scheduler.getActiveTasks();
    }

    public ScheduledTask<?> getTaskById( String id )
        throws NoSuchTaskException
    {
        return scheduler.getTaskById( id );
    }

    public void startService()
        throws StartingException
    {
        getLogger().info( "Starting Scheduler" );

        scheduler.startService();
    }

    public void stopService()
        throws StoppingException
    {
        getLogger().info( "Stopping Scheduler" );

        scheduler.stopService();
    }

    public SchedulerTask<?> createTaskInstance( String taskType )
        throws IllegalArgumentException
    {
        try
        {
            return (SchedulerTask<?>) getPlexusContainer().lookup( SchedulerTask.ROLE, taskType );
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalArgumentException( "Could not create task of type" + taskType, e );
        }
    }

    public SchedulerTask<?> createTaskInstance( Class<?> taskType )
        throws IllegalArgumentException
    {
        return createTaskInstance( taskType.getName() );
    }

}
