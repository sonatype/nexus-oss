package de.is24.nexus.yum.service;

import java.io.File;
import java.net.URL;

import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.scheduling.ScheduledTask;

import de.is24.nexus.yum.repository.YumRepository;


public interface YumService {
  ScheduledTask<YumRepository> createYumRepository(Repository repository);

  ScheduledTask<YumRepository> createYumRepository(Repository repository, String version, File yumRepoDir,
    URL yumRepoUrl);

  void deactivate();

  YumRepository getRepository(Repository repository, String version, URL repoBaseUrl) throws Exception;

  void markDirty(Repository repository, String itemVersion);

  void activate();

  File getBaseTempDir();

  ScheduledTask<YumRepository> createYumRepository(File rpmBaseDir, String rpmBaseUrl, File yumRepoBaseDir,
    URL yumRepoUrl,
    String id,
    boolean singleRpmPerDirectory);

  ScheduledTask<YumRepository> addToYumRepository(Repository repository, String path);

  boolean isActive();

  void recreateRepository(Repository repository);

  ScheduledTask<YumRepository> createGroupRepository(GroupRepository groupRepository);
}
