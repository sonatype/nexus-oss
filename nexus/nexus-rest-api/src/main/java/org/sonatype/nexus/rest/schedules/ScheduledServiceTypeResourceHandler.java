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
import org.sonatype.nexus.tasks.EvictUnusedProxiedItemsTask;
import org.sonatype.nexus.tasks.PublishIndexesTask;
import org.sonatype.nexus.tasks.PurgeTimeline;
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
        // TODO: This should be auto-discovered!

        ScheduledServiceTypeResourceResponse response = new ScheduledServiceTypeResourceResponse();

        ScheduledServiceTypeResource type = new ScheduledServiceTypeResource();
        type.setId( PublishIndexesTask.class.getName() );
        type.setName( getServiceTypeName( type.getId() ) );
        ScheduledServiceTypePropertyResource property = new ScheduledServiceTypePropertyResource();
        property.setId( PublishIndexesTask.REPOSITORY_OR_GROUP_ID_KEY );
        property.setName( "Repository/Group" );
        property.setType( PROPERTY_TYPE_REPO_OR_GROUP );
        property.setRequired( true );
        property.setHelpText( "Select the repository or repository group to assign to this task." );
        type.addProperty( property );
        response.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( EvictUnusedProxiedItemsTask.class.getName() );
        type.setName( getServiceTypeName( type.getId() ) );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( EvictUnusedProxiedItemsTask.REPOSITORY_OR_GROUP_ID_KEY );
        property.setName( "Repository/Group" );
        property.setType( PROPERTY_TYPE_REPO_OR_GROUP );
        property.setRequired( true );
        property.setHelpText( "Select the repository or repository group to assign to this task." );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( EvictUnusedProxiedItemsTask.EVICT_OLDER_CACHE_ITEMS_THEN_KEY );
        property.setName( "Evict items older than (days)" );
        property.setType( PROPERTY_TYPE_NUMBER );
        property.setRequired( true );
        property
            .setHelpText( "Set the number of days, to evict all unused proxied items that were not used the given number of days." );
        type.addProperty( property );
        response.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( PurgeTimeline.class.getName() );
        type.setName( getServiceTypeName( type.getId() ) );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( PurgeTimeline.PURGE_OLDER_THAN_KEY );
        property.setName( "Purge older items than (days)" );
        property.setType( PROPERTY_TYPE_NUMBER );
        property.setRequired( true );
        property
            .setHelpText( "Set the number of days, to purge items from Timeline that are older then the given number of days." );
        type.addProperty( property );
        response.addData( type );
        // TODO: add params Type and SubType

        type = new ScheduledServiceTypeResource();
        type.setId( ReindexTask.class.getName() );
        type.setName( getServiceTypeName( type.getId() ) );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( PublishIndexesTask.REPOSITORY_OR_GROUP_ID_KEY );
        property.setName( "Repository/Group" );
        property.setType( PROPERTY_TYPE_REPO_OR_GROUP );
        property.setRequired( true );
        property.setHelpText( "Select the repository or repository group to assign to this task." );
        type.addProperty( property );
        response.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( RebuildAttributesTask.class.getName() );
        type.setName( getServiceTypeName( type.getId() ) );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( PublishIndexesTask.REPOSITORY_OR_GROUP_ID_KEY );
        property.setName( "Repository/Group" );
        property.setType( PROPERTY_TYPE_REPO_OR_GROUP );
        property.setRequired( true );
        property.setHelpText( "Select the repository or repository group to assign to this task." );
        type.addProperty( property );
        response.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( ClearCacheTask.class.getName() );
        type.setName( getServiceTypeName( type.getId() ) );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( ClearCacheTask.REPOSITORY_OR_GROUP_ID_KEY );
        property.setName( "Repository/Group" );
        property.setType( PROPERTY_TYPE_REPO_OR_GROUP );
        property.setRequired( true );
        property.setHelpText( "Select the repository or repository group to assign to this task." );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( ClearCacheTask.RESOURCE_STORE_PATH_KEY );
        property.setName( "Repository path" );
        property.setType( PROPERTY_TYPE_STRING );
        property
            .setHelpText( "Type in the repository path from which to clear caches recursively (ie. \"/\" for root or \"/org/apache\")" );
        property.setRequired( false );
        type.addProperty( property );
        response.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( SnapshotRemoverTask.class.getName() );
        type.setName( getServiceTypeName( type.getId() ) );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( PublishIndexesTask.REPOSITORY_OR_GROUP_ID_KEY );
        property.setName( "Repository/Group" );
        property.setType( PROPERTY_TYPE_REPO_OR_GROUP );
        property.setRequired( true );
        property.setHelpText( "Select the repository or repository group to assign to this task." );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( SnapshotRemoverTask.MIN_SNAPSHOTS_TO_KEEP_KEY );
        property.setName( "Minimum snapshot count" );
        property.setType( PROPERTY_TYPE_NUMBER );
        property.setRequired( true );
        property.setHelpText( "Minimum number of snapshots to keep for one GAV." );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( SnapshotRemoverTask.REMOVE_OLDER_THAN_DAYS_KEY );
        property.setName( "Snapshot retention (days)" );
        property.setType( PROPERTY_TYPE_NUMBER );
        property.setRequired( true );
        property
            .setHelpText( "The job will purge all snapshots older than the entered number of days, but will obey to Min. count of snapshots to keep." );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( SnapshotRemoverTask.REMOVE_IF_RELEASE_EXISTS_KEY );
        property.setName( "Remove if released" );
        property.setType( PROPERTY_TYPE_BOOLEAN );
        property.setRequired( true );
        property.setHelpText( "The job will purge all snapshots that have a corresponding released artifact (same version not including the -SNAPSHOT)." );
        type.addProperty( property );
        response.addData( type );

        return serialize( variant, response );
    }
}
