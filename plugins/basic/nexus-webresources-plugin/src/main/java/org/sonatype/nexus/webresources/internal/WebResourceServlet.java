/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.webresources.internal;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.web.ErrorStatusException;
import org.sonatype.nexus.web.WebResource;
import org.sonatype.nexus.web.WebUtils;
import org.sonatype.nexus.webresources.WebResourceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_NOT_MODIFIED;

/**
 * Provides access to resources via configured {@link WebResourceService}.
 *
 * @since 2.8
 */
@Singleton
@Named
public class WebResourceServlet
    extends HttpServlet
{
  private static final Logger log = LoggerFactory.getLogger(WebResourceServlet.class);

  private final WebResourceService webResources;

  private final WebUtils webUtils;

  @Inject
  public WebResourceServlet(final WebResourceService webResources,
                            final WebUtils webUtils)
  {
    this.webResources = checkNotNull(webResources);
    this.webUtils = checkNotNull(webUtils);
  }

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException
  {
    webUtils.equipResponseWithStandardHeaders(response);
    super.service(request, response);
  }

  // GET

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException
  {
    String path = request.getPathInfo();

    // default-page handling
    if ("".equals(path) || "/".equals(path)) {
      path = "/index.html";
    }

    WebResource resource = webResources.getResource(path);
    if (resource != null) {
      serveResource(resource, request, response);
    }
    else {
      throw new ErrorStatusException(SC_NOT_FOUND, "Not Found", "Resource not found");
    }
  }

  /**
   * Handles a file response, all the conditional request cases, and eventually the content serving of the file item.
   */
  private void serveResource(final WebResource resource,
                             final HttpServletRequest request,
                             final HttpServletResponse response)
      throws IOException
  {
    log.trace("Serving resource: {}", resource);

    response.setHeader("Content-Type", resource.getContentType());
    response.setDateHeader("Last-Modified", resource.getLastModified());
    response.setHeader("Content-Length", String.valueOf(resource.getSize()));

    // cache-control
    if (resource.shouldCache()) {
      // default cache for 30 days
      response.setHeader("Cache-Control", "max-age=2592000");
    }
    else {
      // do not cache
      webUtils.addNoCacheResponseHeaders(response);
    }

    // honor if-modified-since GETs
    final long ifModifiedSince = request.getDateHeader("if-modified-since");
    // handle conditional GETs
    if (ifModifiedSince > -1 && resource.getLastModified() <= ifModifiedSince) {
      // this is a conditional GET using time-stamp, and resource is not modified
      response.setStatus(SC_NOT_MODIFIED);
    }
    else {
      // NEXUS-5023 disable IE for sniffing into response content
      response.setHeader("X-Content-Type-Options", "nosniff");
      // send the content only if needed (this method will be called for HEAD requests too)
      if ("GET".equalsIgnoreCase(request.getMethod())) {
        try (final InputStream in = resource.getInputStream()) {
          webUtils.sendContent(in, response);
        }
      }
    }
  }
}
