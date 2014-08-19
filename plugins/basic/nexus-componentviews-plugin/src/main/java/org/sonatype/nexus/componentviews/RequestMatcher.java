package org.sonatype.nexus.componentviews;

/**
 * @since 3.0
 */
public interface RequestMatcher
{
  public boolean matches(Request request);

  public String getHandlerName();
}
