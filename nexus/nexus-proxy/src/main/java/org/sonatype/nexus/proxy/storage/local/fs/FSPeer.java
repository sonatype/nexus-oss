package org.sonatype.nexus.proxy.storage.local.fs;

import java.io.File;
import java.util.Collection;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * This is file system specific component encapsulating all the "logic" with file handling, made reusable.
 * 
 * @author cstamas
 */
public interface FSPeer
{
    public boolean isReachable( Repository repository, ResourceStoreRequest request, File target )
        throws LocalStorageException;

    public boolean containsItem( Repository repository, ResourceStoreRequest request, File target )
        throws LocalStorageException;

    public File retrieveItem( Repository repository, ResourceStoreRequest request, File target )
        throws ItemNotFoundException, LocalStorageException;

    public void storeItem( Repository repository, StorageItem item, File target, ContentLocator cl )
        throws UnsupportedStorageOperationException, LocalStorageException;

    public void shredItem( Repository repository, ResourceStoreRequest request, File target )
        throws ItemNotFoundException, UnsupportedStorageOperationException, LocalStorageException;

    public Collection<File> listItems( Repository repository, ResourceStoreRequest request, File target )
        throws ItemNotFoundException, LocalStorageException;
}
