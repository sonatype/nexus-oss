package org.sonatype.nexus.repository.storage;

import javax.annotation.Nullable;

import org.sonatype.nexus.blobstore.api.BlobStore;

/**
 * TODO: No doubt this will find its way into another module at some point.
 *
 * @since 3.0
 */
public interface BlobStoreManager
{
  @Nullable
  BlobStore getStore(String storeName);

  // TODO: Extend this interface to include blob store management features, such as declaring them.

}
