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
package org.sonatype.nexus.repository.view;

import java.util.List;
import java.util.Map;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * View router.
 *
 * @since 3.0
 */
public class Router
  extends ComponentSupport
{
  private final List<Route> routes;

  private final DefaultRoute defaultRoute;

  public Router(final List<Route> routes, final DefaultRoute defaultRoute) {
    this.routes = checkNotNull(routes, "Missing routes");
    this.defaultRoute = checkNotNull(defaultRoute, "Missing default route");
  }

  public Response dispatch(final Request request) throws Exception {
    checkNotNull(request);

    log.debug("Dispatching: {}", request);
    logRequest(request);

    Context context = new Context(request);
    Route route = findRoute(context);
    log.debug("Selected route: {}", route);

    context.setHandlers(route.getHandlers());

    Response response = context.proceed();
    logResponse(response);

    return response;
  }

  private void logRequest(final Request request) {
    if (log.isTraceEnabled()) {
      if (request.getHeaders().isEmpty()) {
        log.trace("No request headers");
      }
      else {
        log.trace("Request headers:");
        for (Map.Entry<String,String> header : request.getHeaders()) {
          log.trace("  {}: {}", header.getKey(), header.getValue());
        }
      }

      if (request.getAttributes().isEmpty()) {
        log.trace("No request attributes");
      }
      else {
        log.trace("Request attributes:");
        for (Map.Entry<String, Object> entry : request.getAttributes().entrySet()) {
          log.trace("  {}={}", entry.getKey(), entry.getValue());
        }
      }
    }
  }

  private void logResponse(final Response response) {
    if (log.isTraceEnabled()) {
      if (response.getHeaders().isEmpty()) {
        log.trace("No response headers");
      }
      else {
        log.trace("Response headers:");
        for (Map.Entry<String,String> header : response.getHeaders()) {
          log.trace("  {}: {}", header.getKey(), header.getValue());
        }
      }

      if (response.getAttributes().isEmpty()) {
        log.trace("No response attributes");
      }
      else {
        log.trace("Response attributes:");
        for (Map.Entry<String, Object> entry : response.getAttributes().entrySet()) {
          log.trace("  {}={}", entry.getKey(), entry.getValue());
        }
      }
    }
  }

  private Route findRoute(final Context context) {
    for (Route route : routes) {
      if (route.getMatcher().matches(context)) {
        return route;
      }
    }
    return defaultRoute;
  }

  //
  // Builder
  //

  /**
   * View {@link Router} builder.
   */
  public static class Builder
  {
    private List<Route> routes = Lists.newArrayList();

    private DefaultRoute defaultRoute;

    public Builder route(final Route route) {
      checkNotNull(route);
      routes.add(route);
      return this;
    }

    public Builder defaultHandlers(final Handler... handlers) {
      this.defaultRoute = new DefaultRoute(handlers);
      return this;
    }

    public Router create() {
      return new Router(routes, defaultRoute);
    }
  }
}
