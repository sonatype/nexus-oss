package de.is24.nexus.yum.repository;

import static org.apache.commons.io.FileUtils.listFiles;
import java.io.File;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.is24.nexus.yum.plugin.impl.MavenRepositoryInfo;
import de.is24.nexus.yum.service.RepositoryRpmManager;


/**
 * This job scans a {@link MavenHostedRepository} for RPMs and adds each version
 * to the {@link MavenRepositoryInfo#addVersion(String) MavenRepositoryInfo}.
 *
 * @author sherold
 */
public class RepositoryScanningJob implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(RepositoryScanningJob.class);
  private static final String[] RPM_EXTENSIONS = new String[] { "rpm" };

  private final RepositoryRpmManager repositoryRpmManager;
  private final MavenRepositoryInfo mavenRepositoryInfo;

  public RepositoryScanningJob(RepositoryRpmManager repositoryRpmManager, MavenRepositoryInfo repositoryInfo) {
    this.repositoryRpmManager = repositoryRpmManager;
    this.mavenRepositoryInfo = repositoryInfo;
  }

  public void run() {
    LOG.info("Start new RepositoryScanningJob for repository : {}", mavenRepositoryInfo.getRepository().getId());
    scanRepository(mavenRepositoryInfo);
    repositoryRpmManager.updateRepository(mavenRepositoryInfo);
    LOG.info("Scanning for repository {} done.", mavenRepositoryInfo.getRepository().getId());
  }

  @SuppressWarnings("unchecked")
  private void scanRepository(MavenRepositoryInfo repositoryInfo) {
    try {
      LOG.info("Start scanning of repository base url : {}", repositoryInfo.getRepository().getLocalUrl());

      File repositoryBaseDir = repositoryInfo.getBaseDir();
      for (File file : (Collection<File>) listFiles(repositoryBaseDir, RPM_EXTENSIONS, true)) {
        repositoryInfo.addVersion(file.getParentFile().getName());
      }

      LOG.info("Found following versions in repository '{}' : {}", repositoryInfo.getId(),
        repositoryInfo.getVersions());

    } catch (Exception e) {
      LOG.error("Could not scan repository " + repositoryInfo.getId(), e);
    }
  }
}
