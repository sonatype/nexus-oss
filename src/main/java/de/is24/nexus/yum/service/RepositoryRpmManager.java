package de.is24.nexus.yum.service;

import java.io.File;
import java.util.concurrent.ExecutionException;
import org.sonatype.plugin.Managed;
import de.is24.nexus.yum.plugin.impl.MavenRepositoryInfo;
import de.is24.nexus.yum.repository.YumRepository;


@Managed
public interface RepositoryRpmManager {
  String DEFAULT_BEAN_NAME = "repositoryRpmManager";
  String URL_PREFIX = "yum-repos";

  File updateRepository(String repositoryId, String version);

  YumRepository getYumRepository() throws InterruptedException, ExecutionException;

  void updateRepository(MavenRepositoryInfo repositoryInfo);
}
