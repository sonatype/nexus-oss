package org.sonatype.nexus.rest.schedules;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatus;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatusResponse;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskState;
import org.sonatype.scheduling.schedules.ManualRunSchedule;
import org.sonatype.scheduling.schedules.Schedule;

/**
 * @author tstevens
 * @plexus.component role-hint="ScheduledServiceListPlexusResource"
 */
public class ScheduledServiceListPlexusResource
    extends AbstractScheduledServicePlexusResource
{

    public ScheduledServiceListPlexusResource()
    {
        this.setModifiable( true );
    }
    
    @Override
    public Object getPayloadInstance()
    {
        return new ScheduledServiceResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return "/schedules";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Map<String, List<ScheduledTask<?>>> tasksMap = getNexusInstance( request ).getAllTasks();

        ScheduledServiceListResourceResponse result = new ScheduledServiceListResourceResponse();

        for ( String key : tasksMap.keySet() )
        {
            List<ScheduledTask<?>> tasks = tasksMap.get( key );

            for ( ScheduledTask<?> task : tasks )
            {
                ScheduledServiceListResource item = new ScheduledServiceListResource();
                item.setResourceURI( createChildReference( request, task.getId() ).toString() );
                item.setLastRunResult( TaskState.BROKEN.equals( task.getTaskState() ) ? "Error" : "Ok" );
                item.setId( task.getId() );
                item.setName( task.getName() );
                item.setStatus( StringUtils.capitalise( task.getTaskState().toString() ) );
                item.setTypeId( task.getType() );
                item.setTypeName( getNexusInstance( request ).getScheduledTaskDescriptor( task.getType() ).getName() );
                item.setCreated( task.getScheduledAt() == null ? "n/a" : task.getScheduledAt().toString() );
                item.setLastRunTime( task.getLastRun() == null ? "n/a" : task.getLastRun().toString() );
                item.setNextRunTime( task.getNextRun() == null ? "n/a" : task.getNextRun().toString() );
                item.setSchedule( getScheduleShortName( task.getSchedule() ) );
                item.setEnabled( task.isEnabled() );

                result.addData( item );
            }

        }

        return result;
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        ScheduledServiceResourceResponse serviceRequest = (ScheduledServiceResourceResponse) payload;
        ScheduledServiceResourceStatusResponse result = null;

        if ( serviceRequest != null )
        {
            ScheduledServiceBaseResource serviceResource = serviceRequest.getData();
            try
            {
                Schedule schedule = getModelSchedule( serviceRequest.getData() );
                ScheduledTask<?> task = null;

                if ( schedule != null )
                {
                    task = getNexusInstance( request ).schedule(
                        getModelName( serviceResource ),
                        getModelNexusTask( serviceResource, request ),
                        getModelSchedule( serviceResource ) );
                }
                else
                {
                    task = getNexusInstance( request ).schedule(
                        getModelName( serviceResource ),
                        getModelNexusTask( serviceResource, request ),
                        new ManualRunSchedule() );
                }

                task.setEnabled( serviceResource.isEnabled() );

                // Need to store the enabled flag update
                getNexusInstance( request ).updateSchedule( task );

                ScheduledServiceResourceStatus resourceStatus = new ScheduledServiceResourceStatus();
                resourceStatus.setResource( serviceResource );
                // Just need to update the id, as the incoming data is a POST w/ no id
                resourceStatus.getResource().setId( task.getId() );
                resourceStatus.setResourceURI( createChildReference( request, task.getId() ).toString() );
                resourceStatus.setStatus( task.getTaskState().toString() );
                resourceStatus.setCreated( task.getScheduledAt() == null ? "n/a" : task.getScheduledAt().toString() );
                resourceStatus.setLastRunResult( TaskState.BROKEN.equals( task.getTaskState() ) ? "Error" : "Ok" );
                resourceStatus.setLastRunTime( task.getLastRun() == null ? "n/a" : task.getLastRun().toString() );
                resourceStatus.setNextRunTime( task.getNextRun() == null ? "n/a" : task.getNextRun().toString() );

                result = new ScheduledServiceResourceStatusResponse();
                result.setData( resourceStatus );
            }
            catch ( RejectedExecutionException e )
            {
                getLogger().warn( "Execution of task " + getModelName( serviceResource ) + " rejected." );

                throw new ResourceException( Status.CLIENT_ERROR_CONFLICT, e.getMessage() );
            }
            catch ( ParseException e )
            {
                getLogger().warn( "Unable to parse data for task " + getModelName( serviceResource ) );

                throw new PlexusResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    e.getMessage(),
                    getNexusErrorResponse( "cronCommand", e.getMessage() ) );
            }
        }
        return result;
    }

}
