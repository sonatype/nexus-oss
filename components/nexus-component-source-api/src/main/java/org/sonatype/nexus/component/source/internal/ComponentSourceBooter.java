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
package org.sonatype.nexus.component.source.internal;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.component.source.config.ComponentSourceConfig;
import org.sonatype.nexus.component.source.config.ComponentSourceConfigContributor;
import org.sonatype.nexus.component.source.config.ComponentSourceConfigStore;
import org.sonatype.nexus.component.source.config.ComponentSourceFactory;
import org.sonatype.nexus.component.source.config.ComponentSourceRegistryInitializedEvent;
import org.sonatype.nexus.events.EventSubscriber;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppingEvent;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ensures that all defined views are created as Nexus starts.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ComponentSourceBooter
    extends ComponentSupport
    implements EventSubscriber
{
  private final Map<String, ComponentSourceFactory> factories;

  private final ComponentSourceConfigStore sourceConfigStore;

  private final InMemorySourceRegistry sourceRegistry;

  private final Set<ComponentSourceConfigContributor> configContributors;

  private final EventBus eventBus;

  @Inject
  public ComponentSourceBooter(Map<String, ComponentSourceFactory> factories,
                               final @Named("orient") ComponentSourceConfigStore sourceConfigStore,
                               final InMemorySourceRegistry sourceRegistry,
                               final Set<ComponentSourceConfigContributor> configContributors, final EventBus eventBus)
  {
    this.eventBus = checkNotNull(eventBus);
    this.factories = checkNotNull(factories);
    this.configContributors = checkNotNull(configContributors);
    this.sourceConfigStore = checkNotNull(sourceConfigStore);
    this.sourceRegistry = checkNotNull(sourceRegistry);
  }

  @Subscribe
  public void on(NexusInitializedEvent event) throws Exception {
    sourceConfigStore.start();
    for (ComponentSourceConfigContributor contrib : configContributors) {
      contrib.contributeTo(sourceConfigStore);
    }
    createSources();
    eventBus.post(new ComponentSourceRegistryInitializedEvent(sourceRegistry));
  }

  @Subscribe
  public void on(NexusStoppingEvent event) throws Exception {
    sourceConfigStore.stop();
  }

  private void createSources()
      throws IOException
  {
    log.debug("Creating component sources.");
    for (ComponentSourceConfig config : sourceConfigStore.getAll().values()) {
      final ComponentSourceFactory factory = factories.get(config.getFactoryName());
      if (factory != null) {
        log.debug("Creating source {}", config.getSourceId());

        sourceRegistry.register(factory.createSource(config));
      }
      else {
        throw new IllegalStateException("No source factory found for view config " + config);
      }
    }
  }
}

