/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.proxy.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventPostRemove;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryStatusCheckerThread;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository registry. It holds handles to registered repositories and sorts them properly. This class is used to get
 * a
 * grip on repositories.
 * <p>
 * Getting reposes from here and changing repo attributes like group, id and rank have no effect on repo registry! For
 * that kind of change, you have to: 1) get repository, 2) remove repository from registry, 3) change repo attributes
 * and 4) add repository.
 * <p>
 * ProximityEvents: this component just "concentrates" the repositiry events of all known repositories by it. It can be
 * used as single point to access all repository events. TODO this is not a good place to keep group repository
 * management code
 *
 * @author cstamas
 */
@Component(role = RepositoryRegistry.class)
public class DefaultRepositoryRegistry
    implements RepositoryRegistry, Disposable
{
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Requirement
  private EventBus eventBus;

  @Requirement
  private RepositoryTypeRegistry repositoryTypeRegistry;

  public void addRepository(final Repository repository) {
    final RepositoryTypeDescriptor rtd =
        repositoryTypeRegistry.getRepositoryTypeDescriptor(repository.getProviderRole(),
            repository.getProviderHint());

    insertRepository(rtd, repository);

    logger.info("Added repository {}", RepositoryStringUtils.getFullHumanizedNameString(repository));
  }

  public void removeRepository(final String repoId)
      throws NoSuchRepositoryException
  {
    doRemoveRepository(repoId, false);
  }

  public void removeRepositorySilently(final String repoId)
      throws NoSuchRepositoryException
  {
    doRemoveRepository(repoId, true);
  }

  public List<Repository> getRepositories() {
    return Collections.unmodifiableList(new ArrayList<Repository>(getRepositoriesMap().values()));
  }

  public <T> List<T> getRepositoriesWithFacet(final Class<T> f) {
    final List<Repository> repositories = getRepositories();

    final ArrayList<T> result = new ArrayList<T>();

    for (Repository repository : repositories) {
      if (repository.getRepositoryKind().isFacetAvailable(f)) {
        result.add(repository.adaptToFacet(f));
      }
    }

    return Collections.unmodifiableList(result);
  }

  public Repository getRepository(final String repoId)
      throws NoSuchRepositoryException
  {
    final Map<String, Repository> repositories = getRepositoriesMap();

    if (repositories.containsKey(repoId)) {
      return repositories.get(repoId);
    }
    else {
      throw new NoSuchRepositoryException(repoId);
    }
  }

  public <T> T getRepositoryWithFacet(final String repoId, final Class<T> f)
      throws NoSuchRepositoryException
  {
    final Repository r = getRepository(repoId);

    if (r.getRepositoryKind().isFacetAvailable(f)) {
      return r.adaptToFacet(f);
    }
    else {
      throw new NoSuchRepositoryException(repoId);
    }
  }

  public boolean repositoryIdExists(final String repositoryId) {
    return getRepositoriesMap().containsKey(repositoryId);
  }

  public List<String> getGroupsOfRepository(final String repositoryId) {
    final ArrayList<String> result = new ArrayList<String>();

    try {
      final Repository repository = getRepository(repositoryId);

      for (GroupRepository group : getGroupsOfRepository(repository)) {
        result.add(group.getId());
      }
    }
    catch (NoSuchRepositoryException e) {
      // ignore, just return empty collection
    }

    return result;
  }

  public List<GroupRepository> getGroupsOfRepository(final Repository repository) {
    final ArrayList<GroupRepository> result = new ArrayList<GroupRepository>();

    for (Repository repo : getRepositories()) {
      if (!repo.getId().equals(repository.getId())
          && repo.getRepositoryKind().isFacetAvailable(GroupRepository.class)) {
        final GroupRepository group = repo.adaptToFacet(GroupRepository.class);

        members:
        for (Repository member : group.getMemberRepositories()) {
          if (repository.getId().equals(member.getId())) {
            result.add(group);
            break members;
          }
        }
      }
    }

    return result;
  }

  // Disposable plexus iface

  public void dispose() {
    // kill the checker daemon threads
    for (Repository repository : getRepositoriesMap().values()) {
      killMonitorThread(repository.adaptToFacet(ProxyRepository.class));
    }
  }

  //
  // priv
  //

  /**
   * The repository registry map
   */
  private final Map<String, Repository> _repositories = new HashMap<String, Repository>();

  /**
   * The repository registry RO "view"
   */
  private volatile Map<String, Repository> _repositoriesView;

  /**
   * Returns a copy of map with repositories. Is synchronized method, to allow consistent-read access. Methods
   * modifying this map are all also synchronized (see API Interface and above), while all the "reading" methods from
   * public API will boil down to this single method.
   */
  protected synchronized Map<String, Repository> getRepositoriesMap() {
    if (_repositoriesView == null) {
      _repositoriesView = Collections.unmodifiableMap(new HashMap<String, Repository>(_repositories));
    }

    return _repositoriesView;
  }

  protected synchronized void repositoriesMapPut(final Repository repository) {
    _repositories.put(repository.getId(), repository);
    _repositoriesView = Collections.unmodifiableMap(new HashMap<String, Repository>(_repositories));
  }

  protected synchronized void repositoriesMapRemove(final String repositoryId) {
    _repositories.remove(repositoryId);
    _repositoriesView = Collections.unmodifiableMap(new HashMap<String, Repository>(_repositories));
  }

  protected void doRemoveRepository(final String repoId, final boolean silently)
      throws NoSuchRepositoryException
  {
    Repository repository = getRepository(repoId);

    RepositoryTypeDescriptor rtd =
        repositoryTypeRegistry.getRepositoryTypeDescriptor(repository.getProviderRole(),
            repository.getProviderHint());

    deleteRepository(rtd, repository, silently);

    if (!silently) {
      logger.info("Removed repository {}", RepositoryStringUtils.getFullHumanizedNameString(repository));
    }
  }

  private void insertRepository(final RepositoryTypeDescriptor rtd, final Repository repository) {
    synchronized (this) {
      repositoriesMapPut(repository);

      rtd.instanceRegistered(this);

      if (repository.getRepositoryKind().isFacetAvailable(ProxyRepository.class)) {
        final ProxyRepository proxy = repository.adaptToFacet(ProxyRepository.class);

        killMonitorThread(proxy);

        RepositoryStatusCheckerThread thread =
            new RepositoryStatusCheckerThread(LoggerFactory.getLogger(getClass().getName() + "-"
                + repository.getId()), (ProxyRepository) repository);

        proxy.setRepositoryStatusCheckerThread(thread);

        thread.setRunning(true);

        thread.setDaemon(true);

        thread.start();
      }
    }

    eventBus.post(new RepositoryRegistryEventAdd(this, repository));
  }

  private void deleteRepository(final RepositoryTypeDescriptor rtd, final Repository repository,
                                final boolean silently)
  {
    if (!silently) {
      eventBus.post(new RepositoryRegistryEventRemove(this, repository));
    }

    // dump the event listeners, as once deleted doesn't care about config changes any longer
    if (repository instanceof AbstractConfigurable) {
      ((AbstractConfigurable) repository).unregisterFromEventBus();
    }

    synchronized (this) {
      rtd.instanceUnregistered(this);

      repositoriesMapRemove(repository.getId());

      killMonitorThread(repository.adaptToFacet(ProxyRepository.class));
    }

    if (!silently) {
      eventBus.post(new RepositoryRegistryEventPostRemove(this, repository));
    }
  }

  // ==

  protected void killMonitorThread(final ProxyRepository proxy) {
    if (null == proxy) {
      return;
    }

    if (null != proxy.getRepositoryStatusCheckerThread()) {
      RepositoryStatusCheckerThread thread =
          (RepositoryStatusCheckerThread) proxy.getRepositoryStatusCheckerThread();

      thread.setRunning(false);

      // and now interrupt it to die
      thread.interrupt();
    }
  }
}
