package org.sonatype.nexus.blobstore.api;

import javax.annotation.Nonnull;

/**
 * 
 * @since 3.0
 */
public interface Recipe
{
  /**
   *
   */
  Type getType();

  /**
   *
   */
  void apply(@Nonnull BlobStore blobStore)  throws Exception;
}
