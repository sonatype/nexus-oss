package org.sonatype.nexus.plugins.tasks.api;

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskState;
import org.sonatype.scheduling.schedules.RunNowSchedule;

@Component( role = PlexusResource.class, hint = "TaskHelperResource" )
public class TaskHelperPlexusResource
    extends AbstractPlexusResource
{
    @Requirement
    private NexusScheduler nexusScheduler;

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/taskhelper";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();
        String name = form.getFirstValue( "name" );
        String taskType = form.getFirstValue( "taskType" );

        response.setStatus( Status.SUCCESS_NO_CONTENT );

        ScheduledTask<?> namedTask = null;

        for ( int i = 0; i < 100; i++ )
        {
            try
            {
                Thread.sleep( 500 );
            }
            catch ( InterruptedException e )
            {
            }

            Map<String, List<ScheduledTask<?>>> taskMap = nexusScheduler.getAllTasks();

            boolean running = false;

            if ( namedTask != null )
            {
                running = isTaskRunning( namedTask, taskType, name );
            }
            else
            {
                for ( List<ScheduledTask<?>> taskList : taskMap.values() )
                {
                    for ( ScheduledTask<?> task : taskList )
                    {
                        if ( isTaskRunning( task, taskType, name ) )
                        {
                            running = true;

                            if ( name != null )
                            {
                                namedTask = task;
                            }

                            break;
                        }
                    }
                }
            }

            if ( !running )
            {
                response.setStatus( Status.SUCCESS_OK );
                break;
            }
        }

        return null;
    }

    private boolean isTaskRunning( ScheduledTask<?> task, String taskType, String name )
    {
        return ( ( TaskState.RUNNING.equals( task.getTaskState() ) || TaskState.SLEEPING.equals( task.getTaskState() ) || ( RunNowSchedule.class.isAssignableFrom( task.getClass() ) && !TaskState.BROKEN.equals( task.getTaskState() ) ) )
            && ( taskType == null || taskType.equals( task.getType() ) ) && ( name == null || name.equals( task.getName() ) ) );
    }
}
