package org.sonatype.nexus.plugins.yum.metarepo.service;

import java.io.File;
import java.util.concurrent.ExecutionException;
import org.sonatype.nexus.plugins.yum.plugin.impl.MavenRepositoryInfo;
import org.sonatype.nexus.plugins.yum.repository.YumRepository;


public interface RepositoryRpmManager {
  String URL_PREFIX = "yum/repo";

  File updateRepository(String repositoryId, String version);

  YumRepository getYumRepository() throws InterruptedException, ExecutionException;

  void updateRepository(MavenRepositoryInfo repositoryInfo);
}
