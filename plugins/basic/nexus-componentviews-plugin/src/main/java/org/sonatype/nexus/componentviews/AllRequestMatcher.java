package org.sonatype.nexus.componentviews;

/**
 * Matches all requests, regardless of the path or any other details.
 *
 * @since 3.0
 */
public class AllRequestMatcher
    implements RequestMatcher
{
  private final String handlerName;

  public AllRequestMatcher(final String handlerName) {
    this.handlerName = handlerName;
  }

  @Override
  public boolean matches(final ViewRequest request) {
    return true;
  }

  @Override
  public String getHandlerName() {
    return handlerName;
  }
}
