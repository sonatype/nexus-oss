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
package com.sonatype.nexus.repository.nuget.internal

import org.sonatype.nexus.repository.Facet
import org.sonatype.nexus.repository.Format
import org.sonatype.nexus.repository.RecipeSupport
import org.sonatype.nexus.repository.Type
import org.sonatype.nexus.repository.search.SearchFacet
import org.sonatype.nexus.repository.security.SecurityHandler
import org.sonatype.nexus.repository.storage.StorageFacet
import org.sonatype.nexus.repository.storage.UnitOfWorkHandler;
import org.sonatype.nexus.repository.types.ProxyType
import org.sonatype.nexus.repository.view.ConfigurableViewFacet
import org.sonatype.nexus.repository.view.handlers.ExceptionHandler
import org.sonatype.nexus.repository.view.Route
import org.sonatype.nexus.repository.view.Router
import org.sonatype.nexus.repository.view.handlers.TimingHandler
import org.sonatype.nexus.repository.view.matchers.LiteralMatcher
import org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

import static com.sonatype.nexus.repository.nuget.internal.NugetFeedHandler.*
import static org.sonatype.nexus.repository.http.HttpHandlers.notFound

/**
 * Support for common operations in defining nuget repos.
 *
 * @since 3.0
 */
abstract class NugetRecipeSupport
    extends RecipeSupport
{
  @Inject
  Provider<NugetSecurityFacet> securityFacet

  @Inject
  Provider<ConfigurableViewFacet> viewFacet

  @Inject
  Provider<StorageFacet> storageFacet

  @Inject
  Provider<SearchFacet> searchFacet

  @Inject
  ExceptionHandler exceptionHandler

  @Inject
  TimingHandler timingHandler

  @Inject
  NugetFeedHandler feedHandler

  @Inject
  NugetItemHandler itemHandler

  @Inject
  SecurityHandler securityHandler

  @Inject
  UnitOfWorkHandler unitOfWorkHandler

  @Inject
  NugetPushHandler pushHandler

  @Inject
  NugetStaticFeedHandler staticFeedHandler

  @Inject
  public NugetRecipeSupport(@Named(ProxyType.NAME) final Type type,
                            @Named(NugetFormat.NAME) final Format format)
  {
    super(type, format)
  }

  protected Facet configure(final ConfigurableViewFacet facet) {
    Router.Builder router = new Router.Builder()

    addFeedRoutes(router)

    addPackageRoute(router)

    // By default, return a 404
    router.defaultHandlers(notFound())

    facet.configure(router.create())

    return facet
  }

  protected void addFeedRoutes(Router.Builder router) {
    // Services root and /$metadata static content
    router.route(new Route.Builder()
        .matcher(LogicMatchers.or(new LiteralMatcher("/"), new LiteralMatcher("/\$metadata")))
        .handler(securityHandler)
        .handler(staticFeedHandler)
        .create());

    // Metadata feed operations
    // <galleryBase>/Operation()/?queryParameters
    // includes $count, Packages, Search, and FindPackagesById
    // TODO: Are the parentheses optional? They are in the old code
    router.route(new Route.Builder()
        .matcher(LogicMatchers.or(new TokenMatcher(FEED_COUNT_PATTERN), new TokenMatcher(FEED_PATTERN),
        new TokenMatcher(PACKAGE_ENTRY_PATTERN)))
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(unitOfWorkHandler)
        .handler(feedHandler)
        .handler(notFound())
        .create())
  }

  protected Router.Builder addPackageRoute(Router.Builder router) {
    router.route(new Route.Builder()
        .matcher(new TokenMatcher("/{id}/{version}"))
        .handler(timingHandler)
        .handler(securityHandler)
        .handler(unitOfWorkHandler)
        .handler(itemHandler)
        .handler(notFound())
        .create())
  }
}
