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
package com.sonatype.nexus.componentviews;

import java.io.IOException;

import org.sonatype.nexus.componentviews.View;
import org.sonatype.nexus.componentviews.ViewRegistry;
import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.nexus.componentviews.config.ViewFactory;
import org.sonatype.nexus.componentviews.config.ViewFactorySource;
import org.sonatype.nexus.componentviews.config.ViewConfigStore;

import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * @since 3.0
 */
public class ViewConfigApiDemo
{
  /**
   * Turn all the view configs in the config store into views, as happens when Nexus starts.
   */
  @Test
  public void instantiateAndRegisterAllViews() throws IOException {
    ViewConfigStore usvcStore = mock(ViewConfigStore.class);
    ViewFactorySource factorySource = mock(ViewFactorySource.class);
    ViewRegistry viewRegistry = mock(ViewRegistry.class);

    for (ViewConfig config : usvcStore.getAll().values()) {
      final ViewFactory factory = factorySource.getFactory(config.getFactoryName());
      final View view = factory.createView(config);
      viewRegistry.registerView(view);
    }
  }

  /**
   * Reloading a view in response to a configuration change.
   */
  public void reloadView() {
    final ViewFactorySource factorySource = mock(ViewFactorySource.class);
    final ViewRegistry viewRegistry = mock(ViewRegistry.class);

    // A changed (and persisted) configuration created by an admin
    final ViewConfig config = mock(ViewConfig.class);

    final ViewFactory factory = factorySource.getFactory(config.getFactoryName());
    final View newView = factory.createView(config);

    // If there's an existing view, we need to wait until it's safely unregistered.
    final View existingView = viewRegistry.findViewByName(config.getViewName());
    if (existingView != null) {
      viewRegistry.unregisterView(existingView);
    }
    viewRegistry.registerView(newView);
  }
}
