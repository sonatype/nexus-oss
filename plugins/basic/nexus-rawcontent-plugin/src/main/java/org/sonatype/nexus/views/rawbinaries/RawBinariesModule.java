package org.sonatype.nexus.views.rawbinaries;

import org.sonatype.nexus.blobstore.api.BlobStore;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * @since 3.0
 */
public class RawBinariesModule
    extends AbstractModule
{
  @Override
  protected void configure() {
    bind(BlobStore.class).annotatedWith(Names.named("rawBinaryBlobs")).toProvider(RawBinaryBlobStoreProvider.class);
  }
}
