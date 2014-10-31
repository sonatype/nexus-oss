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
package org.sonatype.nexus.views.rawbinaries.view;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.component.source.api.ComponentSourceRegistry;
import org.sonatype.nexus.component.source.api.PullComponentSource;
import org.sonatype.nexus.componentviews.AllRequestMatcher;
import org.sonatype.nexus.componentviews.NotFoundHandler;
import org.sonatype.nexus.componentviews.Router;
import org.sonatype.nexus.componentviews.View;
import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.nexus.componentviews.config.ViewFactory;
import org.sonatype.nexus.views.rawbinaries.internal.storage.RawBinaryStore;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * Transforms {@link ViewConfig config} for raw binary proxy views into actual {@link View} objects.
 *
 * @since 3.0
 */
@Named
@Singleton
public class RawBinaryProxyViewFactory
    extends ComponentSupport
    implements ViewFactory
{
  public static final String FACTORY_NAME = "rawBinaryProxy";

  private final RawBinaryStore binaryStore;

  private final NotFoundHandler notFoundHandler;

  private final ComponentSourceRegistry sourceRegistry;

  @Inject
  public RawBinaryProxyViewFactory(final RawBinaryStore binaryStore, final NotFoundHandler notFoundHandler,
                                   final ComponentSourceRegistry sourceRegistry)
  {
    this.binaryStore = checkNotNull(binaryStore);
    this.notFoundHandler = checkNotNull(notFoundHandler);
    this.sourceRegistry = checkNotNull(sourceRegistry);
  }

  @Override
  public String getFactoryName() {
    return FACTORY_NAME;
  }

  @Override
  public View createView(final ViewConfig config) {
    checkNotNull(config);

    final AllRequestMatcher binariesRequestMatcher = new AllRequestMatcher();

    final Router router = new Router();
    final String sourceName = (String) config.getConfiguration().get("sourceName");

    checkNotNull(sourceName, "Source name cannot be null for proxy config");

    final Provider<PullComponentSource> sourceProvider = sourceRegistry.getSourceProvider(sourceName);

    final ProxyingRawBinariesHandler proxyingWrapper = new ProxyingRawBinariesHandler(binaryStore, sourceProvider);
    final HostedRawBinariesHandler hosted = new HostedRawBinariesHandler(binaryStore);

    router.addRoute(binariesRequestMatcher, asList(proxyingWrapper, hosted));

    return new View(config, router, notFoundHandler);
  }
}
