/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.blobstore.file.guice;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.file.BlobMetadataStore;
import org.sonatype.nexus.blobstore.file.FileBlobStore;
import org.sonatype.nexus.blobstore.file.FileOperations;
import org.sonatype.nexus.blobstore.file.FilePathPolicy;
import org.sonatype.nexus.blobstore.file.HashingSubdirFileLocationPolicy;
import org.sonatype.nexus.blobstore.file.SimpleFileOperations;
import org.sonatype.nexus.blobstore.file.kazuki.KazukiBlobMetadataStore;
import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.util.file.DirSupport;
import org.sonatype.sisu.goodies.lifecycle.Lifecycle;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Guice module for creating filesystem-based {@link BlobStore} instances. The name provided to the constructor must
 * be unique across an instance of Nexus, and is used to locate the Kazuki database and the blob store's content
 * directory.
 *
 * This private module exposes two beans:
 *
 * <ul>
 * <li>a {@link BlobStore} {@code @Named(<name constructor parameter>)} </li>
 * <li>a {@link Lifecycle} {@code @Named(<name constructor parameter>)} </li>
 * </ul>
 *
 * @since 3.0
 */
public class FileBlobStoreModule
    extends PrivateModule
{
  private final String name;

  private final Logger log = LoggerFactory.getLogger(FileBlobStoreModule.class);

  /**
   * The {@code name} parameter must be unique across all of Nexus.
   */
  public FileBlobStoreModule(final String name) {
    this.name = checkNotNull(name, "name");
  }

  @Override
  protected void configure() {
    // Create and expose blob store itself
    bind(String.class).annotatedWith(Names.named("FileBlobStoreModule.name-key")).toInstance(name);
    bind(BlobStore.class).annotatedWith(Names.named(name)).toProvider(BlobStoreProvider.class);
    bind(FileOperations.class).to(SimpleFileOperations.class).in(Scopes.SINGLETON);
    expose(BlobStore.class).annotatedWith(Names.named(name));

    // Expose the metadata store's lifecycle
    bind(Lifecycle.class).annotatedWith(Names.named(name))
        .to(BlobMetadataStore.class);
    expose(Lifecycle.class).annotatedWith(Names.named(name));
  }

  @Provides
  @Singleton
  BlobMetadataStore getKazukiBlobMetadataStore(@Named("fileblobstore-internal") final KazukiHolder holder) {
    return new KazukiBlobMetadataStore(holder.getLifecycle(), holder.getKvStore(), holder.getSchemaStore(),
        holder.getSecondaryIndexStore());
  }

  static class BlobStoreProvider
      implements Provider<BlobStore>
  {
    private final String name;

    private final FilePathPolicy paths;

    private final FileOperations fileOperations;

    private final BlobMetadataStore metadataStore;

    @Inject
    BlobStoreProvider(@Named("FileBlobStoreModule.name-key") final String name, final FilePathPolicy paths,
                      final FileOperations fileOperations,
                      final BlobMetadataStore metadataStore)
    {
      this.name = name;
      this.paths = paths;
      this.fileOperations = fileOperations;
      this.metadataStore = metadataStore;
    }

    @Override
    public BlobStore get() {
      return new FileBlobStore(name, paths, fileOperations, metadataStore);
    }
  }

  @Provides
  FilePathPolicy provideFilePathPolicy(final ApplicationDirectories applicationDirectories) {
    final File workDirectory = applicationDirectories.getWorkDirectory("fileblobstore/" + name + "/content");
    return new HashingSubdirFileLocationPolicy(workDirectory.toPath());
  }

  @Provides
  @Singleton
  @Named("fileblobstore-internal")
  KazukiHolder getKazukiHolder(final ApplicationDirectories applicationDirectories) throws IOException {
    File dir = applicationDirectories.getWorkDirectory("fileblobstore/" + name + "/db");
    log.info("File blob store {} metadata stored in Kazuki db: {}", name, dir);
    DirSupport.mkdir(dir);
    File file = new File(dir, dir.getName());

    return new KazukiBuilder(name, file.getAbsoluteFile()).build();
  }
}
