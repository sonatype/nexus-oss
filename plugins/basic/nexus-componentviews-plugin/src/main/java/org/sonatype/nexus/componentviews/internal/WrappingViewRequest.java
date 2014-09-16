/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
  public String getContentType() {
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
}
