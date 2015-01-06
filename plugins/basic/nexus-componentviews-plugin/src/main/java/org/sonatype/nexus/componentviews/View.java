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
package org.sonatype.nexus.componentviews;

import org.sonatype.nexus.componentviews.Router.Route;
import org.sonatype.nexus.componentviews.config.ViewConfig;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Throwables;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * A binding between a view name (which is part of the URL), and a list of {@link RequestMatcher request matchers},
 * each of which can delegate to a chain of {@link Handler}s.
 *
 * @since 3.0
 */
public class View
    extends ComponentSupport
{
  private final Router router;

  private final Route routeOfLastResort;

  private final ViewConfig config;

  public View(final ViewConfig config, final Router router, final Handler handlerOfLastResort) {
    this.router = checkNotNull(router);
    this.config = checkNotNull(config);
    this.routeOfLastResort = new Route(new AllRequestMatcher(), asList(handlerOfLastResort));
  }

  public String getName() {
    return config.getViewName();
  }

  public ViewResponse dispatch(final ViewRequest request) {
    final HandlerContext context = new HandlerContext(request);

    final Route route = router.findRoute(context);

    try {
      if (route == null) {
        log.debug("No route found for request.");
        return routeOfLastResort.dispatch(context);
      }

      return route.dispatch(context);
    }
    catch (Exception e) {
      // Exceptions will get caught by a global filter and nicely reported as 500 errors.
      throw Throwables.propagate(e);
    }
  }

  public ViewConfig getConfig() {
    return config;
  }
}
