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
import org.sonatype.nexus.repository.config.ConfigurationStore;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;

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
  private final ConfigurationStore store;

  private final Map<String,Recipe> recipes;

  private final Map<String, Repository> repositories = Maps.newHashMap();

  @Inject
  public RepositoryManagerImpl(final ConfigurationStore store,
                               final Map<String,Recipe> recipes)
  {
    this.store = checkNotNull(store);
    this.recipes = checkNotNull(recipes);

    log.debug("Recipes:");
    for (Map.Entry<String,Recipe> entry : recipes.entrySet()) {
      log.debug("Recipe '{}': {}", entry.getKey(), entry.getValue());
    }
  }

  @Override
  protected void doStart() throws Exception {
    // TODO: load all repository configs
    // TODO: re-create repository from recipe
    // TODO: start all repositories
    // TODO: fire restored event ... maybe rename to started
  }

  @Override
  protected void doStop() throws Exception {
    // TODO: stop all repositories
    // TODO: fire stopped event?
    repositories.clear();
  }

  @Override
  public Iterable<Repository> browse() {
    return ImmutableList.copyOf(repositories.values());
  }

  @Nullable
  @Override
  public Repository read(final String name) {
    checkNotNull(name);
    return repositories.get(name);
  }

  @Override
  public Repository create(final Configuration configuration) throws Exception {
    checkNotNull(configuration);
    // TODO: create repository from recipe
    // TODO: store configuration
    // TODO: add to repositories collection
    // TODO: fire created event
    return null;
  }

  @Override
  public Repository edit(final Configuration configuration) throws Exception {
    checkNotNull(configuration);
    // TODO: stop repository
    // TODO: reconfigure repository (via ConfigurationFacet)
    // TODO: start repository
    // TODO: fire updated event?
    return null;
  }

  @Override
  public void delete(final String name) throws Exception {
    checkNotNull(name);
    // TODO: stop repository
    // TODO: remove from store
    // TODO: remove from collection
    // TODO: fire deleted event
  }
}
