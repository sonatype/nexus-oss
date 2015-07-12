/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.repository;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.testsuite.NexusHttpsITSupport;
import org.junit.After;

/**
 * Support class for repository format ITs.
 */
public abstract class RepositoryTestSupport
    extends NexusHttpsITSupport
{
  private List<Repository> repositories = new ArrayList<>();

  @Inject
  private RepositoryManager repositoryManager;

  /**
   * Creates a repository, first removing an existing one if necessary.
   */
  protected Repository createRepository(final Configuration config) throws Exception {
    log.debug("Waiting for Nexus");
    waitFor(responseFrom(nexusUrl));
    log.info("Creating repository {}", config.getRepositoryName());
    final Repository repository = repositoryManager.create(config);
    repositories.add(repository);
    calmPeriod();
    return repository;
  }

  public void deleteRepository(Repository repository) throws Exception {
    repositories.remove(repository);
    log.info("Deleting repository {}", repository.getName());
    repositoryManager.delete(repository.getName());
    calmPeriod();
  }

  @After
  public void deleteRepositories() throws Exception {
    for (Repository repository : repositories) {
      log.info("Deleting repository {}", repository.getName());
      repositoryManager.delete(repository.getName());
    }
    calmPeriod();
  }

  @Nonnull
  protected URL repositoryBaseUrl(final Repository repository) {
    return resolveUrl(nexusUrl, "/repository/" + repository.getName() + "/");
  }
}
