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
import org.sonatype.nexus.index.tasks.PublishIndexesTask;
import org.sonatype.nexus.rest.attributes.RebuildAttributesTask;
import org.sonatype.nexus.rest.cache.ClearCacheTask;
import org.sonatype.nexus.rest.index.ReindexTask;
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
        type.setId( PublishIndexesTask.class.getName() );
        type.setName( "Publish Indexes" );
        ScheduledServiceTypePropertyResource property = new ScheduledServiceTypePropertyResource();
        property.setId( "1" );
        property.setName( "Repository ID" );
        property.setType( "string" );
        property.setHelpText( "Type in the repository ID to publish the indexes." );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "2" );
        property.setName( "Repository Group ID" );
        property.setType( "string" );
        property.setHelpText( "Type in the repository group ID to publish the indexes for all member repositories." );
        type.addProperty( property );
        response.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( ReindexTask.class.getName() );
        type.setName( "Reindex Repositories" );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "1" );
        property.setName( "Repository ID" );
        property.setType( "string" );
        property.setHelpText( "Type in the repository ID to reindex." );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "2" );
        property.setName( "Repository Group ID" );
        property.setType( "string" );
        property.setHelpText( "Type in the repository group ID to reindex all member repositories." );
        type.addProperty( property );
        response.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( RebuildAttributesTask.class.getName() );
        type.setName( "Rebuild Repository Atributes" );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "1" );
        property.setName( "Repository ID" );
        property.setType( "string" );
        property.setHelpText( "Type in the repository ID to rebuild attributes." );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "2" );
        property.setName( "Repository Group ID" );
        property.setType( "string" );
        property.setHelpText( "Type in the repository group ID to rebuild attributes for all member repositories." );
        type.addProperty( property );
        response.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( ClearCacheTask.class.getName() );
        type.setName( "Clear Repository Caches" );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "1" );
        property.setName( "Repository ID" );
        property.setType( "string" );
        property.setHelpText( "Type in the repository ID to rebuild attributes." );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "2" );
        property.setName( "Repository Group ID" );
        property.setType( "string" );
        property.setHelpText( "Type in the repository group ID to rebuild attributes for all member repositories." );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "3" );
        property.setName( "Repository path" );
        property.setType( "string" );
        property
            .setHelpText( "Type in the repository path from which to clear caches recursively (ie. \"/\" for root or \"/org/apache\")" );
        type.addProperty( property );
        response.addData( type );

        return serialize( variant, response );
    }
}
