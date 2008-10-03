package org.sonatype.nexus.rest.schedules;

import java.text.ParseException;
import java.util.Iterator;
import java.util.concurrent.RejectedExecutionException;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatus;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatusResponse;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskState;

/**
 * @author tstevens
 * @plexus.component role-hint="ScheduledServicePlexusResource"
 */
public class ScheduledServicePlexusResource
    extends AbstractScheduledServicePlexusResource
{

    public static final String SCHEDULED_SERVICE_ID_KEY = "scheduledServiceId";

    public ScheduledServicePlexusResource()
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
        return "/schedules/{" + SCHEDULED_SERVICE_ID_KEY + "}";
    }

    protected String getScheduledServiceId( Request request )
    {
        return request.getAttributes().get( SCHEDULED_SERVICE_ID_KEY ).toString();
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        ScheduledServiceResourceResponse result = new ScheduledServiceResourceResponse();
        try
        {
            ScheduledTask<?> task = getNexusInstance( request ).getTaskById( getScheduledServiceId( request ) );

            ScheduledServiceBaseResource resource = getServiceRestModel( task );

            if ( resource != null )
            {
                result.setData( resource );
            }
            else
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Invalid schedule id ("
                    + getScheduledServiceId( request ) + "), can't load task." );
            }
        }
        catch ( NoSuchTaskException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "There is no task with ID="
                + getScheduledServiceId( request ) );
        }

        return result;
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        ScheduledServiceResourceResponse serviceRequest = (ScheduledServiceResourceResponse) payload;
        ScheduledServiceResourceStatusResponse result = null;

        if ( serviceRequest != null )
        {
            ScheduledServiceBaseResource resource = serviceRequest.getData();

            try
            {
                // currently we allow editing of:
                // task name
                // task schedule (even to another type)
                // task params
                ScheduledTask<?> task = getNexusInstance( request ).getTaskById( getScheduledServiceId( request ) );

                task.setEnabled( resource.isEnabled() );

                task.setName( getModelName( resource ) );

                task.setSchedule( getModelSchedule( resource ) );

                for ( Iterator iter = resource.getProperties().iterator(); iter.hasNext(); )
                {
                    ScheduledServicePropertyResource prop = (ScheduledServicePropertyResource) iter.next();

                    task.getTaskParams().put( prop.getId(), prop.getValue() );
                }

                task.reset();

                // Store the changes
                getNexusInstance( request ).updateSchedule( task );

                ScheduledServiceResourceStatus resourceStatus = new ScheduledServiceResourceStatus();
                resourceStatus.setResource( resource );
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
            catch ( NoSuchTaskException e )
            {
                getLogger().warn( "Unable to locate task id:" + resource.getId(), e );

                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Scheduled service not found!" );
            }
            catch ( RejectedExecutionException e )
            {
                throw new ResourceException( Status.CLIENT_ERROR_CONFLICT, e.getMessage() );
            }
            catch ( ParseException e )
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage() );
            }
        }
        return result;
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        try
        {
            getNexusInstance( request ).getTaskById( getScheduledServiceId( request ) ).cancel();

            response.setStatus( Status.SUCCESS_NO_CONTENT );
        }
        catch ( NoSuchTaskException e )
        {
            response.setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Scheduled service not found!" );
        }
    }

}
