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

import javax.inject.Named;

import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.Responses;
import org.sonatype.nexus.repository.view.Route;
import org.sonatype.nexus.repository.view.Router;
import org.sonatype.nexus.repository.view.ViewFacet;
import org.sonatype.nexus.repository.view.matchers.LiteralMatcher;

import static com.google.common.base.Preconditions.checkState;

/**
 * Simple {@link ViewFacet}.
 *
 * @since 3.0
 */
@Named
public class SimpleViewFacet
    extends FacetSupport
    implements ViewFacet
{
  private Router router;

  @Override
  protected void doInit(final Repository repository) throws Exception {
    Router.Builder builder = new Router.Builder();

    builder.route(new Route.Builder()
            .matcher(new LiteralMatcher("/foo", true))
            .handler(new Handler()
            {
              @Override
              public Response handle(final Context context) throws Exception {
                return Responses.notFound();
              }
            })
            .create()
    );

    builder.defaultHandlers(new Handler()
    {
      @Override
      public Response handle(final Context context) throws Exception {
        return Responses.notFound();
      }
    });

    router = builder.create();
  }

  @Override
  public Router getRouter() {
    checkState(router != null);
    return router;
  }
}
