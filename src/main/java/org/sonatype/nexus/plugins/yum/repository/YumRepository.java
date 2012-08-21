package org.sonatype.nexus.plugins.yum.repository;

import java.io.File;


public class YumRepository implements FileDirectoryStructure {
  private final File yumRepoBaseDir;
  private boolean dirty = false;
  private final String version;
  private final String id;

  public YumRepository(File yumRepoBaseDir, String id, String version) {
    this.yumRepoBaseDir = yumRepoBaseDir;
    this.id = id;
    this.version = version;
  }

  public static final String YUM_REPOSITORY_DIR_NAME = "repodata";
  public static final String REPOMD_XML = "repomd.xml";

  public File getBaseDir() {
    return yumRepoBaseDir;
  }

  public File getFile(String path) {
    return (path == null) ? yumRepoBaseDir : new File(yumRepoBaseDir, path);
  }

  public boolean isDirty() {
    return dirty;
  }

  public void setDirty() {
    this.dirty = true;
  }

  public String getVersion() {
    return version;
  }

  public String getId() {
    return id;
  }

}
