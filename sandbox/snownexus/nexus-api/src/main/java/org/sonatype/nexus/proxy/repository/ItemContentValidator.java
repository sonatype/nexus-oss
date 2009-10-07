package org.sonatype.nexus.proxy.repository;

import java.util.List;

import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;

/**
 * Item content validator.
 * 
 * @author cstamas
 */
public interface ItemContentValidator
{
    boolean isRemoteItemContentValid( ProxyRepository proxy, ResourceStoreRequest request, String baseUrl,
                                      AbstractStorageItem item, List<NexusArtifactEvent> events )
        throws StorageException;
}
