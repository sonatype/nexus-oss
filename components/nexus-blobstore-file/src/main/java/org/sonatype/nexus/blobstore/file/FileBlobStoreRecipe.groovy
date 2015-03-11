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
