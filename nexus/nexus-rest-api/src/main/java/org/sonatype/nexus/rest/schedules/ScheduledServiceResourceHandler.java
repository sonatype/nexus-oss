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
package org.sonatype.nexus.rest.schedules;

import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatus;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatusResponse;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskState;

public class ScheduledServiceResourceHandler
    extends AbstractScheduledServiceResourceHandler
{
    public static final String SCHEDULED_SERVICE_ID_KEY = "scheduledServiceId";

    /** The scheduledService ID */
    private String scheduledServiceId;

    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public ScheduledServiceResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

        this.scheduledServiceId = getRequest().getAttributes().get( SCHEDULED_SERVICE_ID_KEY ).toString();
    }

    protected String getScheduledServiceId()
    {
        return this.scheduledServiceId;
    }

    /**
     * We are handling HTTP GET's.
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * Method constructing and returning the Repository route representation.
     */
    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        try
        {
            ScheduledServiceResourceResponse response = new ScheduledServiceResourceResponse();

            ScheduledTask<?> task = getNexus().getTaskById( getScheduledServiceId() );

            ScheduledServiceBaseResource resource = getServiceRestModel( task );            
            
            if ( resource != null )
            {
                response.setData( resource );

                return serialize( variant, response );
            }
            
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Invalid schedule id (" + getScheduledServiceId() + "), can't load task." );

            return null;
        }
        catch ( NoSuchTaskException e )
        {
            getResponse().setStatus(
                Status.CLIENT_ERROR_NOT_FOUND,
                "There is no task with ID=" + getScheduledServiceId() );

            return null;
        }
    }

    /**
     * This resource allows PUT.
     */
    public boolean allowPut()
    {
        return true;
    }

    /**
     * Update a repository route.
     */
    public void put( Representation representation )
    {
        ScheduledServiceResourceResponse request = (ScheduledServiceResourceResponse) deserialize( new ScheduledServiceResourceResponse() );

        if ( request == null )
        {
            return;
        }
        else
        {
            ScheduledServiceBaseResource resource = request.getData();

            try
            {
                // currently we allow editing of:
                // task name
                // task schedule (even to another type)
                // task params
                ScheduledTask<?> task = getNexus().getTaskById( getScheduledServiceId() );

                task.setEnabled( resource.isEnabled() );

                task.setName( getModelName( resource ) );

                task.setSchedule( getModelSchedule( resource ) );

                for ( Iterator iter = resource.getProperties().iterator(); iter.hasNext(); )
                {
                    ScheduledServicePropertyResource prop = (ScheduledServicePropertyResource) iter.next();

                    task.getTaskParams().put( prop.getId(), prop.getValue() );
                }

                task.reset();
                
                //Store the changes
                getNexus().updateSchedule( task );
                
                ScheduledServiceResourceStatus resourceStatus = new ScheduledServiceResourceStatus();
                resourceStatus.setResource( resource );
                // Just need to update the id, as the incoming data is a POST w/ no id
                resourceStatus.getResource().setId( task.getId() );
                resourceStatus.setResourceURI( calculateSubReference( task.getId() ).toString() );
                resourceStatus.setStatus( task.getTaskState().toString() );
                resourceStatus.setCreated( task.getScheduledAt() == null ? "n/a" : task.getScheduledAt().toString() );
                resourceStatus.setLastRunResult( TaskState.BROKEN.equals( task.getTaskState() ) ? "Error" : "Ok" );
                resourceStatus.setLastRunTime( task.getLastRun() == null ? "n/a" : task.getLastRun().toString() );
                resourceStatus.setNextRunTime( task.getNextRun() == null ? "n/a" : task.getNextRun().toString() );

                ScheduledServiceResourceStatusResponse response = new ScheduledServiceResourceStatusResponse();
                response.setData( resourceStatus );

                getResponse().setEntity( serialize( representation, response ) );
            }
            catch ( NoSuchTaskException e )
            {
                getLogger().log( Level.SEVERE, "Unable to locate task id:" + resource.getId(), e );
                
                getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Scheduled service not found!" );
            }
            catch ( RejectedExecutionException e )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_CONFLICT, e.getMessage() );
                
                return;
            }
            catch ( ParseException e )
            {
                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage() );
            }
        }
    }

    /**
     * This resource allows DELETE.
     */
    public boolean allowDelete()
    {
        return true;
    }

    /**
     * Delete a task.
     */
    public void delete()
    {
        try
        {
            getNexus().getTaskById( getScheduledServiceId() ).cancel();
        }
        catch ( NoSuchTaskException e )
        {
            getResponse().setStatus( Status.CLIENT_ERROR_NOT_FOUND, "Scheduled service not found!" );
        }
    }
}
