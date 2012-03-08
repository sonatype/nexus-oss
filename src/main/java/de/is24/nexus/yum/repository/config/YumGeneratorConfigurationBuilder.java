package de.is24.nexus.yum.repository.config;

import java.io.File;


public class YumGeneratorConfigurationBuilder {
  private File rpmDir;
  private String rpmUrl;
  private File repoDir;
  private String repoUrl;
  private String id;
  private String version;
  private File cacheDir;
  private String addedFile;
  private boolean singleRpmPerDirectory;

  public YumGeneratorConfigurationBuilder rpmDir(File rpmDir) {
    this.rpmDir = rpmDir;
    if (repoDir == null) {
      repoDir = rpmDir;
    }
    return this;
  }

  public YumGeneratorConfigurationBuilder rpmUrl(String rpmUrl) {
    this.rpmUrl = rpmUrl;
    if (repoUrl == null) {
      repoUrl = rpmUrl;
    }
    return this;
  }

  public YumGeneratorConfigurationBuilder repoDir(File repoDir) {
    this.repoDir = repoDir;
    return this;
  }

  public YumGeneratorConfigurationBuilder repoUrl(String repoUrl) {
    this.repoUrl = repoUrl;
    if (rpmUrl == null) {
      rpmUrl = repoUrl;
    }
    return this;
  }

  public YumGeneratorConfigurationBuilder id(String id) {
    this.id = id;
    return this;
  }

  public YumGeneratorConfigurationBuilder version(String version) {
    this.version = version;
    return this;
  }

  public YumGeneratorConfigurationBuilder cacheDir(File cacheDir) {
    this.cacheDir = cacheDir;
    return this;
  }

  public YumGeneratorConfigurationBuilder singleRpmPerDirectory(boolean singleRpmPerDirectory) {
    this.singleRpmPerDirectory = singleRpmPerDirectory;
    return this;
  }

  public YumGeneratorConfiguration toConfig() {
    return new DefaultYumGeneratorConfiguration(rpmDir, rpmUrl,
      repoDir, repoUrl, id, version,
      cacheDir, addedFile, singleRpmPerDirectory);
  }

  public static YumGeneratorConfigurationBuilder newConfigBuilder() {
    return new YumGeneratorConfigurationBuilder();
  }

  public YumGeneratorConfigurationBuilder addedFile(String filePath) {
    this.addedFile = filePath;
    return this;
  }

}
