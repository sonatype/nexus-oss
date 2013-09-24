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

package org.sonatype.nexus.configuration.application.runtime;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.util.PlexusUtils;
import org.sonatype.sisu.goodies.lifecycle.Lifecycle;
import org.sonatype.sisu.goodies.lifecycle.Starter;
import org.sonatype.sisu.goodies.lifecycle.Stopper;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link ApplicationRuntimeConfigurationBuilder}.
 */
@Singleton
@Named
public class DefaultApplicationRuntimeConfigurationBuilder
    extends AbstractLoggingComponent
    implements ApplicationRuntimeConfigurationBuilder
{
  private final Injector injector;

  @Inject
  public DefaultApplicationRuntimeConfigurationBuilder(final Injector injector) {
    this.injector = checkNotNull(injector);
  }

  @Override
  public Repository createRepositoryFromModel(final Configuration configuration,
      final Class<? extends Repository> type, final String name, final CRepository repoConf)
      throws ConfigurationException
  {
    final Repository repository = createRepository(type, name);
    repository.configure(repoConf);
    if (repository instanceof Lifecycle) {
      Starter.start((Lifecycle) repository);
    }
    return repository;
  }

  @Override
  public void releaseRepository(final Repository repository, final Configuration configuration,
      final CRepository repoConf) throws ConfigurationException
  {
    if (repository instanceof Lifecycle) {
      Stopper.stop((Lifecycle) repository);
    }
    // to be here only as far as we transition from Plexus
    PlexusUtils.release(repository);
  }

  // ----------------------------------------
  // private stuff

  private Repository createRepository(final Class<? extends Repository> type, final String name)
      throws InvalidConfigurationException
  {
    try {
      return injector.getProvider(Key.get(type, Names.named(name))).get();
    }
    catch (Exception e) {
      throw new InvalidConfigurationException("Could not lookup a new instance of Repository!", e);
    }
  }
}
