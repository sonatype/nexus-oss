package org.sonatype.nexus.blobstore.file.task;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.scheduling.NexusTaskSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link NexusTask} that compacts a blob store.
 *
 * @since 3.0
 */
public class CompactTask
    extends NexusTaskSupport
{
  private final BlobStore blobStore;

  public CompactTask(final BlobStore blobStore) {
    this.blobStore = checkNotNull(blobStore);
  }

  @Override
  protected void execute() throws Exception {
    blobStore.compact();
  }

  @Override
  protected String getMessage() {
    return "Compacting blob store " + blobStore.getName();
  }
}
