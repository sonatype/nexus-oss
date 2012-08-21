package org.sonatype.nexus.plugins.yum.plugin;

import org.sonatype.nexus.proxy.repository.Repository;

import org.sonatype.nexus.plugins.yum.plugin.impl.DelayedDirectoryDeletionTask;

public interface DeletionService {

  void deleteRpm(Repository repository, String path);

  void deleteDirectory(Repository repository, String path);

  void execute(DelayedDirectoryDeletionTask delayedDirectoryDeletionTask);

}
