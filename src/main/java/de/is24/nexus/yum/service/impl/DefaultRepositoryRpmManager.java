package de.is24.nexus.yum.service.impl;

import static java.lang.String.format;
import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.plugin.Managed;
import de.is24.nexus.yum.plugin.impl.MavenRepositoryInfo;
import de.is24.nexus.yum.repository.RepositoryRpmGenerator;
import de.is24.nexus.yum.repository.YumRepository;
import de.is24.nexus.yum.service.RepositoryRpmManager;
import de.is24.nexus.yum.service.YumService;


@Managed
@Named(RepositoryRpmManager.DEFAULT_BEAN_NAME)
@Singleton
public class DefaultRepositoryRpmManager implements RepositoryRpmManager {
  private static final Logger log = LoggerFactory.getLogger(DefaultRepositoryRpmManager.class);

  @Inject
  @Named(YumService.DEFAULT_BEAN_NAME)
  private YumService yumService;

  @Inject
  private GlobalRestApiSettings restApiSettings;

  private File rpmLocation;

  private Future<YumRepository> yumRepositoryFuture;

  public synchronized void updateRepository(MavenRepositoryInfo repositoryInfo) {
    boolean needUpdate = false;
    for (String version : repositoryInfo.getVersions()) {
      File rpmFile = new File(getRpmCacheDir(), getRpmFileName(repositoryInfo.getId(), version));
      if (!rpmFile.exists()) {
        generateRpmForRepository(repositoryInfo.getId(), version);
        needUpdate = true;
      }
    }

    if (needUpdate) {
      updateYumRepository();
    }
  }

  public File updateRepository(String repositoryId, String version) {
    File rpmFile = new File(getRpmCacheDir(), getRpmFileName(repositoryId, version));
    if (!rpmFile.exists()) {
      generateRpmForRepository(repositoryId, version);
      updateYumRepository();
    }

    return rpmFile;
  }

  private void updateYumRepository() {
    try {
      log.info("Updating global yum repository for repository RPMs...");

      URL repositoryUrl = new URL(restApiSettings.getBaseUrl() + "/service/local/" + URL_PREFIX);
      yumRepositoryFuture = yumService.createYumRepository(getRpmCacheDir(), repositoryUrl.toString(), getRpmCacheDir(),
        repositoryUrl,
        "nexus-yum-repos", false);
    } catch (Exception e) {
      log.error("Could not create nexus RPM repository", e);
    }
  }

  private void generateRpmForRepository(String repositoryId, String version) {
    try {
      log.info("Generating RPM for repository '{}' and version '{}'", repositoryId, version);
      new RepositoryRpmGenerator(restApiSettings.getBaseUrl(), repositoryId, version, getRpmCacheDir())
      .generateReleaseRpm();
    } catch (Exception e) {
      log.error("Could not create RPM for repository '" + repositoryId + "' and version '" + version + "'.", e);
    }
  }

  private String getRpmFileName(String repositoryId, String version) {
    return format("is24-rel-%s-%s-repo-1-1.noarch.rpm", repositoryId, version);
  }

  private File getRpmCacheDir() {
    if (rpmLocation == null) {
      rpmLocation = new File(yumService.getBaseTempDir(), ".repositoryRpms");
      rpmLocation.mkdirs();
    }

    return rpmLocation;
  }

  public YumRepository getYumRepository() throws InterruptedException, ExecutionException {
    if ((yumRepositoryFuture == null) || !yumRepositoryFuture.get().getBaseDir().exists()) {
      updateYumRepository();
    }

    return yumRepositoryFuture.get();
  }

}
