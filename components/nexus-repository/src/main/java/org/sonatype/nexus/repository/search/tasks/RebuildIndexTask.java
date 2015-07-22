/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.search.tasks;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.search.SearchFacet;
import org.sonatype.nexus.scheduling.TaskSupport;

import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Internal task to rebuild index of given repository.
 *
 * @since 3.0
 */
@Named
public class RebuildIndexTask
    extends TaskSupport
{
  public static final String REPOSITORY_NAME_FIELD_ID = "repositoryName";

  private final RepositoryManager repositoryManager;

  @Inject
  public RebuildIndexTask(final RepositoryManager repositoryManager) {
    this.repositoryManager = checkNotNull(repositoryManager);
  }

  @Override
  protected Object execute() throws Exception {
    final Repository repository = getRepository();
    repository.facet(SearchFacet.class).rebuildIndex();
    return null;
  }

  @Override
  public String getMessage() {
    return "Rebuilding index of " + getRepository();
  }

  @Nonnull
  private Repository getRepository() {
    final String repositoryName = getConfiguration().getString(REPOSITORY_NAME_FIELD_ID);
    checkArgument(!Strings.isNullOrEmpty(repositoryName));
    Repository repository = repositoryManager.get(repositoryName);
    checkNotNull(repository);
    return repository;
  }
}
