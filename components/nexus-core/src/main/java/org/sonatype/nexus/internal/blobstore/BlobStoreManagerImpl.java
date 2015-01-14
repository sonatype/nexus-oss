/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.internal.blobstore;

import java.nio.file.Path;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.api.BlobStoreManager;
import org.sonatype.nexus.blobstore.file.BlobMetadataStore;
import org.sonatype.nexus.blobstore.file.FileBlobStore;
import org.sonatype.nexus.blobstore.file.MapdbBlobMetadataStore;
import org.sonatype.nexus.blobstore.file.SimpleFileOperations;
import org.sonatype.nexus.blobstore.file.VolumeChapterLocationStrategy;
import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport;
import org.sonatype.nexus.configuration.application.ApplicationDirectories;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport.State.STARTED;

/**
 * Default {@link BlobStoreManager} implementation.
 *
 * @since 3.0
 */
@Named
@Singleton
public class BlobStoreManagerImpl
    extends StateGuardLifecycleSupport
    implements BlobStoreManager
{
  private static final String BASEDIR = "blobs";

  private final Path basedir;

  private final Map<String,BlobStore> stores = Maps.newHashMap();

  @Inject
  public BlobStoreManagerImpl(final ApplicationDirectories directories) {
    checkNotNull(directories);
    this.basedir = directories.getWorkDirectory(BASEDIR).toPath();
  }

  // TODO: read configuration for blob-stores and start

  @Override
  protected void doStop() throws Exception {
    // stop all known blob-stores
    for (Map.Entry<String,BlobStore> entry : stores.entrySet()) {
      String name = entry.getKey();
      BlobStore store = entry.getValue();
      log.debug("Stopping blob-store: {}", name);
      try {
        store.stop();
      }
      catch (Exception e) {
        log.debug("Failed to stop blob-store: {}", name, e);
      }
    }
  }

  @Override
  @Guarded(by = STARTED)
  public BlobStore get(final String name) {
    checkNotNull(name);

    synchronized (stores) {
      BlobStore store = stores.get(name);

      // blob-store not defined, create
      if (store == null) {
        store = create(name);

        // start
        try {
          store.start();
        }
        catch (Exception e) {
          throw Throwables.propagate(e);
        }

        // cache
        stores.put(name, store);
      }

      return store;
    }
  }

  /**
   * Create a new blob-store with given name.
   */
  private BlobStore create(final String name) {
    log.debug("Creating blob-store: {}", name);

    Path root = basedir.resolve(name);
    Path content = root.resolve("content");
    Path metadata = root.resolve("metadata");
    BlobMetadataStore metadataStore = MapdbBlobMetadataStore.create(metadata.toFile());

    return new FileBlobStore(
        content,
        new VolumeChapterLocationStrategy(),
        new SimpleFileOperations(),
        metadataStore
    );
  }
}
