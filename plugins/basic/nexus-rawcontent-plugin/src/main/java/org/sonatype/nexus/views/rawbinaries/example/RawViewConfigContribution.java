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
package org.sonatype.nexus.views.rawbinaries.example;

import java.io.IOException;
import java.util.Collections;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.componentviews.ViewId;
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

  private static final String INTERNAL_ID = "foo_234a34234";

  private static final String PROXY_VIEW_NAME = "bin-proxy";

  private static final String PROXY_INTERNAL_ID = "bar_34wfe32434";

  @Override
  public void contributeTo(final ViewConfigStore viewConfigStore) throws IOException {

    final ViewConfig hostedViewConfig = new ViewConfig(new ViewId(VIEW_NAME, INTERNAL_ID),
        RawBinaryViewFactory.FACTORY_NAME,
        Collections.<String, Object>emptyMap());

    addIfAbsent(hostedViewConfig, viewConfigStore);

    final ViewConfig proxyViewConfig = new ViewConfig(new ViewId(PROXY_VIEW_NAME, PROXY_INTERNAL_ID),
        RawBinaryProxyViewFactory.FACTORY_NAME,
        ImmutableMap.of("sourceName", (Object) RawSourceConfigContribution.SOURCE_NAME));

    addIfAbsent(proxyViewConfig, viewConfigStore);
  }

  private void addIfAbsent(final ViewConfig config, final ViewConfigStore viewConfigStore) throws IOException {
    final ViewConfig viewConfig = viewConfigStore.get(config.getViewName());
    if (viewConfig == null) {

      log.info("Force-adding config for /views/{}.", config.getViewName());

      viewConfigStore.add(config);
    }
  }
}
