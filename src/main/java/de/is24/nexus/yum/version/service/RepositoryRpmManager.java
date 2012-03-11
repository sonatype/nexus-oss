package de.is24.nexus.yum.version.service;

import java.io.File;
import java.util.concurrent.ExecutionException;
import de.is24.nexus.yum.plugin.impl.MavenRepositoryInfo;
import de.is24.nexus.yum.repository.YumRepository;


public interface RepositoryRpmManager {
  String URL_PREFIX = "yum/repo";

  File updateRepository(String repositoryId, String version);

  YumRepository getYumRepository() throws InterruptedException, ExecutionException;

  void updateRepository(MavenRepositoryInfo repositoryInfo);
}
