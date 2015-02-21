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
package com.sonatype.nexus.repository.nuget.internal

import org.sonatype.nexus.repository.*
import org.sonatype.nexus.repository.search.SearchFacet
import org.sonatype.nexus.repository.security.SecurityHandler
import org.sonatype.nexus.repository.storage.StorageFacet
import org.sonatype.nexus.repository.types.HostedType
import org.sonatype.nexus.repository.view.ConfigurableViewFacet
import org.sonatype.nexus.repository.view.Route
import org.sonatype.nexus.repository.view.Router
import org.sonatype.nexus.repository.view.handlers.TimingHandler
import org.sonatype.nexus.repository.view.matchers.AlwaysMatcher
import org.sonatype.nexus.repository.view.matchers.LiteralMatcher
import org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher

import javax.annotation.Nonnull
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

import static com.sonatype.nexus.repository.nuget.internal.NugetFeedHandler.*
import static org.sonatype.nexus.repository.http.HttpHandlers.notFound

/**
 * Nuget hosted repository recipe.
 *
 * @since 3.0
 */
@Named(NugetHostedRecipe.NAME)
@Singleton
class NugetHostedRecipe
    extends RecipeSupport
{
  static final String NAME = "nuget-hosted"

  @Inject
  Provider<NugetSecurityFacet> securityFacet

  @Inject
  Provider<ConfigurableViewFacet> viewFacet

  @Inject
  Provider<NugetGalleryFacetImpl> galleryFacet

  @Inject
  Provider<StorageFacet> storageFacet

  @Inject
  Provider<SearchFacet> searchFacet

  @Inject
  TimingHandler timingHandler

  @Inject
  NugetFeedHandler feedHandler

  @Inject
  NugetItemHandler itemHandler

  @Inject
  SecurityHandler securityHandler

  @Inject
  NugetPushHandler pushHandler

  @Inject
  NugetStaticFeedHandler staticFeedHandler

  @Inject
  public NugetHostedRecipe(@Named(HostedType.NAME) final Type type,
                           @Named(NugetFormat.NAME) final Format format)
  {
    super(type, format)
  }

  @Override
  void apply(@Nonnull final Repository repository) throws Exception {
    repository.attach(storageFacet.get())
    repository.attach(searchFacet.get())
    repository.attach(galleryFacet.get())
    repository.attach(securityFacet.get())
    repository.attach(configure(viewFacet.get()))
  }

  private Facet configure(final ConfigurableViewFacet facet) {
    Router.Builder router = new Router.Builder()

    // Services root and /$metadata static content
    router.route(new Route.Builder()
        .matcher(LogicMatchers.or(new LiteralMatcher("/"), new LiteralMatcher("/\$metadata")))
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
    //        .handler(securityHandler)
        .handler(feedHandler)
        .handler(notFound())
        .create())

    // serving up package content
    router.route(new Route.Builder()
        .matcher(new TokenMatcher("/{id}/{version}"))
        .handler(timingHandler)
    //        .handler(securityHandler)
        .handler(itemHandler)
        .handler(notFound())
        .create())

    // Temporary means of uploading .nupkg archives to rip out their .nuspec
    router.route(new Route.Builder()
        .matcher(new AlwaysMatcher())
        .handler(timingHandler)
    //            .handler(securityHandler)
        .handler(pushHandler)
        .create())

    // By default, return a 404
    router.defaultHandlers(notFound())

    facet.configure(router.create())

    return facet
  }
}
