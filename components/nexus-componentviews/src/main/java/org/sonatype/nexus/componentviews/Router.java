package org.sonatype.nexus.componentviews;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * @since 3.0
 */
public class Router
{
  private final List<Route> routes;

  public Router(final List<Route> routes) {
    this.routes = new ArrayList<>(routes);
  }

  @Nullable
  public Route findRoute(Request request) {
    for (Route route : routes) {
      if (route.matches(request)) {
        return route;
      }
    }
    return null;
  }
}
