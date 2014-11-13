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
package org.sonatype.nexus.component.services.internal.storage;


import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.file.BlobMetadataStore;
import org.sonatype.nexus.blobstore.file.FileBlobStore;
import org.sonatype.nexus.blobstore.file.MapdbBlobMetadataStore;
import org.sonatype.nexus.blobstore.file.SimpleFileOperations;
import org.sonatype.nexus.blobstore.file.VolumeChapterLocationStrategy;
import org.sonatype.nexus.component.services.storage.ComponentStore;
import org.sonatype.nexus.configuration.application.ApplicationDirectories;

/**
 * Provides {@link BlobStore} instance for {@link ComponentStore}.
 *
 * TODO: Replace this with something that gets a BlobStoreManager or similar that can provide the right instance(s).
 *
 * @since 3.0
 */
@Named(ComponentBlobStoreProvider.NAME)
@Singleton
class ComponentBlobStoreProvider
    implements Provider<BlobStore>
{
  public static final String NAME = "componentBlobs";

  private final ApplicationDirectories appDirs;

  private BlobMetadataStore metadataStore;

  @Inject
  public ComponentBlobStoreProvider(final ApplicationDirectories appDirs) {
    this.appDirs = appDirs;
  }

  @Override
  public BlobStore get() {
    final Path root = appDirs.getWorkDirectory(NAME).toPath();

    Path content = root.resolve("content");
    Path metadata = root.resolve("metadata");

    this.metadataStore = MapdbBlobMetadataStore.create(metadata.toFile());

    return new FileBlobStore(content, new VolumeChapterLocationStrategy(), new SimpleFileOperations(), metadataStore);
  }
}
