package org.sonatype.nexus.task;

import junit.framework.Assert;

import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.scheduling.NexusScheduler;

public class EvictUnusedProxiedItemsTaskTest
    extends AbstractMavenRepoContentTests
{
    private static final long A_DAY = 24L * 60L * 60L * 1000L;

    NexusScheduler scheduler;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusConfiguration.setSecurityEnabled( false );

        nexusConfiguration.saveConfiguration();

        scheduler = (NexusScheduler) lookup( NexusScheduler.class );
    }

    public void testDeleteEmptyFolder()
        throws Exception
    {
        fillInRepo();

        while ( scheduler.getActiveTasks().size() > 0 )
        {
            Thread.sleep( 100 );
        }

        long tsDeleting = System.currentTimeMillis() - 10000L;
        long tsToBeKept = tsDeleting + 1000L;
        long tsToBeDeleted = tsDeleting - 1000L;

        String[] itemsToBeKept = { "/org/sonatype/test-evict/1.0/test.txt" };
        String[] itemsToBeDeleted = {
            "/org/sonatype/test-evict/sonatype-test-evict_1.4_mail",
            "/org/sonatype/test-evict/sonatype-test-evict_1.4_mail/1.0-SNAPSHOT/sonatype-test-evict_1.4_mail-1.0-SNAPSHOT.jar" };

        for ( String item : itemsToBeKept )
        {
            ResourceStoreRequest request = new ResourceStoreRequest( item );
            request.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, "127.0.0.1" );

            StorageItem storageItem = apacheSnapshots.retrieveItem( request );

            storageItem.setLastRequested( tsToBeKept );

            apacheSnapshots.storeItem( false, storageItem );
        }

        for ( String item : itemsToBeDeleted )
        {
            ResourceStoreRequest request = new ResourceStoreRequest( item );
            request.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, "127.0.0.1" );

            StorageItem storageItem = apacheSnapshots.retrieveItem( request );

            storageItem.setLastRequested( tsToBeDeleted );

            apacheSnapshots.storeItem( false, storageItem );
        }

        defaultNexus.evictAllUnusedProxiedItems( new ResourceStoreRequest( "/" ), tsDeleting );

        for ( String item : itemsToBeKept )
        {
            try
            {
                assertNotNull( apacheSnapshots.retrieveItem( new ResourceStoreRequest( item ) ) );
            }
            catch ( ItemNotFoundException e )
            {
                fail( "Item should not have been deleted: " + item );
            }
        }

        for ( String item : itemsToBeDeleted )
        {
            try
            {
                apacheSnapshots.retrieveItem( new ResourceStoreRequest( item ) );

                fail( "Item should have been deleted: " + item );
            }
            catch ( ItemNotFoundException e )
            {
                // this is correct
            }
        }
    }

    public void testRunAfterTouched()
        throws Exception
    {
        fillInRepo();

        String item = "/org/sonatype/test-evict/1.0/test.txt";
        ResourceStoreRequest request = new ResourceStoreRequest( item );
        request.getRequestContext().put( AccessManager.REQUEST_REMOTE_ADDRESS, "127.0.0.1" );
        StorageItem storageItem = apacheSnapshots.retrieveItem( request );
        long lastRequest =  System.currentTimeMillis() - 10 * A_DAY;
        storageItem.setLastRequested( lastRequest );
        apacheSnapshots.storeItem( false, storageItem );

        apacheSnapshots.retrieveItem( request );

        defaultNexus.evictAllUnusedProxiedItems( new ResourceStoreRequest( "/" ), System.currentTimeMillis() - 7
            * A_DAY );

        try
        {
            apacheSnapshots.retrieveItem( request );
        }
        catch ( ItemNotFoundException e )
        {
            fail( "Item should not have been deleted: " + item );
        }
    }
}
