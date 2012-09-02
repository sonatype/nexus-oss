package org.sonatype.nexus.plugins.yum.repository.service;

import java.io.File;
import java.net.URL;

import org.sonatype.nexus.plugins.yum.repository.YumRepository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.scheduling.ScheduledTask;


public interface YumService {
  ScheduledTask<YumRepository> createYumRepository(Repository repository);

  ScheduledTask<YumRepository> createYumRepository(Repository repository, String version, File yumRepoDir,
    URL yumRepoUrl);

  YumRepository getRepository(Repository repository, String version, URL repoBaseUrl) throws Exception;

  void markDirty(Repository repository, String itemVersion);

  ScheduledTask<YumRepository> createYumRepository(File rpmBaseDir, String rpmBaseUrl, File yumRepoBaseDir,
    URL yumRepoUrl,
    String id,
    boolean singleRpmPerDirectory);

  ScheduledTask<YumRepository> addToYumRepository(Repository repository, String path);

  void recreateRepository(Repository repository);

  ScheduledTask<YumRepository> createGroupRepository(GroupRepository groupRepository);
}
