package de.is24.nexus.yum.service;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Future;
import org.sonatype.nexus.proxy.repository.Repository;
import de.is24.nexus.yum.repository.YumRepository;


public interface YumService {
  String DEFAULT_BEAN_NAME = "yumService";

  Future<YumRepository> createYumRepository(Repository repository);

  Future<YumRepository> createYumRepository(Repository repository, String version, File yumRepoDir, URL yumRepoUrl);

  void deactivate();

  YumRepository getRepository(Repository repository, String version, URL repoBaseUrl) throws Exception;

  void markDirty(Repository repository, String itemVersion);

  void activate();

  File getBaseTempDir();

  Future<YumRepository> createYumRepository(File rpmBaseDir, String rpmBaseUrl, File yumRepoBaseDir, URL yumRepoUrl,
    String id,
    boolean singleRpmPerDirectory);

  Future<YumRepository> addToYumRepository(Repository repository, String path);
}
