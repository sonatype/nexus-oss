package org.sonatype.nexus.blobstore.api.configuration;

/**
 * @since 3.0
 */
public interface BlobStoreConfiguration
{
  String getName();

  void setName(String name);

  String getPath();

  void setPath(String path);
}
