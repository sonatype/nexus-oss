package org.sonatype.nexus.componentviews;

/**
 * Part of a view that inspects a {@link ViewRequest} to determine if it should be handled, and if so, indicates which
 * handler
 *
 * @since 3.0
 */
public interface RequestMatcher
{
  /**
   * Returns {@code true} if the handler indicated by {@link #getHandlerName()} should process the request, {@code
   * false} otherwise.
   */
  public boolean matches(ViewRequest request);

  /**
   * The return value must uniquely identify one of the view's handlers.
   */
  public String getHandlerName();
}
