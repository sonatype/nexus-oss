/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.updatesite;

import java.util.List;

import javax.inject.Named;

import org.sonatype.nexus.plugins.p2.repository.UpdateSiteProxyRepository;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.TaskScheduler;
import org.sonatype.nexus.proxy.repository.RepositoryTaskSupport;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;

import com.google.common.collect.Lists;

/**
 * Update Site mirror task.
 */
@Named
public class UpdateSiteMirrorTask
    extends RepositoryTaskSupport<Void>
{
  public static TaskInfo<Void> submit(final TaskScheduler scheduler,
                                      final UpdateSiteProxyRepository updateSite,
                                      final boolean force)
  {
    final TaskConfiguration task = scheduler.createTaskConfigurationInstance(UpdateSiteMirrorTask.class);
    task.setRepositoryId(updateSite.getId());
    task.setBoolean(UpdateSiteMirrorTaskDescriptor.FORCE_MIRROR_FIELD_ID, force);
    task.setName("Eclipse Update Site Mirror (" + updateSite.getId() + ")");
    return scheduler.submit(task);
  }

  @Override
  protected Void execute()
      throws Exception
  {
    List<UpdateSiteProxyRepository> repos = getRepositories();
    for (UpdateSiteProxyRepository updateSite : repos) {
      updateSite.mirror(getForce());
    }

    return null;
  }

  private List<UpdateSiteProxyRepository> getRepositories()
      throws NoSuchRepositoryException
  {
    if (getConfiguration().getRepositoryId() != null) {
      Repository repo = getRepositoryRegistry().getRepository(getConfiguration().getRepositoryId());
      if (repo.getRepositoryKind().isFacetAvailable(UpdateSiteProxyRepository.class)) {
        return Lists.newArrayList(repo.adaptToFacet(UpdateSiteProxyRepository.class));
      }
      else if (repo.getRepositoryKind().isFacetAvailable(GroupRepository.class)) {
        return updateSites(repo.adaptToFacet(GroupRepository.class));
      }
      else {
        throw new IllegalStateException("Update site mirror task only applicable to Eclipse Update Sites");
      }
    }

    return getRepositoryRegistry().getRepositoriesWithFacet(UpdateSiteProxyRepository.class);
  }

  private List<UpdateSiteProxyRepository> updateSites(GroupRepository group) {
    List<UpdateSiteProxyRepository> us = Lists.newArrayList();

    for (Repository repo : group.getMemberRepositories()) {
      if (repo.getRepositoryKind().isFacetAvailable(UpdateSiteProxyRepository.class)) {
        us.add(repo.adaptToFacet(UpdateSiteProxyRepository.class));
      }
    }

    if (us.isEmpty()) {
      log.warn(
          "Group '{}' has no UpdateSite repositories members. Update site mirror task will be silent skipped!",
          group.getId());
    }

    return us;
  }

  @Override
  public String getMessage() {
    if (getConfiguration().getRepositoryId() == null) {
      return "Mirroring content of All Eclipse Update Sites.";
    }
    Repository repo;
    try {
      repo = getRepositoryRegistry().getRepository(getConfiguration().getRepositoryId());
    }
    catch (NoSuchRepositoryException e) {
      return "Repository not found";
    }

    if (repo.getRepositoryKind().isFacetAvailable(GroupRepository.class)) {
      return "Mirroring content of All Eclipse Update Sites in group ID='" + repo.getId() + "'.";
    }

    return "Mirroring content of Eclipse Update Site ID='" + repo.getId() + "'.";
  }

  public void setRepositoryId(final String repositoryId) {
    try {
      getRepositoryRegistry().getRepository(repositoryId);
    }
    catch (final NoSuchRepositoryException e) {
      throw new IllegalStateException(e);
    }
    getConfiguration().setRepositoryId(repositoryId);
  }

  public boolean getForce() {
    return getConfiguration().getBoolean(UpdateSiteMirrorTaskDescriptor.FORCE_MIRROR_FIELD_ID, false);
  }
}
