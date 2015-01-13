/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.raw.internal;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.Format;
import org.sonatype.nexus.repository.RecipeSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.Type;
import org.sonatype.nexus.repository.httpclient.HttpClientFacet;
import org.sonatype.nexus.repository.raw.internal.negativecache.NegativeCacheFacet;
import org.sonatype.nexus.repository.raw.internal.negativecache.NegativeCacheHandler;
import org.sonatype.nexus.repository.raw.internal.negativecache.PathNegativeCacheKeyProvider;
import org.sonatype.nexus.repository.view.ConfigurableViewFacet;
import org.sonatype.nexus.repository.view.Route;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.nexus.repository.view.handlers.TimingHandler;
import org.sonatype.nexus.repository.view.matchers.AlwaysMatcher;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.httpbridge.HttpHandlers.notFound;

/**
 * A recipe for creating 'proxy' repositories for the 'raw' format.
 *
 * @since 3.0
 */
@Named("raw-proxy")
@Singleton
public class RawProxyRecipe
    extends RecipeSupport
{
  private final NegativeCacheHandler negativeCacheHandler;

  private final RawProxyHandler rawProxyHandler;

  private final TimingHandler timingHandler;

  private final Provider<ConfigurableViewFacet> viewFacet;

  private final Provider<HttpClientFacet> httpClient;

  private final Provider<NegativeCacheFacet> negativeCache;

  private final Provider<PathNegativeCacheKeyProvider> negativeCacheKeyProvider;

  @Inject
  public RawProxyRecipe(final @Named("proxy") Type type,
                        final @Named("raw") Format format,
                        final NegativeCacheHandler negativeCacheHandler,
                        final RawProxyHandler rawProxyHandler,
                        final TimingHandler timingHandler,
                        final Provider<ConfigurableViewFacet> viewFacet,
                        final Provider<HttpClientFacet> httpClient,
                        final Provider<NegativeCacheFacet> negativeCache,
                        final Provider<PathNegativeCacheKeyProvider> negativeCacheKeyProvider)
  {
    super(type, format);


    this.negativeCacheHandler = checkNotNull(negativeCacheHandler);
    this.rawProxyHandler = checkNotNull(rawProxyHandler);
    this.timingHandler = checkNotNull(timingHandler);
    this.viewFacet = checkNotNull(viewFacet);
    this.httpClient = checkNotNull(httpClient);
    this.negativeCache = checkNotNull(negativeCache);
    this.negativeCacheKeyProvider = checkNotNull(negativeCacheKeyProvider);
  }

  @Override
  public void apply(final @Nonnull Repository repository) throws Exception {
    repository.attach(httpClient.get());
    repository.attach(configure(viewFacet.get()));
    repository.attach(negativeCache.get());
    repository.attach(negativeCacheKeyProvider.get());
  }

  /**
   * Configure {@link ViewFacet}.
   */
  private ViewFacet configure(final ConfigurableViewFacet facet) {
    Router.Builder router = new Router.Builder();

    // Build the primary route of the raw proxy view

    router.route(new Route.Builder()
            .matcher(new AlwaysMatcher())
            .handler(timingHandler)
            .handler(negativeCacheHandler)
                // TODO: Do we have the content cached already? Is the cache up to date?
                // Find the content remotely, or return 404
            .handler(rawProxyHandler)
            .handler(notFound())
            .create()
    );

    // By default, return a 404
    router.defaultHandlers(notFound());

    facet.configure(router.create());

    return facet;
  }
}
