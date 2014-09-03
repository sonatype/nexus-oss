package org.sonatype.nexus.componentviews.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.sonatype.nexus.componentviews.ViewRequest;
import org.sonatype.nexus.componentviews.config.ViewConfig;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A simple implementation of {@link ViewRequest} which decorates the {@link HttpServletRequest}.
 *
 * @since 3.0
 */
public class WrappingViewRequest
    implements ViewRequest
{
  private final HttpServletRequest request;

  private final ViewConfig viewConfig;

  private final String path;

  private final Map<String, Object> requestContext = new HashMap<>();

  public WrappingViewRequest(final HttpServletRequest request, final String viewName, final ViewConfig viewConfig,
                             final String path)
  {
    this.viewConfig = checkNotNull(viewConfig);
    this.request = checkNotNull(request);
    this.path = checkNotNull(path);
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
  public ViewConfig getViewConfig() {
    return viewConfig;
  }

  @Override
  public String getMimeType() {
    return request.getContentType();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return request.getInputStream();
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
