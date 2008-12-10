/**
 * Sonatype NexusTM [Open Source Version].
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
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
    implements NexusScheduler, Contextualizable
{
    /**
     * The scheduler.
     */
    @Requirement
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

    public <T> ScheduledTask<T> submit( String name, NexusTask<T> nexusTask )
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

    public <T> ScheduledTask<T> schedule( String name, NexusTask<T> nexusTask, Schedule schedule )
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

    public void startService()
        throws Exception
    {
        getLogger().info( "Starting Scheduler" );

        scheduler.startService();
    }

    public void stopService()
        throws Exception
    {
        getLogger().info( "Stopping Scheduler" );

        scheduler.stopService();
    }

    public NexusTask<?> createTaskInstance( String taskType )
        throws IllegalArgumentException
    {
        return (NexusTask<?>) scheduler.createTaskInstance( taskType );
    }

    public NexusTask<?> createTaskInstance( Class<?> taskType )
        throws IllegalArgumentException
    {
        return (NexusTask<?>) scheduler.createTaskInstance( taskType );
    }

}
