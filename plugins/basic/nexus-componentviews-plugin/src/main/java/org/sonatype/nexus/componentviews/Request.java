package org.sonatype.nexus.componentviews;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

/**
 * @since 3.0
 */
public interface Request
{
  enum HttpMethod
  {
    GET, PUT, DELETE;
  }

  String getViewName();

  /**
   * A path starting with "/" that excludes the view name.
   */
  String getPath();

  /**
   * The HTTP method invoked (e.g. "GET", "PUT").
   */
  HttpMethod getMethod();

  /**
   * The MIME type of the content, for PUT and POST requests.
   */
  @Nullable
  String getMimeType();

  /**
   * An input stream for the request body.
   */
  InputStream getInputStream() throws IOException;

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
