package org.sonatype.nexus.proxy.wastebasket;

import java.io.IOException;

import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;

public interface Wastebasket
{
    DeleteOperation getDeleteOperation();

    void setDeleteOperation( DeleteOperation deleteOperation );

    /**
     * Returns the count of items in wastebasket.
     * 
     * @return
     * @throws IOException
     */
    long getItemCount()
        throws IOException;

    /**
     * Returns the sum of sizes of items in the wastebasket.
     * 
     * @return
     * @throws IOException
     */
    long getSize()
        throws IOException;

    /**
     * Empties the wastebasket.
     * 
     * @throws IOException
     */
    void purge()
        throws IOException;

    /**
     * Performs a delete operation. It delets at once if item is file or link. If it is a collection, it will delete it
     * and all it's subitems (recursively).
     * 
     * @param path
     * @throws IOException
     */
    void delete( RepositoryItemUid uid, LocalRepositoryStorage ls )
        throws StorageException;
}
