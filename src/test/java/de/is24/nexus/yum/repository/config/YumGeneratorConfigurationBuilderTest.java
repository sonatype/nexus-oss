package de.is24.nexus.yum.repository.config;

import static de.is24.nexus.yum.repository.config.YumGeneratorConfigurationBuilder.newConfigBuilder;
import static junit.framework.Assert.assertEquals;
import java.io.File;
import org.junit.Test;

import de.is24.nexus.yum.repository.config.YumGeneratorConfiguration;


public class YumGeneratorConfigurationBuilderTest {
  private static final String REPO_URL = "repo://";
  private static final String RPM_URL = "rpm://";
  private static final String VERSION = "version";
  private static final String ID = "id";
  private static final File CACHE_DIR = new File("cache");
  private static final File REPO_DIR = new File("repo");
  private static final File RPMS = new File("rpms");

  @Test
  public void shouldCreateConfig() throws Exception {
    YumGeneratorConfiguration config = newConfigBuilder().cacheDir(CACHE_DIR).id(ID).repoDir(REPO_DIR).rpmDir(RPMS)
      .version(VERSION).rpmUrl(RPM_URL).repoUrl(REPO_URL).toConfig();
    assertEquals(CACHE_DIR, config.getBaseCacheDir());
    assertEquals(ID, config.getId());
    assertEquals(REPO_DIR, config.getBaseRepoDir());
    assertEquals(RPMS, config.getBaseRpmDir());
    assertEquals(VERSION, config.getVersion());
    assertEquals(REPO_URL, config.getBaseRepoUrl());
    assertEquals(RPM_URL, config.getBaseRpmUrl());
  }

  @Test
  public void shouldCreateConfig2() throws Exception {
    YumGeneratorConfiguration config = newConfigBuilder().cacheDir(CACHE_DIR).id(ID).repoDir(REPO_DIR).rpmDir(RPMS)
      .version(VERSION).repoUrl(REPO_URL).rpmUrl(RPM_URL).toConfig();
    assertEquals(CACHE_DIR, config.getBaseCacheDir());
    assertEquals(ID, config.getId());
    assertEquals(REPO_DIR, config.getBaseRepoDir());
    assertEquals(RPMS, config.getBaseRpmDir());
    assertEquals(VERSION, config.getVersion());
    assertEquals(REPO_URL, config.getBaseRepoUrl());
    assertEquals(RPM_URL, config.getBaseRpmUrl());
  }

  @Test
  public void shouldAutoSetConfig() throws Exception {
    YumGeneratorConfiguration config = newConfigBuilder().cacheDir(CACHE_DIR).id(ID).rpmDir(RPMS).version(VERSION)
      .rpmUrl(
        RPM_URL).toConfig();
    assertEquals(CACHE_DIR, config.getBaseCacheDir());
    assertEquals(ID, config.getId());
    assertEquals(RPMS, config.getBaseRepoDir());
    assertEquals(RPMS, config.getBaseRpmDir());
    assertEquals(VERSION, config.getVersion());
    assertEquals(RPM_URL, config.getBaseRepoUrl());
    assertEquals(RPM_URL, config.getBaseRpmUrl());
  }

  @Test
  public void shouldAutoSetRpmUrl() throws Exception {
    YumGeneratorConfiguration config = newConfigBuilder().cacheDir(CACHE_DIR).id(ID).rpmDir(RPMS).version(VERSION)
      .repoUrl(REPO_URL).toConfig();
    assertEquals(CACHE_DIR, config.getBaseCacheDir());
    assertEquals(ID, config.getId());
    assertEquals(RPMS, config.getBaseRepoDir());
    assertEquals(RPMS, config.getBaseRpmDir());
    assertEquals(VERSION, config.getVersion());
    assertEquals(REPO_URL, config.getBaseRepoUrl());
    assertEquals(REPO_URL, config.getBaseRpmUrl());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionForNoFile() {
    newConfigBuilder().id(ID).rpmDir(RPMS).version(VERSION).repoUrl(REPO_URL).toConfig();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionForNoId() {
    newConfigBuilder().id(" ").rpmDir(RPMS).version(VERSION).repoUrl(REPO_URL).cacheDir(CACHE_DIR).toConfig();
  }
}
