package org.sonatype.nexus.plugins.tasks.api;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        return new PathProtectionDescriptor( getResourceUri(), "anon" );
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
        String attemptsParam = form.getFirstValue( "attempts" );
        int attempts = 300;

        if ( attemptsParam != null )
        {
            try
            {
                attempts = Integer.parseInt( attemptsParam );
            }
            catch ( NumberFormatException e )
            {
                // ignore, will use default of 300
            }
        }

        ScheduledTask<?> namedTask = null;

        if ( name != null )
        {
            namedTask = getTaskByName( name );

            if ( namedTask == null )
            {
                // task wasn't found, so bounce on outta here
                response.setStatus( Status.SUCCESS_OK );
                return "OK";
            }
        }

        for ( int i = 0; i < attempts; i++ )
        {
            try
            {
                Thread.sleep( 500 );
            }
            catch ( InterruptedException e )
            {
            }

            if ( namedTask != null )
            {
                if ( isTaskCompleted( namedTask ) )
                {
                    response.setStatus( Status.SUCCESS_OK );
                    return "OK";
                }
            }
            else
            {
                Set<ScheduledTask<?>> tasks = getTasks( taskType );

                boolean running = false;

                for ( ScheduledTask<?> task : tasks )
                {
                    if ( !isTaskCompleted( task ) )
                    {
                        running = true;
                        break;
                    }
                }

                if ( !running )
                {
                    response.setStatus( Status.SUCCESS_OK );
                    return "OK";
                }
            }
        }

        response.setStatus( Status.SUCCESS_NO_CONTENT );
        return "Tasks Not Finished";
    }

    private boolean isTaskCompleted( ScheduledTask<?> task )
    {
        // always have to wait for internal tasks
        if ( RunNowSchedule.class.isAssignableFrom( task.getClass() ) )
        {
            return false;
        }

        // otherwise, just check the status
        if ( TaskState.RUNNING.equals( task.getTaskState() ) || TaskState.SLEEPING.equals( task.getTaskState() ) )
        {
            return false;
        }

        return true;
    }

    private ScheduledTask<?> getTaskByName( String name )
    {
        Map<String, List<ScheduledTask<?>>> taskMap = nexusScheduler.getAllTasks();

        for ( List<ScheduledTask<?>> taskList : taskMap.values() )
        {
            for ( ScheduledTask<?> task : taskList )
            {
                if ( task.getName().equals( name ) )
                {
                    return task;
                }
            }
        }

        return null;
    }

    private Set<ScheduledTask<?>> getTasks( String taskType )
    {
        Set<ScheduledTask<?>> tasks = new HashSet<ScheduledTask<?>>();

        Map<String, List<ScheduledTask<?>>> taskMap = nexusScheduler.getAllTasks();

        for ( List<ScheduledTask<?>> taskList : taskMap.values() )
        {
            for ( ScheduledTask<?> task : taskList )
            {
                if ( taskType == null || task.getType().equals( taskType ) )
                {
                    tasks.add( task );
                }
            }
        }

        return tasks;
    }
}
