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

package org.sonatype.nexus.repository.simple;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.httpbridge.HttpResponses;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Route;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.nexus.repository.view.ViewFacetSupport;
import org.sonatype.nexus.repository.view.handlers.TimingHandler;
import org.sonatype.nexus.repository.view.matchers.LiteralMatcher;
import org.sonatype.nexus.repository.view.matchers.RegexMatcher;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple {@link ViewFacet}.
 *
 * @since 3.0
 */
@Named
public class SimpleViewFacet
    extends ViewFacetSupport
{
  private final TimingHandler timingHandler;

  private final SimpleIndexHandler indexHandler;

  private final SimpleContentHandler contentHandler;

  @Inject
  public SimpleViewFacet(final TimingHandler timingHandler,
                         final SimpleIndexHandler indexHandler,
                         final SimpleContentHandler contentHandler)
  {
    this.timingHandler = checkNotNull(timingHandler);
    this.indexHandler = checkNotNull(indexHandler);
    this.contentHandler = checkNotNull(contentHandler);
  }

  @Override
  protected Router createRouter() throws Exception {
    Router.Builder builder = new Router.Builder();

    // TODO: Consider if we want to build an OrMatcher helper?
    // TODO: Consider if we want to add route-ids so we can reference defined routes for re-use (maybe builder state only)?
    // TODO: Consider a set of default handlers, as here we have timingHandler on each route?

    builder.route(new Route.Builder()
        .matcher(new LiteralMatcher("/content/"))
        .handler(timingHandler)
        .handler(indexHandler)
        .create());

    builder.route(new Route.Builder()
        .matcher(new LiteralMatcher("/content/index.html"))
        .handler(timingHandler)
        .handler(indexHandler)
        .create());

    builder.route(new Route.Builder()
        .matcher(new RegexMatcher("/content/(.*)"))
        .handler(timingHandler)
        .handler(contentHandler)
        .create());

    builder.defaultHandlers(
        timingHandler,
        new Handler()
        {
          @Override
          public Response handle(final @Nonnull Context context) throws Exception {
            return HttpResponses.notFound();
          }
        }
    );

    return builder.create();
  }
}
