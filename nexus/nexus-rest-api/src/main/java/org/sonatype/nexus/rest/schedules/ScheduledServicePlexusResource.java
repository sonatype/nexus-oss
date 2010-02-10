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
package org.sonatype.nexus.rest.schedules;

import java.text.ParseException;
import java.util.Iterator;
import java.util.concurrent.RejectedExecutionException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatus;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatusResponse;
import org.sonatype.nexus.scheduling.TaskUtils;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskState;

/**
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "ScheduledServicePlexusResource" )
@Path( ScheduledServicePlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class ScheduledServicePlexusResource
    extends AbstractScheduledServicePlexusResource
{
    public static final String SCHEDULED_SERVICE_ID_KEY = "scheduledServiceId";
    
    public static final String RESOURCE_URI = "/schedules/{" + SCHEDULED_SERVICE_ID_KEY + "}"; 

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
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/schedules/*", "authcBasic,perms[nexus:tasks]" );
    }

    protected String getScheduledServiceId( Request request )
    {
        return request.getAttributes().get( SCHEDULED_SERVICE_ID_KEY ).toString();
    }

    /**
     * Get the details of an existing scheduled task.
     * 
     * @param scheduledServiceId The scheduled task to access.
     */
    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( ScheduledServicePlexusResource.SCHEDULED_SERVICE_ID_KEY ) },
                              output = ScheduledServiceResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        ScheduledServiceResourceResponse result = new ScheduledServiceResourceResponse();
        try
        {
            ScheduledTask<?> task = getNexusScheduler().getTaskById( getScheduledServiceId( request ) );

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

    /**
     * Update the configuration of an existing scheduled task.
     * 
     * @param scheduledServiceId The scheduled task to access.
     */
    @Override
    @PUT
    @ResourceMethodSignature( pathParams = { @PathParam( ScheduledServicePlexusResource.SCHEDULED_SERVICE_ID_KEY ) },
                              input = ScheduledServiceResourceResponse.class,
                              output = ScheduledServiceResourceStatusResponse.class )
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
                ScheduledTask<?> task = getNexusScheduler().getTaskById( getScheduledServiceId( request ) );

                task.setEnabled( resource.isEnabled() );

                task.setName( getModelName( resource ) );

                task.setSchedule( getModelSchedule( resource ) );

                for ( Iterator<ScheduledServicePropertyResource> iter = resource.getProperties().iterator(); iter.hasNext(); )
                {
                    ScheduledServicePropertyResource prop = iter.next();

                    task.getTaskParams().put( prop.getId(), prop.getValue() );
                }

                TaskUtils.setAlertEmail( task, resource.getAlertEmail() );
                TaskUtils.setId( task, resource.getId() );
                TaskUtils.setName( task, resource.getName() );

                task.reset();

                // Store the changes
                getNexusScheduler().updateSchedule( task );

                ScheduledServiceResourceStatus resourceStatus = new ScheduledServiceResourceStatus();
                resourceStatus.setResource( resource );
                // Just need to update the id, as the incoming data is a POST w/ no id
                resourceStatus.getResource().setId( task.getId() );
                resourceStatus.setResourceURI( createChildReference( request, this, task.getId() ).toString() );
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
            catch ( InvalidConfigurationException e )
            {
                handleConfigurationException( e );
            }
        }
        return result;
    }

    /**
     * Delete an existing scheduled task.
     * 
     * @param scheduledServiceId The scheduled task to access.
     */
    @Override
    @DELETE
    @ResourceMethodSignature( pathParams = { @PathParam( ScheduledServicePlexusResource.SCHEDULED_SERVICE_ID_KEY ) } )
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        try
        {
            getNexusScheduler().getTaskById( getScheduledServiceId( request ) ).cancel();

            response.setStatus( Status.SUCCESS_NO_CONTENT );
        }
        catch ( NoSuchTaskException e )
        {
            response.setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Scheduled service not found!" );
        }
    }

}
