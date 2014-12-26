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

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.NotFoundHandler;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Responses;
import org.sonatype.nexus.repository.view.Route;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.nexus.repository.view.ViewFacetSupport;
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

  private final ContentHandler contentHandler;

  @Inject
  public SimpleViewFacet(final TimingHandler timingHandler,
                         final ContentHandler contentHandler)
  {
    this.timingHandler = checkNotNull(timingHandler);
    this.contentHandler = checkNotNull(contentHandler);
  }

  @Override
  protected Router createRouter() throws Exception {
    Router.Builder builder = new Router.Builder();

    builder.route(new Route.Builder()
        .matcher(new LiteralMatcher("/foo"))
        .handler(timingHandler)
        .handler(new Handler()
        {
          @Nonnull
          @Override
          public Response handle(final @Nonnull Context context) throws Exception {
            return Responses.notFound("Xenu says no /foo for you");
          }
        })
        .create());

    builder.route(new Route.Builder()
        .matcher(new RegexMatcher("/content/(.*)"))
        .handler(timingHandler)
        .handler(new Handler()
        {
          @Nonnull
          @Override
          public Response handle(@Nonnull final Context context) throws Exception {
            log.debug("Xenu approves");
            return context.proceed();
          }
        })
        .handler(contentHandler)
        .create());

    builder.defaultHandlers(
        timingHandler,
        new Handler()
        {
          @Nonnull
          @Override
          public Response handle(@Nonnull final Context context) throws Exception {
            log.debug("Xenu may not be happy if you keep this up");
            return context.proceed();
          }
        },
        new NotFoundHandler()
    );

    return builder.create();
  }
}
