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

package org.sonatype.nexus.repository.manager;

import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Recipe;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.config.ConfigurationFacet;
import org.sonatype.nexus.repository.config.ConfigurationStore;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Default {@link RepositoryManager} implementation.
 *
 * @since 3.0
 */
@Named
@Singleton
public class RepositoryManagerImpl
    extends LifecycleSupport
    implements RepositoryManager
{
  private final EventBus eventBus;

  private final ConfigurationStore store;

  private final Map<String,Recipe> recipes;

  private final Map<String, Repository> repositories = Maps.newHashMap();

  @Inject
  public RepositoryManagerImpl(final EventBus eventBus,
                               final ConfigurationStore store,
                               final Map<String,Recipe> recipes)
  {
    this.eventBus = checkNotNull(eventBus);
    this.store = checkNotNull(store);
    this.recipes = checkNotNull(recipes);
  }

  private Recipe recipe(final String name) {
    Recipe recipe = recipes.get(name);
    checkState(recipe != null, "Missing recipe: %s", name);
    return recipe;
  }

  private Repository repository(final String name) {
    Repository repository = repositories.get(name);
    checkState(repository != null, "Missing repository: %s", name);
    return repository;
  }

  private void track(final Repository repository) {
    repositories.put(repository.getName(), repository);
  }

  // TODO: Generally need to consider exception handling to ensure proper state is maintained always

  @Override
  protected void doStart() throws Exception {
    log.debug("Restoring repositories");
    for (Configuration configuration : store.browse()) {
      log.debug("Restoring repository: {}", configuration);
      Recipe recipe = recipe(configuration.getRecipeName());
      Repository repository = recipe.create(configuration);
      track(repository);
    }

    log.debug("Starting repositories");
    for (Repository repository : repositories.values()) {
      repository.start();

      eventBus.post(new RepositoryRestoredEvent(repository));
    }
  }

  @Override
  protected void doStop() throws Exception {
    log.debug("Stopping repositories");
    for (Repository repository : repositories.values()) {
      repository.stop();
    }

    log.debug("Disposing repositories");
    for (Repository repository : repositories.values()) {
      repository.dispose();
    }

    repositories.clear();
  }

  @Override
  public Iterable<Repository> browse() {
    ensureStarted();

    return ImmutableList.copyOf(repositories.values());
  }

  @Nullable
  @Override
  public Repository read(final String name) {
    checkNotNull(name);
    ensureStarted();

    return repositories.get(name);
  }

  @Override
  public Repository create(final Configuration configuration) throws Exception {
    checkNotNull(configuration);
    ensureStarted();

    log.debug("Creating repository: {}", configuration);
    Recipe recipe = recipe(configuration.getRecipeName());
    Repository repository = recipe.create(configuration);

    store.create(configuration);
    track(repository);
    repository.start();

    eventBus.post(new RepositoryCreatedEvent(repository));

    return repository;
  }

  @Override
  public Repository update(final Configuration configuration) throws Exception {
    checkNotNull(configuration);
    ensureStarted();

    log.debug("Updating repository: {}", configuration);
    Repository repository = repository(configuration.getRepositoryName());
    repository.stop();
    repository.facet(ConfigurationFacet.class).update(configuration);
    repository.start();

    return repository;
  }

  @Override
  public void delete(final String name) throws Exception {
    checkNotNull(name);
    ensureStarted();

    log.debug("Deleting repository: {}", name);
    Repository repository = repository(name);
    repository.stop();
    repository.dispose();
    store.delete(name);
    repositories.remove(name);

    eventBus.post(new RepositoryDeletedEvent(name));
  }
}
