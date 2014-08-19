package org.sonatype.nexus.componentviews;

import javax.annotation.Nullable;

/**
 * @since 3.0
 */
public interface Request
{
  public enum HttpMethod
  {
    GET, PUT, DELETE;
  }

  public String getViewName();

  /**
   * A path starting with "/" that excludes the view name.
   */
  public String getPath();

  /**
   * The HTTP method invoked (e.g. "GET", "PUT").
   */
  public HttpMethod getMethod();

  /**
   * Adds a value to the request context.
   */
  void setAttribute(String name, Object value);

  /**
   * Retrieves a value from the request context.
   */
  @Nullable
  Object getAttribute(String name);
}
