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
package org.sonatype.nexus.views.rawbinaries.example;

import java.io.IOException;
import java.util.Collections;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.nexus.componentviews.config.ViewConfigContributor;
import org.sonatype.nexus.componentviews.config.ViewConfigStore;
import org.sonatype.nexus.views.rawbinaries.view.RawBinaryProxyViewFactory;
import org.sonatype.nexus.views.rawbinaries.view.RawBinaryViewFactory;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.ImmutableMap;

/**
 * Contributes configuration for example "bin" and "bin-proxy" views.
 *
 * @since 3.0
 */
@Named("binaryViews")
@Singleton
public class RawViewConfigContribution
    extends ComponentSupport
    implements ViewConfigContributor
{
  private static final String VIEW_NAME = "binaries";

  private static final String PROXY_VIEW_NAME = "bin-proxy";

  @Override
  public void contributeTo(final ViewConfigStore viewConfigStore) throws IOException {
    final ViewConfig viewConfig = viewConfigStore.get(VIEW_NAME);
    if (viewConfig == null) {
      log.info("Force-adding config for /views/{}.", VIEW_NAME);
      viewConfigStore.add(
          new ViewConfig(VIEW_NAME, RawBinaryViewFactory.FACTORY_NAME, Collections.<String, Object>emptyMap()));
    }

    final ViewConfig proxyViewConfig = viewConfigStore.get(PROXY_VIEW_NAME);
    if (proxyViewConfig == null) {
      log.info("Force-adding config for /views/{}", PROXY_VIEW_NAME);
      viewConfigStore.add(
          new ViewConfig(PROXY_VIEW_NAME, RawBinaryProxyViewFactory.FACTORY_NAME,
              ImmutableMap.of("sourceName", (Object) RawSourceConfigContribution.SOURCE_NAME)));
    }
  }
}
