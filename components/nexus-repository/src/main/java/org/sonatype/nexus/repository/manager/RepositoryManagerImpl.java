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

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Recipe;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.RepositoryFactory;
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

  private final RepositoryFactory factory;

  private final Map<String, Repository> repositories = Maps.newHashMap();

  @Inject
  public RepositoryManagerImpl(final EventBus eventBus,
                               final ConfigurationStore store,
                               final RepositoryFactory factory,
                               final Map<String,Recipe> recipes)
  {
    this.eventBus = checkNotNull(eventBus);
    this.store = checkNotNull(store);
    this.factory = checkNotNull(factory);
    this.recipes = checkNotNull(recipes);
  }

  /**
   * Lookup a recipe by name.
   */
  private Recipe recipe(final String name) {
    Recipe recipe = recipes.get(name);
    checkState(recipe != null, "Missing recipe: %s", name);
    return recipe;
  }

  /**
   * Lookup a repository by name.
   */
  private Repository repository(final String name) {
    Repository repository = repositories.get(name);
    checkState(repository != null, "Missing repository: %s", name);
    return repository;
  }

  /**
   * Construct a new repository from configuration.
   */
  private Repository newRepository(final Configuration configuration) throws Exception {
    String recipeName = configuration.getRecipeName();
    Recipe recipe = recipe(recipeName);
    log.debug("Using recipe: [{}] {}", recipeName, recipe);
    Repository repository = factory.create(recipe.getType(), recipe.getFormat());
    repository.init(configuration);
    // TODO: mandatory facets/recipe?
    recipe.apply(repository);
    return repository;
  }

  /**
   * Mark repository as tracked.
   */
  private void track(final Repository repository) {
    repositories.put(repository.getName(), repository);
  }

  // TODO: Generally need to consider exception handling to ensure proper state is maintained always

  @Override
  protected void doStart() throws Exception {
    List<Configuration> configurations = store.list();
    if (configurations.isEmpty()) {
      log.debug("No repositories configured");
      return;
    }

    log.debug("Restoring {} repositories", configurations.size());
    for (Configuration configuration : configurations) {
      log.debug("Restoring repository: {}", configuration);
      Repository repository = newRepository(configuration);
      track(repository);

      eventBus.post(new RepositoryLoadedEvent(repository));
    }

    log.debug("Starting {} repositories", repositories.size());
    for (Repository repository : repositories.values()) {
      repository.start();

      eventBus.post(new RepositoryRestoredEvent(repository));
    }
  }

  @Override
  protected void doStop() throws Exception {
    if (repositories.isEmpty()) {
      log.debug("No repositories defined");
      return;
    }

    log.debug("Stopping {} repositories", repositories.size());
    for (Repository repository : repositories.values()) {
      repository.stop();
    }

    log.debug("Disposing {} repositories", repositories.size());
    for (Repository repository : repositories.values()) {
      repository.destroy();
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
  public Repository get(final String name) {
    checkNotNull(name);
    ensureStarted();

    return repositories.get(name);
  }

  @Override
  public Repository create(final Configuration configuration) throws Exception {
    checkNotNull(configuration);
    ensureStarted();

    log.debug("Creating repository: {}", configuration);
    Repository repository = newRepository(configuration);

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

    eventBus.post(new RepositoryUpdatedEvent(repository));

    return repository;
  }

  @Override
  public void delete(final String name) throws Exception {
    checkNotNull(name);
    ensureStarted();

    log.debug("Deleting repository: {}", name);
    Repository repository = repository(name);
    Configuration configuration = repository.getConfiguration();
    repository.stop();
    repository.destroy();
    store.delete(configuration);
    repositories.remove(name);

    eventBus.post(new RepositoryDeletedEvent(repository));
  }
}
