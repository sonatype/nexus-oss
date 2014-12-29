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
package org.sonatype.nexus.repository.simple.internal;

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
import org.sonatype.nexus.repository.types.HostedType;
import org.sonatype.nexus.repository.view.Route;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.nexus.repository.view.handlers.TimingHandler;
import org.sonatype.nexus.repository.view.matchers.LiteralMatcher;
import org.sonatype.nexus.repository.view.matchers.RegexMatcher;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.view.matchers.logic.LogicMatchers.or;

/**
 * Simple hosted repository recipe.
 *
 * @since 3.0
 */
@Named(SimpleHostedRecipe.NAME)
@Singleton
public class SimpleHostedRecipe
  extends RecipeSupport
{
  public static final String NAME = SimpleFormat.NAME + "-" + HostedType.NAME;

  private final Provider<SimpleHostedFacet> contentFacet;

  private final TimingHandler timingHandler;

  private final SimpleIndexHandler indexHandler;

  private final SimpleContentMutableHandler contentHandler;

  @Inject
  public SimpleHostedRecipe(final @Named(HostedType.NAME) Type type,
                            final @Named(SimpleFormat.NAME) Format format,
                            final Provider<SimpleHostedFacet> contentFacet,
                            final TimingHandler timingHandler,
                            final SimpleIndexHandler indexHandler,
                            final SimpleContentMutableHandler contentHandler)
  {
    super(type, format);
    this.contentFacet = checkNotNull(contentFacet);
    this.timingHandler = checkNotNull(timingHandler);
    this.indexHandler = checkNotNull(indexHandler);
    this.contentHandler = checkNotNull(contentHandler);
  }

  @Override
  public void apply(final @Nonnull Repository repository) throws Exception {
    repository.attach(contentFacet.get());

    configure(repository.facet(ViewFacet.class));
  }

  /**
   * Configure {@link ViewFacet}.
   */
  private void configure(final ViewFacet facet) {
    Router.Builder builder = new Router.Builder();

    builder.route(new Route.Builder()
        .matcher(or(
            new LiteralMatcher("/"),
            new LiteralMatcher("/index.html")))
        .handler(timingHandler)
        .handler(indexHandler)
        .create());

    builder.route(new Route.Builder()
        .matcher(new RegexMatcher("/(.*)"))
        .handler(timingHandler)
        .handler(contentHandler)
        .create());

    builder.defaultHandlers(
        HttpHandlers.badRequest()
    );

    facet.install(builder.create());
  }
}
