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
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.ScheduledServiceTypePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceTypeResource;
import org.sonatype.nexus.rest.model.ScheduledServiceTypeResourceResponse;

public class ScheduledServiceTypeResourceHandler 
    extends AbstractScheduledServiceResourceHandler
{
    /**
     * The default constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public ScheduledServiceTypeResourceHandler( Context context, Request request, Response response )
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
        ScheduledServiceTypeResourceResponse response = new ScheduledServiceTypeResourceResponse();
        
        ScheduledServiceTypeResource type = new ScheduledServiceTypeResource();
        type.setId( "1" );
        type.setName( "Purge Snapshots" );
        ScheduledServiceTypePropertyResource property = new ScheduledServiceTypePropertyResource();
        property.setName( "Some Config Value" );
        property.setType( "string" );
        property.setHelpText( "Some Help Text" );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setName( "Other Config Value" );
        property.setType( "string" );
        property.setHelpText( "Other Help Text" );
        type.addProperty( property );        
        response.addData( type );
        
        type = new ScheduledServiceTypeResource();
        type.setId( "2" );
        type.setName( "Synchronize Repositories" );
        property = new ScheduledServiceTypePropertyResource();
        property.setName( "Some Config Value" );
        property.setType( "string" );
        property.setHelpText( "Some Help Text" );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setName( "Other Config Value" );
        property.setType( "string" );
        property.setHelpText( "Other Help Text" );
        type.addProperty( property );        
        response.addData( type );

        return serialize( variant, response );
    }
}
