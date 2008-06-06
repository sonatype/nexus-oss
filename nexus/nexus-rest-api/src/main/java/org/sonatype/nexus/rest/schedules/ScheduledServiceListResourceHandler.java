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
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskState;

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
        Map<String, List<ScheduledTask<?>>> tasksMap = getNexus().getActiveTasks();

        ScheduledServiceListResourceResponse response = new ScheduledServiceListResourceResponse();

        for ( String key : tasksMap.keySet() )
        {
            List<ScheduledTask<?>> tasks = tasksMap.get( key );

            for ( ScheduledTask<?> task : tasks )
            {
                ScheduledServiceListResource item = new ScheduledServiceListResource();
                item.setResourceURI( calculateSubReference( task.getId() ).toString() );
                item.setCreated( task.getScheduledAt() );
                item.setLastRunResult( TaskState.BROKEN.equals( task.getTaskState() ) ? "Error" : "Ok" );
                item.setName( task.getId() );
                item.setServiceStatus( StringUtils.capitalise( task.getTaskState().toString() ) );
                item.setServiceTypeId( task.getType() );
                item.setServiceTypeName( getServiceTypeName( task.getType() ) );
                if ( task.getLastRun() != null)
                {
                    item.setLastRunTime( task.getLastRun().toString() );
                }
                else
                {
                    item.setLastRunTime( "n/a" );
                }
                if ( task.getNextRun() != null)
                {
                    item.setNextRunTime( task.getNextRun().toString() );
                }
                else
                {
                    item.setNextRunTime( "n/a" );
                }
                
                item.setServiceSchedule( getScheduleShortName( task.getSchedule() ) );
                
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
        ScheduledServiceResourceResponse response = (ScheduledServiceResourceResponse) deserialize( new ScheduledServiceResourceResponse() );
        
        if ( response == null )
        {
            return;
        }
        else
        {
            ScheduledServiceBaseResource resource = response.getData();
            
            getNexus().schedule( getModelName( resource ), getModelNexusTask( resource ), getModelSchedule( resource ) );
            
            getResponse().setEntity( serialize( entity, response ) );
        }
    }

}
