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
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatus;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceStatusResponse;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskState;
import org.sonatype.scheduling.schedules.Schedule;

public class ScheduledServiceListResourceHandler
    extends AbstractScheduledServiceResourceHandler
{
    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public ScheduledServiceListResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * We are handling HTTP GETs.
     */
    public boolean allowGet()
    {
        return true;
    }

    /**
     * We create the List of Scheduled Services by getting them from Nexus App.
     */
    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        Map<Class<?>, List<ScheduledTask<?>>> tasksMap = getNexus().getAllTasks();

        ScheduledServiceListResourceResponse response = new ScheduledServiceListResourceResponse();

        for ( Class<?> key : tasksMap.keySet() )
        {
            List<ScheduledTask<?>> tasks = tasksMap.get( key );

            for ( ScheduledTask<?> task : tasks )
            {
                ScheduledServiceListResource item = new ScheduledServiceListResource();
                item.setResourceURI( calculateSubReference( task.getId() ).toString() );
                item.setLastRunResult( TaskState.BROKEN.equals( task.getTaskState() ) ? "Error" : "Ok" );
                item.setId( task.getId() );
                item.setName( task.getName() );
                item.setStatus( StringUtils.capitalise( task.getTaskState().toString() ) );
                item.setTypeId( task.getType().getName() );
                item.setTypeName( getServiceTypeName( task.getType() ) );
                item.setCreated( task.getScheduledAt() == null ? "n/a" : task.getScheduledAt().toString() );
                item.setLastRunTime( task.getLastRun() == null ? "n/a" : task.getLastRun().toString() );
                item.setNextRunTime( task.getNextRun() == null ? "n/a" : task.getNextRun().toString() );
                item.setSchedule( getScheduleShortName( task.getSchedule() ) );

                response.addData( item );
            }

        }

        return serialize( variant, response );
    }

    /**
     * This service allows POST.
     */
    public boolean allowPost()
    {
        return true;
    }

    public void post( Representation entity )
    {
        ScheduledServiceResourceResponse request = (ScheduledServiceResourceResponse) deserialize( new ScheduledServiceResourceResponse() );

        if ( request == null )
        {
            return;
        }
        else
        {
            try
            {
                Schedule schedule = getModelSchedule( request.getData() );
                ScheduledTask<?> task = null;
                
                if ( schedule != null )
                {                
                    task = getNexus().schedule(
                        getModelName( request.getData() ),
                        getModelNexusTask( request.getData() ),
                        getModelSchedule( request.getData() ) );
                }
                else
                {
                    task = getNexus().store(
                        getModelName( request.getData() ),
                        getModelNexusTask( request.getData() ) );
                }

                task.setEnabled( request.getData().isEnabled() );
                
                //Need to store the enabled flag update
                getNexus().updateSchedule( task );

                ScheduledServiceResourceStatus resourceStatus = new ScheduledServiceResourceStatus();
                resourceStatus.setResource( request.getData() );
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

                getResponse().setEntity( serialize( entity, response ) );
            }
            catch ( RejectedExecutionException e )
            {
                getLogger().log( Level.SEVERE, "Execution of task " + getModelName( request.getData() ) + " rejected." );
                getResponse().setStatus( Status.CLIENT_ERROR_CONFLICT, e.getMessage() );
                
                return;
            }
            catch ( ParseException e )
            {
                getLogger().log( Level.SEVERE, "Unable to parse data for task " + getModelName( request.getData() ) );
                getResponse().setStatus( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage() );
                
                getResponse().setEntity(
                                        serialize( entity, getNexusErrorResponse(
                                            "cronCommand",
                                            e.getMessage() ) ) );

                return;
            }
        }
    }

}
