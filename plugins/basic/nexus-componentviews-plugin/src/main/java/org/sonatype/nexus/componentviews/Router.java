/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Polls an ordered list of {@link RequestMatcher}s to find the first one willing to handle a given request.
 *
 * @since 3.0
 */
public class Router
{
  private final List<Route> routes = new ArrayList<>();

  /**
   * Gets the {@link Route} for a given {@link ViewRequest}, or {@code null} if no matching route exists.
   */
  @Nullable
  public Route findRoute(HandlerContext context) {
    for (Route route : routes) {
      if (route.getRequestMatcher().matches(context)) {
        return route;
      }
    }
    return null;
  }

  public void addRoute(final RequestMatcher requestMatcher, final List<Handler> handlers) {
    routes.add(new Route(requestMatcher, handlers));
  }

  static class Route
  {
    private final RequestMatcher requestMatcher;

    private final List<Handler> handlers;

    Route(final RequestMatcher requestMatcher, final List<Handler> handlers) {
      this.requestMatcher = checkNotNull(requestMatcher);
      this.handlers = checkNotNull(handlers);
    }

    public ViewResponse dispatch(HandlerContext context) throws Exception {
      context.setHandlers(handlers);
      return context.proceed();
    }

    public RequestMatcher getRequestMatcher() {
      return requestMatcher;
    }
  }
}
