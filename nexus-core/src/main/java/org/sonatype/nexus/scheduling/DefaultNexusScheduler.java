/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.scheduling;

import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.Scheduler;
import org.sonatype.scheduling.schedules.Schedule;

/**
 * The Nexus scheduler.
 * 
 * @author cstamas
 */
@Component( role = NexusScheduler.class )
public class DefaultNexusScheduler
    extends AbstractLoggingComponent
    implements NexusScheduler
{
    @Requirement
    private Scheduler scheduler;

    @Requirement
    private PlexusContainer plexusContainer;

    protected PlexusContainer getPlexusContainer()
    {
        return plexusContainer;
    }

    @Override
    public void initializeTasks()
    {
        scheduler.initializeTasks();
    }

    @Override
    public void shutdown()
    {
        scheduler.shutdown();
    }
    
    @Override
    public <T> ScheduledTask<T> submit( String name, NexusTask<T> nexusTask )
        throws RejectedExecutionException, NullPointerException
    {
        if ( nexusTask.allowConcurrentSubmission( scheduler.getActiveTasks() ) )
        {
            return scheduler.submit( name, nexusTask );
        }
        else
        {
            throw new RejectedExecutionException( "Task of this type is already submitted!" );
        }
    }

    @Override
    public <T> ScheduledTask<T> schedule( String name, NexusTask<T> nexusTask, Schedule schedule )
        throws RejectedExecutionException, NullPointerException
    {
        if ( nexusTask.allowConcurrentSubmission( scheduler.getActiveTasks() ) )
        {
            return scheduler.schedule( name, nexusTask, schedule );
        }
        else
        {
            throw new RejectedExecutionException( "Task of this type is already scheduled!" );
        }
    }

    @Override
    public <T> ScheduledTask<T> updateSchedule( ScheduledTask<T> task )
        throws RejectedExecutionException, NullPointerException
    {
        if ( task != null )
        {
            scheduler.updateSchedule( task );
        }

        return task;
    }

    @Override
    public Map<String, List<ScheduledTask<?>>> getAllTasks()
    {
        return scheduler.getAllTasks();
    }

    @Override
    public Map<String, List<ScheduledTask<?>>> getActiveTasks()
    {
        return scheduler.getActiveTasks();
    }

    @Override
    public ScheduledTask<?> getTaskById( String id )
        throws NoSuchTaskException
    {
        return scheduler.getTaskById( id );
    }

    @Override
    @Deprecated
    @SuppressWarnings( "unchecked" )
    public NexusTask<?> createTaskInstance( String taskType )
        throws IllegalArgumentException
    {
        return (NexusTask) scheduler.createTaskInstance( taskType );
    }

    @Override
    public <T> T createTaskInstance( Class<T> taskType )
        throws IllegalArgumentException
    {
        return scheduler.createTaskInstance( taskType );
    }

}
