package org.sonatype.nexus.componentviews;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Polls an ordered list of {@link RequestMatcher}s to find the first one willing to handle a given request.
 *
 * @since 3.0
 */
public class Router
{
  private final List<RequestMatcher> requestMatchers;

  public Router(final List<RequestMatcher> requestMatchers) {
    this.requestMatchers = new ArrayList<>(requestMatchers);
  }

  @Nullable
  public RequestMatcher findRoute(ViewRequest request) {
    for (RequestMatcher requestMatcher : requestMatchers) {
      if (requestMatcher.matches(request)) {
        return requestMatcher;
      }
    }
    return null;
  }
}
