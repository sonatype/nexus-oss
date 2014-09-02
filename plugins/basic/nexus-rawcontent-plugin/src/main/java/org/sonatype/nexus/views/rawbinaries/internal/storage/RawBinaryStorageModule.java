package org.sonatype.nexus.views.rawbinaries.internal.storage;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.views.rawbinaries.internal.storage.orientblobstore.OrientRawBinaryStore;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * @since 3.0
 */
@Named
public class RawBinaryStorageModule
    extends AbstractModule
{
  @Override
  protected void configure() {
    bind(BlobStore.class).annotatedWith(Names.named("rawBinaryBlobs")).toProvider(RawBinaryBlobStoreProvider.class)
        .in(Singleton.class);
    bind(RawBinaryStore.class).to(OrientRawBinaryStore.class).in(Singleton.class);
    //bind(RawBinaryStorageBooter.class);
  }
}
