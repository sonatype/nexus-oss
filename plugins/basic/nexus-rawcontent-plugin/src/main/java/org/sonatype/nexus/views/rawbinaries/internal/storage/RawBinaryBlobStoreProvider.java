package org.sonatype.nexus.views.rawbinaries.internal.storage;

import java.nio.file.Path;

import javax.inject.Inject;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.file.FileBlobStore;
import org.sonatype.nexus.blobstore.file.VolumeChapterLocationStrategy;
import org.sonatype.nexus.blobstore.file.MapdbBlobMetadataStore;
import org.sonatype.nexus.blobstore.file.SimpleFileOperations;
import org.sonatype.nexus.configuration.application.ApplicationDirectories;

import com.google.inject.Provider;

/**
 * @since 3.0
 */
public class RawBinaryBlobStoreProvider
    implements Provider<BlobStore>
{
  private final ApplicationDirectories appDirs;

  private MapdbBlobMetadataStore metadataStore;

  @Inject
  public RawBinaryBlobStoreProvider(final ApplicationDirectories appDirs) {
    this.appDirs = appDirs;
  }

  @Override
  public BlobStore get() {
    final Path root = appDirs.getWorkDirectory("rawBinaries").toPath();

    Path content = root.resolve("content");
    Path metadata = root.resolve("metadata");

    this.metadataStore = new MapdbBlobMetadataStore(metadata.toFile());

    final FileBlobStore fileBlobStore = new FileBlobStore(content, new VolumeChapterLocationStrategy(),
        new SimpleFileOperations(), metadataStore);

    return fileBlobStore;
  }
}
