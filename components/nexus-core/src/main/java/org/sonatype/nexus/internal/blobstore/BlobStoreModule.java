package org.sonatype.nexus.internal.blobstore;

import javax.inject.Named;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.api.BlobStoreFactory;
import org.sonatype.nexus.blobstore.api.BlobStoreImpl;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * BlobStore module.
 *
 * @since 3.0
 */
@Named
public class BlobStoreModule
    extends AbstractModule
{
  @Override
  protected void configure() {
    install(new FactoryModuleBuilder()
        .implement(BlobStore.class, BlobStoreImpl.class)
        .build(BlobStoreFactory.class)
    );
  }
}
