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
import org.sonatype.nexus.repository.httpbridge.HttpHandlers;
import org.sonatype.nexus.repository.view.ConfigurableViewFacet;
import org.sonatype.nexus.repository.view.Route;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.nexus.repository.view.handlers.TimingHandler;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
@Named("raw-hosted")
@Singleton
public class RawHostedRecipe
    extends RecipeSupport
{
  // TODO: For now, we use an in-memory raw storage facet
  private final Provider<InMemoryRawStorageFacet> rawStorageFacet;

  private final Provider<ConfigurableViewFacet> viewFacet;

  private final Provider<RawIndexFacet> rawIndexFacet;

  private final TimingHandler timingHandler;

  private final RawStorageHandler rawStorageHandler;

  @Inject
  public RawHostedRecipe(final Type type, final Format format,
                         final Provider<ConfigurableViewFacet> viewFacet,
                         final Provider<InMemoryRawStorageFacet> rawStorageFacet,
                         final Provider<RawIndexFacet> rawIndexFacet,
                         final TimingHandler timingHandler,
                         final RawStorageHandler rawStorageHandler)
  {
    super(type, format);
    this.viewFacet = checkNotNull(viewFacet);
    this.rawStorageFacet = checkNotNull(rawStorageFacet);
    this.timingHandler = checkNotNull(timingHandler);
    this.rawStorageHandler = checkNotNull(rawStorageHandler);
    this.rawIndexFacet = checkNotNull(rawIndexFacet);
  }

  @Override
  public void apply(@Nonnull final Repository repository) throws Exception {
    repository.attach(rawStorageFacet.get());
    repository.attach(configure(viewFacet.get()));
    repository.attach(rawIndexFacet.get());
  }

  /**
   * Configure {@link ViewFacet}.
   */
  private ViewFacet configure(final ConfigurableViewFacet facet) {
    Router.Builder builder = new Router.Builder();

    builder.route(new Route.Builder()
        .matcher(new TokenMatcher("/{name:.+}"))
        .handler(timingHandler)
        .handler(rawStorageHandler)
        .create());

    builder.defaultHandlers(
        HttpHandlers.badRequest()
    );

    facet.configure(builder.create());

    return facet;
  }
}
