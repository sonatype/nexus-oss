package org.sonatype.nexus.client.core.subsystem.artifact;

import java.io.File;

public class UploadRequest {

  private final String repositoryId;
  private final boolean hasPom;
  private final String groupId;
  private final String artifactId;
  private final String version;
  private final String packaging;
  private final String classifier;
  private final String extension;
  private final File file;
  private final File pomFile;

  public UploadRequest(String repositoryId, String groupId, String artifactId, String version, String packaging, String classifier,
      String extension, File file) {
    this.repositoryId = repositoryId;
    this.hasPom = false;
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.packaging = packaging;
    this.classifier = classifier;
    this.extension = extension;
    this.file = file;
    this.pomFile = null;
  }

  public UploadRequest(String repositoryId, File pomFile, String classifier, String extension, File file) {
    this.repositoryId = repositoryId;
    this.hasPom = true;
    this.pomFile = pomFile;
    this.classifier = classifier;
    this.extension = extension;
    this.file = file;
    this.groupId = null;
    this.artifactId = null;
    this.version = null;
    this.packaging = null;
  }

  public String getRepositoryId() {
    return repositoryId;
  }

  public boolean isHasPom() {
    return hasPom;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public String getVersion() {
    return version;
  }

  public String getPackaging() {
    return packaging;
  }

  public String getClassifier() {
    return classifier;
  }

  public String getExtension() {
    return extension;
  }

  public File getFile() {
    return file;
  }

  public File getPomFile() {
    return pomFile;
  }
}
