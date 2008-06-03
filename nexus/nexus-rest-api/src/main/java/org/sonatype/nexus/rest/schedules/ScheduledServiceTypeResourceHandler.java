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
import org.sonatype.nexus.maven.tasks.SnapshotRemoverTask;
import org.sonatype.nexus.rest.model.ScheduledServiceTypePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceTypeResource;
import org.sonatype.nexus.rest.model.ScheduledServiceTypeResourceResponse;
import org.sonatype.nexus.tasks.ClearCacheTask;
import org.sonatype.nexus.tasks.PublishIndexesTask;
import org.sonatype.nexus.tasks.RebuildAttributesTask;
import org.sonatype.nexus.tasks.ReindexTask;

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
        type.setName( getServiceTypeName( type.getId() ) );
        ScheduledServiceTypePropertyResource property = new ScheduledServiceTypePropertyResource();
        property.setId( "1" );
        property.setName( "Repository" );
        property.setType( PROPERTY_TYPE_REPO );
        property.setHelpText( "Select a repository to publish the indexes." );
        property.setRequired( false );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "2" );
        property.setName( "Repository Group" );
        property.setType( PROPERTY_TYPE_REPO_GROUP );
        property.setHelpText( "Select a repository group to publish the indexes for all member repositories." );
        property.setRequired( false );
        type.addProperty( property );
        response.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( ReindexTask.class.getName() );
        type.setName( getServiceTypeName( type.getId() ) );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "1" );
        property.setName( "Repository" );
        property.setType( PROPERTY_TYPE_REPO );
        property.setHelpText( "Select a repository to reindex." );
        property.setRequired( false );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "2" );
        property.setName( "Repository Group" );
        property.setType( PROPERTY_TYPE_REPO_GROUP );
        property.setHelpText( "Select a repository group to reindex all member repositories." );
        property.setRequired( false );
        type.addProperty( property );
        response.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( RebuildAttributesTask.class.getName() );
        type.setName( getServiceTypeName( type.getId() ) );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "1" );
        property.setName( "Repository" );
        property.setType( PROPERTY_TYPE_REPO );
        property.setHelpText( "Select a repository to rebuild attributes." );
        property.setRequired( false );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "2" );
        property.setName( "Repository Group" );
        property.setType( PROPERTY_TYPE_REPO_GROUP );
        property.setHelpText( "Select a repository group to rebuild attributes for all member repositories." );
        property.setRequired( false );
        type.addProperty( property );
        response.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( ClearCacheTask.class.getName() );
        type.setName( getServiceTypeName( type.getId() ) );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "1" );
        property.setName( "Repository" );
        property.setType( PROPERTY_TYPE_REPO );
        property.setHelpText( "Select a repository to rebuild attributes." );
        property.setRequired( false );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "2" );
        property.setName( "Repository Group" );
        property.setType( PROPERTY_TYPE_REPO_GROUP );
        property.setHelpText( "Select a repository group to rebuild attributes for all member repositories." );
        property.setRequired( false );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "3" );
        property.setName( "Repository path" );
        property.setType( PROPERTY_TYPE_STRING );
        property.setRequired( true );
        property
            .setHelpText( "Type in the repository path from which to clear caches recursively (ie. \"/\" for root or \"/org/apache\")" );
        type.addProperty( property );
        response.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( SnapshotRemoverTask.class.getName() );
        type.setName( getServiceTypeName( type.getId() ) );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "1" );
        property.setName( "Repository" );
        property.setType( PROPERTY_TYPE_REPO );
        property.setHelpText( "Select a repository to remove snapshots from." );
        property.setRequired( false );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "2" );
        property.setName( "Repository Group" );
        property.setType( PROPERTY_TYPE_REPO_GROUP );
        property.setHelpText( "Select a repository group to remove snapshots from all it's member repositories." );
        property.setRequired( false );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "3" );
        property.setName( "Minimum snapshot count" );
        property.setType( PROPERTY_TYPE_NUMBER );
        property.setHelpText( "Minimum number of snapshots to keep for one GAV." );
        property.setRequired( true );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( "4" );
        property.setName( "Snapshot retention (days)" );
        property.setType( PROPERTY_TYPE_NUMBER );
        property
            .setHelpText( "The job will purge all snapshots older than the entered number of days, but will obey to Min. count of snapshots to keep." );
        property.setRequired( true );
        type.addProperty( property );
        response.addData( type );

        return serialize( variant, response );
    }
}
