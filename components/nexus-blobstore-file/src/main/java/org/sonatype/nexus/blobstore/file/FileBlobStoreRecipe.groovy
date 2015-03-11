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
package org.sonatype.nexus.blobstore.file

import java.nio.file.Path
import java.nio.file.Paths

import javax.annotation.Nonnull
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

import org.sonatype.nexus.blobstore.api.BlobStore
import org.sonatype.nexus.blobstore.api.Recipe
import org.sonatype.nexus.blobstore.api.Type
import org.sonatype.sisu.goodies.common.ComponentSupport

/**
 * @since 3.0
 */
@Named(FileBlobStoreRecipe.NAME)
@Singleton
class FileBlobStoreRecipe
    extends ComponentSupport
    implements Recipe
{
  static final String NAME = 'file'

  private final Type type;

  @Inject
  Provider<FileBlobStorageFacet> storageFacet

  @Inject
  public FileBlobStoreRecipe(final @Named(FileType.NAME) Type type) {
    this.type = type
  }

  @Override
  Type getType() {
    return type
  }

  @Override
  public void apply(@Nonnull final BlobStore blobStore) throws Exception {
    blobStore.attach(storageFacet.get());
//    Path root = Paths.get(getPath(blobStore.getBlobStoreConfiguration().getAttributes()));
//    Path content = root.resolve("content");
//    File metadataFile = root.resolve("metadata").toFile();
//    content.toFile().mkdirs();
//    metadataFile.mkdirs();
//    blobStore.attach(new FileBlobStorageFacet(
//        content,
//        new VolumeChapterLocationStrategy(),
//        new SimpleFileOperations(),
//        MapdbBlobMetadataStore.create(metadataFile)));
  }

//  private String getPath(final Map<String, Map<String, Object>> attributes) {
//    return (String) attributes.get("file").get("path");
//  }
}
