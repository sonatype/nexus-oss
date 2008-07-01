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

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatus;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatusResponse;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskState;

public class ScheduledServiceRunResourceHandler extends AbstractScheduledServiceResourceHandler
{
    public static final String SCHEDULED_SERVICE_ID_KEY = "scheduledServiceId";
    private String scheduledServiceId;
    
    public ScheduledServiceRunResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
        this.scheduledServiceId = getRequest().getAttributes().get( SCHEDULED_SERVICE_ID_KEY ).toString();
    }
    
    protected String getScheduledServiceId()
    {
        return this.scheduledServiceId;
    }
    
    public boolean allowGet()
    {
        return true;
    }

    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        try
        {
            ScheduledTask<?> task = getNexus().getTaskById( getScheduledServiceId() );
            
            task.runNow();

            ScheduledServiceBaseResource resource = getServiceRestModel( task );
            
            
            if ( resource != null )
            {
                ScheduledServiceResourceStatus resourceStatus = new ScheduledServiceResourceStatus();
                resourceStatus.setResource( resource );
                resourceStatus.setResourceURI( calculateSubReference( task.getId() ).toString() );
                resourceStatus.setStatus( task.getTaskState().toString() );
                resourceStatus.setCreated( task.getScheduledAt() == null ? "n/a" : task.getScheduledAt().toString() );
                resourceStatus.setLastRunResult( TaskState.BROKEN.equals( task.getTaskState() ) ? "Error" : "Ok" );
                resourceStatus.setLastRunTime( task.getLastRun() == null ? "n/a" : task.getLastRun().toString() );
                resourceStatus.setNextRunTime( task.getNextRun() == null ? "n/a" : task.getNextRun().toString() );

                ScheduledServiceResourceStatusResponse response = new ScheduledServiceResourceStatusResponse();
                response.setData( resourceStatus );

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
}
