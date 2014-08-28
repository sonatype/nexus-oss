package org.sonatype.nexus.componentviews.internal;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.sonatype.nexus.componentviews.Request;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
public class WrappingRequest
    implements Request
{
  private final HttpServletRequest request;

  private final String viewName;

  private final String path;

  private final Map<String, Object> requestContext = new HashMap<>();

  public WrappingRequest(final HttpServletRequest request, final String viewName, final String path) {
    this.request = checkNotNull(request);
    this.viewName = checkNotNull(viewName);
    this.path = checkNotNull(path);
  }

  @Override
  public String getViewName() {
    return viewName;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public HttpMethod getMethod() {
    switch (request.getMethod()) {
      case "GET":
        return HttpMethod.GET;
      case "PUT":
        return HttpMethod.PUT;
      case "DELETE":
        return HttpMethod.DELETE;
      default:
        throw new IllegalStateException("Method not allowed.");
    }
  }

  @Override
  public String getMimeType() {
    return request.getContentType();
  }

  @Override
  public InputStream getInputStream() {
    return null;
  }

  @Override
  public void setAttribute(final String name, final Object value) {
    requestContext.put(name, value);
  }

  @Nullable
  @Override
  public Object getAttribute(final String name) {
    return requestContext.get(name);
  }

  private String firstPathSegment(String path) {
    final String[] split = path.split("/");
    if (split.length < 2) {
      return null;
    }
    return split[1];
  }
}
