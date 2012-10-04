/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
 package org.sonatype.nexus.plugins.yum.plugin.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.sonatype.nexus.plugins.yum.repository.RepositoryUtils;
import org.sonatype.nexus.proxy.maven.MavenRepository;

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
