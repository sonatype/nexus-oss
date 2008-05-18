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
import java.util.Date;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServiceListResourceResponse;
import org.sonatype.nexus.rest.model.ScheduledServiceResourceResponse;

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
        ScheduledServiceListResourceResponse response = new ScheduledServiceListResourceResponse();
        
        ScheduledServiceListResource item = new ScheduledServiceListResource();
        item.setCreated( new Date() );
        item.setLastRunResult( "last run result" );
        item.setLastRunTime( "last run time" );
        item.setName( "name0" );
        item.setNextRunTime( "next run time" );
        item.setResourceURI( calculateSubReference( "0" ).toString() );
        item.setServiceSchedule( "none" );
        item.setServiceStatus( "status" );
        item.setServiceTypeName( "Purge Snapshots" );
        item.setServiceTypeId( "1" );
        response.addData( item );
        
        item = new ScheduledServiceListResource();
        item.setCreated( new Date() );
        item.setLastRunResult( "last run result" );
        item.setLastRunTime( "last run time" );
        item.setName( "name1" );
        item.setNextRunTime( "next run time" );
        item.setResourceURI( calculateSubReference( "1" ).toString() );
        item.setServiceSchedule( "once" );
        item.setServiceStatus( "status" );
        item.setServiceTypeName( "Purge Snapshots" );
        item.setServiceTypeId( "1" );        
        response.addData( item );
        
        item = new ScheduledServiceListResource();
        item.setCreated( new Date() );
        item.setLastRunResult( "last run result" );
        item.setLastRunTime( "last run time" );
        item.setName( "name2" );
        item.setNextRunTime( "next run time" );
        item.setResourceURI( calculateSubReference( "2" ).toString() );
        item.setServiceSchedule( "daily" );
        item.setServiceStatus( "status" );
        item.setServiceTypeName( "Purge Snapshots" );
        item.setServiceTypeId( "1" );
        response.addData( item );
        
        item = new ScheduledServiceListResource();
        item.setCreated( new Date() );
        item.setLastRunResult( "last run result" );
        item.setLastRunTime( "last run time" );
        item.setName( "name3" );
        item.setNextRunTime( "next run time" );
        item.setResourceURI( calculateSubReference( "3" ).toString() );
        item.setServiceSchedule( "weekly" );
        item.setServiceStatus( "status" );
        item.setServiceTypeName( "Purge Snapshots" );
        item.setServiceTypeId( "1" );
        response.addData( item );
        
        item = new ScheduledServiceListResource();
        item.setCreated( new Date() );
        item.setLastRunResult( "last run result" );
        item.setLastRunTime( "last run time" );
        item.setName( "name4" );
        item.setNextRunTime( "next run time" );
        item.setResourceURI( calculateSubReference( "4" ).toString() );
        item.setServiceSchedule( "monthly" );
        item.setServiceStatus( "status" );
        item.setServiceTypeName( "Purge Snapshots" );
        item.setServiceTypeId( "1" );
        response.addData( item );
        
        item = new ScheduledServiceListResource();
        item.setCreated( new Date() );
        item.setLastRunResult( "last run result" );
        item.setLastRunTime( "last run time" );
        item.setName( "name5" );
        item.setNextRunTime( "next run time" );
        item.setResourceURI( calculateSubReference( "5" ).toString() );
        item.setServiceSchedule( "advanced" );
        item.setServiceStatus( "status" );
        item.setServiceTypeName( "Purge Snapshots" );
        item.setServiceTypeId( "1" );        
        response.addData( item );

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

        if ( response != null )
        {
            ScheduledServiceBaseResource resource = response.getData();

            resource.setId( Long.toHexString( System.currentTimeMillis() ) );

            getResponse().setEntity( serialize( entity, response ) );
        }
    }

}
