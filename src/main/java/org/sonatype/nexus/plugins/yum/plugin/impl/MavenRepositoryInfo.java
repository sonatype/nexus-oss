package org.sonatype.nexus.plugins.yum.plugin.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.sonatype.nexus.plugins.yum.repository.RepositoryUtils;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.plugins.yum.repository.RepositoryUtils;


public class MavenRepositoryInfo {
  private final MavenRepository repository;
  private final Set<String> versions = new HashSet<String>();

  public MavenRepositoryInfo(MavenRepository repository) {
    this.repository = repository;
  }

  public MavenRepository getRepository() {
    return repository;
  }

  public File getBaseDir() throws MalformedURLException, URISyntaxException {
    return RepositoryUtils.getBaseDir(repository);
  }

  public void addVersion(String version) {
    versions.add(version);
  }

  public String getId() {
    return repository.getId();
  }

  public Set<String> getVersions() {
    return versions;
  }

}
