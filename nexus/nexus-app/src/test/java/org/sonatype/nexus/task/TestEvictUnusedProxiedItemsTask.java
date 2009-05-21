package org.sonatype.nexus.task;

import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageItem;

public class TestEvictUnusedProxiedItemsTask
    extends AbstractMavenRepoContentTests
{

    public void testDeleteEmptyFolder()
        throws Exception
    {
        fillInRepo();

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

            StorageItem storageItem = apacheSnapshots.retrieveItem( request );

            storageItem.setLastRequested( tsToBeKept );
            
            apacheSnapshots.storeItem( false, storageItem );
        }

        for ( String item : itemsToBeDeleted )
        {
            ResourceStoreRequest request = new ResourceStoreRequest( item );

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
    
}
