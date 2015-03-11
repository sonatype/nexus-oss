package org.sonatype.nexus.blobstore.api;

/**
 * BlobStore factory.
 * 
 * @since 3.0
 * 
 */
public interface BlobStoreFactory
{
  BlobStore create(Type type);
}
