package de.is24.nexus.yum.repository;

import static de.is24.nexus.yum.execution.ExecutionUtil.execCommand;
import static de.is24.nexus.yum.repository.YumRepository.REPOMD_XML;
import static de.is24.nexus.yum.repository.YumRepository.YUM_REPOSITORY_DIR_NAME;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Create a yum-repository directory via 'createrepo' command line tool.
 *
 * @author sherold
 *
 */
public class YumRepositoryGeneratorJob implements Callable<YumRepository>, ListFileFactory {
  private static final String PACKAGE_FILE_DIR_NAME = ".packageFiles";
  private static final Logger LOG = LoggerFactory.getLogger(YumRepositoryGeneratorJob.class);

  public static boolean activated = true;

  private final YumGeneratorConfiguration config;

  public YumRepositoryGeneratorJob(YumGeneratorConfiguration config) {
    this.config = config;
  }

  public String getRepositoryId() {
    return config.getId();
  }

  public YumRepository call() throws Exception {
    if (activated) {
      LOG.info("Generating Yum-Repository for '{}' ...", config.getBaseRpmDir());
      try {
        config.getBaseRepoDir().mkdirs();

        File rpmListFile = createRpmListFile();
        execCommand(buildCreateRepositoryCommand(rpmListFile));

        replaceUrl();
      } catch (IOException e) {
        LOG.warn("Generating Yum-Repo failed", e);
        throw new IOException("Generating Yum-Repo failed", e);
      }
      Thread.sleep(100);
      LOG.info("Generation complete.");
      return new YumRepository(config.getBaseRepoDir(), config.getId(), config.getVersion());
    }

    return null;
  }

  private File createRpmListFile() throws IOException {
    return new RpmListWriter(config, this).writeList();
  }

  private File createCacheDir() {
    File cacheDir = new File(config.getBaseCacheDir(), getRepositoryIdVersion());
    cacheDir.mkdirs();
    return cacheDir;
  }

  private String getRepositoryIdVersion() {
    return config.getId() +
      (isNotBlank(config.getVersion()) ? ("-version-" + config.getVersion()) : "");
  }

  private void replaceUrl() throws IOException {
    File repomd = new File(config.getBaseRepoDir(), YUM_REPOSITORY_DIR_NAME + File.separator + REPOMD_XML);
    if (activated && repomd.exists()) {
      String repomdStr = FileUtils.readFileToString(repomd);
      repomdStr = repomdStr.replace(config.getBaseRpmUrl(), config.getBaseRepoUrl());
      FileUtils.writeStringToFile(repomd, repomdStr);
    }
  }

  private String buildCreateRepositoryCommand(File packageList) {
    String baseRepoDir = config.getBaseRepoDir().getAbsolutePath();
    String baseRpmUrl = config.getBaseRpmUrl();
    String packageFile = packageList.getAbsolutePath();
    String cacheDir = createCacheDir().getAbsolutePath();
    String baseRpmDir = config.getBaseRpmDir().getAbsolutePath();
    return String.format("createrepo --update -o %s -u %s  -v -d -i %s -c %s %s", baseRepoDir, baseRpmUrl, packageFile,
      cacheDir,
      baseRpmDir);
  }

  public static void deactivate() {
    activated = false;
  }

  public static void activate() {
    activated = true;
  }

  public File getRpmListFile(String repositoryId) {
    return new File(createBasePackageDir(), config.getId() + ".txt");
  }

  private File createBasePackageDir() {
    File basePackageDir = new File(config.getBaseCacheDir(), PACKAGE_FILE_DIR_NAME);
    basePackageDir.mkdirs();
    return basePackageDir;
  }

  public File getRpmListFile(String repositoryId, String version) {
    return new File(createBasePackageDir(), config.getId() + "-" + version + ".txt");
  }

  public static boolean isActive() {
    return activated;
  }

}
