package de.is24.nexus.yum.repository.config;

import java.io.File;

import org.apache.commons.lang.StringUtils;


public class DefaultYumGeneratorConfiguration implements YumGeneratorConfiguration {
  private final File rpmDir;
  private final String rpmUrl;
  private final File repoDir;
  private final String repoUrl;
  private final String id;
  private final String version;
  private final File cacheDir;
  private final String addedFile;
  private final boolean singleRpmPerDirectory;

  public DefaultYumGeneratorConfiguration(File rpmDir, String rpmUrl,
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

  public DefaultYumGeneratorConfiguration(File rpmDir, String rpmUrl,
    File repoDir, String id,
    File cacheDir) {
    this(rpmDir, rpmUrl, repoDir, rpmUrl, id, null, cacheDir, null, true);
  }

	@Override
	public boolean conflictsWith(YumGeneratorConfiguration config) {
		if (StringUtils.equals(getId(), config.getId())) {
			return StringUtils.equals(getVersion(), config.getVersion());
		}
		return false;
	}

  @Override
	public File getBaseRpmDir() {
    return rpmDir;
  }

  @Override
	public String getBaseRpmUrl() {
    return rpmUrl;
  }

  @Override
	public File getBaseRepoDir() {
    return repoDir;
  }

  @Override
	public String getBaseRepoUrl() {
    return repoUrl;
  }

  @Override
	public String getId() {
    return id;
  }

  @Override
	public String getVersion() {
    return version;
  }

  @Override
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

  @Override
	public String getAddedFile() {
    return addedFile;
  }

  @Override
	public boolean isSingleRpmPerDirectory() {
    return singleRpmPerDirectory;
  }

}
