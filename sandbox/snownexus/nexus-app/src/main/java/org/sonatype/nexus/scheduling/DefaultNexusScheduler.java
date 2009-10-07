/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.scheduling;

import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
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
    extends AbstractLogEnabled
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

    public void initializeTasks()
    {
        scheduler.initializeTasks();
    }

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

    public <T> ScheduledTask<T> updateSchedule( ScheduledTask<T> task )
        throws RejectedExecutionException, NullPointerException
    {
        if ( task != null )
        {
            scheduler.updateSchedule( task );
        }

        return task;
    }

    public Map<String, List<ScheduledTask<?>>> getAllTasks()
    {
        return scheduler.getAllTasks();
    }

    public Map<String, List<ScheduledTask<?>>> getActiveTasks()
    {
        return scheduler.getActiveTasks();
    }

    public ScheduledTask<?> getTaskById( String id )
        throws NoSuchTaskException
    {
        return scheduler.getTaskById( id );
    }

    @SuppressWarnings( "unchecked" )
    public NexusTask<?> createTaskInstance( String taskType )
        throws IllegalArgumentException
    {
        return (NexusTask) scheduler.createTaskInstance( taskType );
    }

    public <T> T createTaskInstance( Class<T> taskType )
        throws IllegalArgumentException
    {
        return scheduler.createTaskInstance( taskType );
    }

}
