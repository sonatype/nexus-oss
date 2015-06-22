/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.repository.nuget.internal.proxy

import com.sonatype.nexus.repository.nuget.internal.NugetFormat
import com.sonatype.nexus.repository.nuget.internal.NugetRecipeSupport
import org.sonatype.nexus.repository.Facet
import org.sonatype.nexus.repository.Format
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.Type
import org.sonatype.nexus.repository.httpclient.HttpClientFacet
import org.sonatype.nexus.repository.proxy.ProxyHandler
import org.sonatype.nexus.repository.types.ProxyType
import org.sonatype.nexus.repository.view.ConfigurableViewFacet
import org.sonatype.nexus.repository.view.Route
import org.sonatype.nexus.repository.view.Router
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher

import javax.annotation.Nonnull
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

import static org.sonatype.nexus.repository.http.HttpHandlers.notFound

/**
 * Nuget hosted repository recipe.
 *
 * @since 3.0
 */
@Named(NugetProxyRecipe.NAME)
@Singleton
class NugetProxyRecipe
    extends NugetRecipeSupport
{
  public static final String NAME = "nuget-proxy"

  @Inject
  Provider<NugetProxyGalleryFacet> galleryFacet

  @Inject
  Provider<NugetProxyFacet> proxyFacet

  @Inject
  Provider<HttpClientFacet> httpClientFacet

  @Inject
  ProxyHandler proxyHandler

  @Inject
  public NugetProxyRecipe(@Named(ProxyType.NAME) final Type type,
                          @Named(NugetFormat.NAME) final Format format)
  {
    super(type, format)
  }

  @Override
  void apply(@Nonnull final Repository repository) throws Exception {
    repository.attach(storageFacet.get())
    repository.attach(searchFacet.get())
    repository.attach(httpClientFacet.get())
    repository.attach(proxyFacet.get())
    repository.attach(galleryFacet.get())
    repository.attach(securityFacet.get())
    repository.attach(configure(viewFacet.get()))
  }

  protected Facet configure(final ConfigurableViewFacet facet) {
    Router.Builder router = new Router.Builder()

    addFeedRoutes(router)

    // just like the default package route, but send to proxyHandler
    router.route(new Route.Builder()
        .matcher(new TokenMatcher("/{id}/{version}"))
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(exceptionHandler)
        .handler(unitOfWorkHandler)
        .handler(proxyHandler)
        .handler(notFound())
        .create())

    // By default, return a 404
    router.defaultHandlers(notFound())

    facet.configure(router.create())

    return facet
  }
}
