package de.is24.nexus.yum.repository;

import java.io.File;
import org.apache.commons.lang.StringUtils;


public class YumDefaultGeneratorConfiguration implements YumGeneratorConfiguration {
  private final File rpmDir;
  private final String rpmUrl;
  private final File repoDir;
  private final String repoUrl;
  private final String id;
  private final String version;
  private final File cacheDir;
  private final String addedFile;
  private final boolean singleRpmPerDirectory;

  public YumDefaultGeneratorConfiguration(File rpmDir, String rpmUrl,
    File repoDir, String repoUrl, String id, String version,
    File cacheDir, String addedFile, boolean singleRpmPerDirectory) {
    this.rpmDir = assertFile(rpmDir);
    this.rpmUrl = assertNotBlank(rpmUrl);
    this.repoDir = assertFile(repoDir);
    this.repoUrl = assertNotBlank(repoUrl);
    this.id = assertNotBlank(id);
    this.version = version;
    this.cacheDir = assertFile(cacheDir);
    this.addedFile = addedFile;
    this.singleRpmPerDirectory = singleRpmPerDirectory;
  }

  public YumDefaultGeneratorConfiguration(File rpmDir, String rpmUrl,
    File repoDir, String id,
    File cacheDir) {
    this(rpmDir, rpmUrl, repoDir, rpmUrl, id, null, cacheDir, null, true);
  }

  public File getBaseRpmDir() {
    return rpmDir;
  }

  public String getBaseRpmUrl() {
    return rpmUrl;
  }

  public File getBaseRepoDir() {
    return repoDir;
  }

  public String getBaseRepoUrl() {
    return repoUrl;
  }

  public String getId() {
    return id;
  }

  public String getVersion() {
    return version;
  }

  public File getBaseCacheDir() {
    return cacheDir;
  }

  private static String assertNotBlank(String string) {
    if (StringUtils.isBlank(string)) {
      throw new IllegalArgumentException("String is not allowed to be blank");
    }

    return string;
  }


  private static File assertFile(File file) {
    if (file == null) {
      throw new IllegalArgumentException("File is not allowed to be null");
    }
    return file;
  }

  public String getAddedFile() {
    return addedFile;
  }

  public boolean isSingleRpmPerDirectory() {
    return singleRpmPerDirectory;
  }

}
