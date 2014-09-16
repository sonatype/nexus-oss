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
   * Gets the handler for the given request, or {@code null} if no matching handler exists.
   */
  @Nullable
  public Handler findHandler(ViewRequest request) {

    for (Route route : routes) {
      if (route.getRequestMatcher().matches(request)) {
        return route.getHandler();
      }
    }

    return null;
  }

  /**
   * Routes will be considered in the order they're added.
   */
  public void addRoute(final RequestMatcher requestMatcher, final Handler handler) {
    routes.add(new Route(checkNotNull(requestMatcher), checkNotNull(handler)));
  }

  private static class Route
  {
    private final RequestMatcher requestMatcher;

    private final Handler handler;

    private Route(final RequestMatcher requestMatcher, final Handler handler) {
      this.requestMatcher = requestMatcher;
      this.handler = handler;
    }

    public RequestMatcher getRequestMatcher() {
      return requestMatcher;
    }

    public Handler getHandler() {
      return handler;
    }
  }
}
