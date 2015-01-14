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
package org.sonatype.nexus.internal.repository;

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
import org.sonatype.nexus.configuration.application.ApplicationDirectories;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Temporary {@link BlobStore} provider.
 *
 * @since 3.0
 */
@Named(TemporaryBlobStoreProvider.NAME)
@Singleton
public class TemporaryBlobStoreProvider
  implements Provider<BlobStore>
{
  public static final String BLOBS = "blobs";

  public static final String NAME = "temp";

  private final ApplicationDirectories directories;

  @Inject
  public TemporaryBlobStoreProvider(final ApplicationDirectories directories) {
    this.directories = checkNotNull(directories);
  }

  @Override
  public BlobStore get() {
    Path root = directories.getWorkDirectory(BLOBS).toPath().resolve(NAME);
    Path content = root.resolve("content");
    Path metadata = root.resolve("metadata");
    BlobMetadataStore metadataStore = MapdbBlobMetadataStore.create(metadata.toFile());
    return new FileBlobStore(content, new VolumeChapterLocationStrategy(), new SimpleFileOperations(), metadataStore);
  }
}
