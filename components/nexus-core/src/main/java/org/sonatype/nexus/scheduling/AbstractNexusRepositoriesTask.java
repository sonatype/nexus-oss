/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.scheduling;

import javax.inject.Inject;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

import org.codehaus.plexus.util.StringUtils;

import static com.google.common.base.Preconditions.checkNotNull;

@Deprecated
public abstract class AbstractNexusRepositoriesTask<T>
    extends AbstractNexusTask<T>
{

  private RepositoryRegistry repositoryRegistry;

  @Inject
  public void setRepositoryRegistry(final RepositoryRegistry repositoryRegistry) {
    this.repositoryRegistry = checkNotNull(repositoryRegistry);
  }

  protected RepositoryRegistry getRepositoryRegistry() {
    return repositoryRegistry;
  }

  // This is simply a default to help for old api tasks
  // This method SHOULD be overridden in new task impls
  protected String getRepositoryFieldId() {
    return "repositoryId";
  }

  public String getRepositoryId() {
    final String id = getParameters().get(getRepositoryFieldId());
    if ("all_repo".equals(id)) {
      return null;
    }
    return id;
  }

  public void setRepositoryId(String repositoryId) {
    if (!StringUtils.isEmpty(repositoryId)) {
      getParameters().put(getRepositoryFieldId(), repositoryId);
    }
  }

  public String getRepositoryName() {
    try {
      Repository repo = getRepositoryRegistry().getRepository(getRepositoryId());

      return repo.getName();
    }
    catch (NoSuchRepositoryException e) {
      this.getLogger().warn("Could not read repository!", e);

      return getRepositoryId();
    }
  }

}
