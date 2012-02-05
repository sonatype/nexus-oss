package de.is24.nexus.yum.plugin;

import org.sonatype.nexus.proxy.repository.Repository;

import de.is24.nexus.yum.plugin.impl.DelayedDirectoryDeletionTask;

public interface DeletionService {

  void deleteRpm(Repository repository, String path);

  void deleteDirectory(Repository repository, String path);

  void execute(DelayedDirectoryDeletionTask delayedDirectoryDeletionTask);

}
