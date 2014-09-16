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
package org.sonatype.nexus.componentviews.internal;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.ViewRegistry;
import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.nexus.componentviews.config.ViewConfigContributor;
import org.sonatype.nexus.componentviews.config.ViewConfigStore;
import org.sonatype.nexus.componentviews.config.ViewFactory;
import org.sonatype.nexus.componentviews.config.ViewFactorySource;
import org.sonatype.nexus.componentviews.example.PathListViewFactory;
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
public class ViewModuleBooter
    extends ComponentSupport
    implements EventSubscriber
{
  private final EventBus eventBus;

  private final ViewFactorySource viewFactorySource;

  private final ViewConfigStore viewConfigStore;

  private final ViewRegistry viewRegistry;

  private final Set<ViewConfigContributor> configContributors;

  // TODO: This is a temporary example view
  private final PathListViewFactory pathListViewFactory;

  @Inject
  public ViewModuleBooter(final EventBus eventBus, final ViewFactorySource viewFactorySource,
                          final ViewConfigStore viewConfigStore, final ViewRegistry viewRegistry,
                          final PathListViewFactory pathListViewFactory,
                          final Set<ViewConfigContributor> configContributors)
  {
    this.configContributors = checkNotNull(configContributors);
    this.eventBus = checkNotNull(eventBus);
    this.viewFactorySource = checkNotNull(viewFactorySource);
    this.viewConfigStore = checkNotNull(viewConfigStore);
    this.viewRegistry = checkNotNull(viewRegistry);
    this.pathListViewFactory = checkNotNull(pathListViewFactory);
  }

  @Subscribe
  public void on(NexusInitializedEvent event) throws Exception {
    viewConfigStore.start();
    for (ViewConfigContributor contrib : configContributors) {
      contrib.contributeTo(viewConfigStore);
    }
    createViews();
  }

  @Subscribe
  public void on(NexusStoppingEvent event) throws Exception {
    viewConfigStore.stop();
  }

  private void createViews()
      throws IOException
  {
    log.debug("Creating component views.");
    for (ViewConfig config : viewConfigStore.getAll().values()) {
      final ViewFactory factory = viewFactorySource.getFactory(config.getFactoryName());
      if (factory != null) {
        log.debug("Creating view {}", config.getViewName());
        viewRegistry.registerView(factory.createView(config));
      }
      else {
        log.error("No view factory found for view config {}", config);
        throw new IllegalStateException("No view factory found for view config " + config);
      }
    }
  }
}

