package org.sonatype.nexus.componentviews;

/**
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
  public boolean matches(final Request request) {
    return true;
  }

  @Override
  public String getHandlerName() {
    return handlerName;
  }
}
