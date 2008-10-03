package org.sonatype.nexus.rest.schedules;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.maven.tasks.SnapshotRemoverTask;
import org.sonatype.nexus.rest.model.ScheduledServiceTypePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceTypeResource;
import org.sonatype.nexus.rest.model.ScheduledServiceTypeResourceResponse;
import org.sonatype.nexus.tasks.ClearCacheTask;
import org.sonatype.nexus.tasks.EmptyTrashTask;
import org.sonatype.nexus.tasks.EvictUnusedProxiedItemsTask;
import org.sonatype.nexus.tasks.PublishIndexesTask;
import org.sonatype.nexus.tasks.PurgeTimeline;
import org.sonatype.nexus.tasks.RebuildAttributesTask;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.nexus.tasks.SynchronizeShadowsTask;

/**
 * @author tstevens
 * @plexus.component role-hint="ScheduledServiceTypePlexusResource"
 */
public class ScheduledServiceTypePlexusResource
    extends AbstractScheduledServicePlexusResource
{

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/schedule_types";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
     // TODO: This should be auto-discovered!

        ScheduledServiceTypeResourceResponse result = new ScheduledServiceTypeResourceResponse();

        ScheduledServiceTypeResource type = new ScheduledServiceTypeResource();
        type.setId( PublishIndexesTask.HINT );
        type.setName( getServiceTypeName( PublishIndexesTask.HINT ) );
        ScheduledServiceTypePropertyResource property = new ScheduledServiceTypePropertyResource();
        property.setId( PublishIndexesTask.REPOSITORY_OR_GROUP_ID_KEY );
        property.setName( "Repository/Group" );
        property.setType( PROPERTY_TYPE_REPO_OR_GROUP );
        property.setRequired( true );
        property.setHelpText( "Select the repository or repository group to assign to this task." );
        type.addProperty( property );
        result.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( SynchronizeShadowsTask.HINT );
        type.setName( getServiceTypeName( SynchronizeShadowsTask.HINT ) );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( SynchronizeShadowsTask.SHADOW_REPOSITORY_ID );
        property.setName( "Shadow Repository" );
        property.setType( PROPERTY_TYPE_SHADOW );
        property.setRequired( true );
        property.setHelpText( "Select the repository shadow to assign to this task." );
        type.addProperty( property );
        result.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( EvictUnusedProxiedItemsTask.HINT );
        type.setName( getServiceTypeName( EvictUnusedProxiedItemsTask.HINT ) );
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
        result.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( PurgeTimeline.HINT );
        type.setName( getServiceTypeName( PurgeTimeline.HINT ) );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( PurgeTimeline.PURGE_OLDER_THAN_KEY );
        property.setName( "Purge older items than (days)" );
        property.setType( PROPERTY_TYPE_NUMBER );
        property.setRequired( true );
        property
            .setHelpText( "Set the number of days, to purge items from Timeline that are older then the given number of days." );
        type.addProperty( property );
        result.addData( type );
        // TODO: add params Type and SubType

        type = new ScheduledServiceTypeResource();
        type.setId( ReindexTask.HINT );
        type.setName( getServiceTypeName( ReindexTask.HINT ) );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( PublishIndexesTask.REPOSITORY_OR_GROUP_ID_KEY );
        property.setName( "Repository/Group" );
        property.setType( PROPERTY_TYPE_REPO_OR_GROUP );
        property.setRequired( true );
        property.setHelpText( "Select the repository or repository group to assign to this task." );
        type.addProperty( property );
        result.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( RebuildAttributesTask.HINT );
        type.setName( getServiceTypeName( RebuildAttributesTask.HINT ) );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( PublishIndexesTask.REPOSITORY_OR_GROUP_ID_KEY );
        property.setName( "Repository/Group" );
        property.setType( PROPERTY_TYPE_REPO_OR_GROUP );
        property.setRequired( true );
        property.setHelpText( "Select the repository or repository group to assign to this task." );
        type.addProperty( property );
        result.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( ClearCacheTask.HINT );
        type.setName( getServiceTypeName( ClearCacheTask.HINT ) );
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
        result.addData( type );

        type = new ScheduledServiceTypeResource();
        type.setId( SnapshotRemoverTask.HINT );
        type.setName( getServiceTypeName( SnapshotRemoverTask.HINT ) );
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
        property.setRequired( false );
        property.setHelpText( "Minimum number of snapshots to keep for one GAV." );
        type.addProperty( property );
        property = new ScheduledServiceTypePropertyResource();
        property.setId( SnapshotRemoverTask.REMOVE_OLDER_THAN_DAYS_KEY );
        property.setName( "Snapshot retention (days)" );
        property.setType( PROPERTY_TYPE_NUMBER );
        property.setRequired( false );
        property
            .setHelpText( "The job will purge all snapshots older than the entered number of days, but will obey to Min. count of snapshots to keep." );
        type.addProperty( property );        
        
        property = new ScheduledServiceTypePropertyResource();
        property.setId( SnapshotRemoverTask.REMOVE_IF_RELEASE_EXISTS_KEY );
        property.setName( "Remove if released" );
        property.setType( PROPERTY_TYPE_BOOLEAN );
        property.setRequired( false );
        property
            .setHelpText( "The job will purge all snapshots that have a corresponding released artifact (same version not including the -SNAPSHOT)." );
        type.addProperty( property );
        
        result.addData( type );
        
        type = new ScheduledServiceTypeResource();
        type.setId( EmptyTrashTask.HINT );
        type.setName( getServiceTypeName( EmptyTrashTask.HINT ) );
        result.addData( type );

        return result;
    }
    
    

}
