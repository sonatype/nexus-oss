package org.sonatype.nexus.componentviews;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * @since 3.0
 */
public class Router
{
  private final List<RequestMatcher> requestMatchers;

  public Router(final List<RequestMatcher> requestMatchers) {
    this.requestMatchers = new ArrayList<>(requestMatchers);
  }

  @Nullable
  public RequestMatcher findRoute(Request request) {
    for (RequestMatcher requestMatcher : requestMatchers) {
      if (requestMatcher.matches(request)) {
        return requestMatcher;
      }
    }
    return null;
  }
}
