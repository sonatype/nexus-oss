package org.sonatype.nexus.plugins.yum.plugin.m2yum;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.maven2.M2Repository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.plugin.Managed;


@Component(
  role = Repository.class, hint = M2YumRepository.ID, instantiationStrategy = "per-lookup",
  description = "Maven2-Yum Repository"
)
@Managed
@SuppressWarnings("deprecation")
public class M2YumRepository extends M2Repository {
  public static final String ID = "maven2yum";

  @Override
  public boolean isMavenMetadataPath(String path) {
    return super.isMavenMetadataPath(path) || isYumRepoPath(path);
  }

  @Override
  public void storeItem(boolean fromTask, StorageItem item) throws UnsupportedStorageOperationException,
    IllegalOperationException, StorageException {
    if (isYumRepoPath(item.getPath())) {
      // Ignore items within repodata folder.
      return;
    }

    super.storeItem(fromTask, item);
  }

  private boolean isYumRepoPath(final String path) {
    return path.startsWith("/repodata/");
  }

}
