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

package org.sonatype.nexus.staticresources.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.nexus.internal.DevModeResources;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.plugins.rest.CacheControl;
import org.sonatype.nexus.plugins.rest.DefaultStaticResource;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;
import org.sonatype.nexus.staticresources.IndexPageRenderer;
import org.sonatype.nexus.util.SystemPropertiesHelper;
import org.sonatype.nexus.util.io.StreamSupport;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides access to static resources.
 *
 * @since 2.8
 */
@Singleton
@Named
public class StaticResourcesServlet
    extends HttpServlet
{
  /**
   * Buffer size to be used when pushing content to the {@link HttpServletResponse#getOutputStream()} stream. Default
   * is 8KB.
   */
  private static final int BUFFER_SIZE = SystemPropertiesHelper.getInteger(StaticResourcesServlet.class.getName()
      + ".BUFFER_SIZE", -1);

  private final Logger logger = LoggerFactory.getLogger(StaticResourcesServlet.class);

  private final List<NexusResourceBundle> nexusResourceBundles;

  private final MimeSupport mimeSupport;

  private final GlobalRestApiSettings globalRestApiSettings;

  private final IndexPageRenderer indexPageRenderer;

  private final String serverString;

  private final Map<String, StaticResource> staticResources;

  @Inject
  public StaticResourcesServlet(final List<NexusResourceBundle> nexusResourceBundles,
                                final MimeSupport mimeSupport,
                                final GlobalRestApiSettings globalRestApiSettings,
                                final @Nullable IndexPageRenderer indexPageRenderer,
                                final ApplicationStatusSource applicationStatusSource)
  {
    this.nexusResourceBundles = checkNotNull(nexusResourceBundles);
    this.mimeSupport = checkNotNull(mimeSupport);
    this.globalRestApiSettings = checkNotNull(globalRestApiSettings);
    this.indexPageRenderer = checkNotNull(indexPageRenderer);
    this.serverString = "Nexus/" + checkNotNull(applicationStatusSource).getSystemStatus().getVersion();
    this.staticResources = Maps.newHashMap();
    logger.debug("bufferSize={}", BUFFER_SIZE);
    discoverResources();
  }

  protected void discoverResources() {
    if (nexusResourceBundles.size() > 0) {
      for (NexusResourceBundle bundle : nexusResourceBundles) {
        final List<StaticResource> resources = bundle.getContributedResouces();
        if (resources != null) {
          for (StaticResource resource : resources) {
            final String path = resource.getPath();
            logger.debug("Serving static resource on path {} :: {}", path, resource);
            final StaticResource old = staticResources.put(path, resource);
            if (old != null) {
              logger.info("Overlapping static resources on path {}: old={}, new={}", path, old, resource);
            }
          }
        }
      }
    }

    // log the results
    if (DevModeResources.hasResourceLocations()) {
      logger.info("DEV mode resources ENABLED, will override mounted ones if applicable.");
    }
    logger.info("Discovered and serving {} static resources.", staticResources.size());
    if (logger.isDebugEnabled()) {
      logger.debug("StaticResources {}", staticResources.keySet());
    }
  }

  /**
   * Calculates the "application root" URL, as seen by client (from {@link HttpServletRequest} made by it), or, if
   * "force base URL" configuration is set, to that URL.
   */
  protected String getAppRootUrl(final HttpServletRequest request) {
    final StringBuilder result = new StringBuilder();
    if (globalRestApiSettings.isEnabled() && globalRestApiSettings.isForceBaseUrl()
        && !Strings.isNullOrEmpty(globalRestApiSettings.getBaseUrl())) {
      result.append(globalRestApiSettings.getBaseUrl());
    }
    else {
      String appRoot = request.getRequestURL().toString();
      final String pathInfo = request.getPathInfo();
      if (!Strings.isNullOrEmpty(pathInfo)) {
        appRoot = appRoot.substring(0, appRoot.length() - pathInfo.length());
      }
      final String servletPath = request.getServletPath();
      if (!Strings.isNullOrEmpty(servletPath)) {
        appRoot = appRoot.substring(0, appRoot.length() - servletPath.length());
      }
      result.append(appRoot);
    }
    if (!result.toString().endsWith("/")) {
      result.append("/");
    }
    return result.toString();
  }

  // service

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
                                                                                                      IOException
  {
    response.setHeader("Server", serverString);
    super.service(request, response);
  }

  // GET

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
                                                                                                    IOException
  {
    final String requestPath = request.getPathInfo();
    logger.info("Requested resource {}", requestPath);
    // 0) see is index.html needed actually
    if ("".equals(requestPath) || "/".equals(requestPath)) {
      // redirect to index.html
      response.setStatus(HttpServletResponse.SC_FOUND);
      response.addHeader("Location", getAppRootUrl(request) + "index.html");
      return;
    }
    if ("/index.html".equals(requestPath)) {
      doGetIndex(request, response);
      return;
    }

    // locate it
    StaticResource staticResource = null;
    // 1) first "dev" resources if enabled (to override everything else)
    if (DevModeResources.hasResourceLocations()) {
      final File file = DevModeResources.getFileIfOnFileSystem(requestPath);
      if (file != null) {
        logger.info("Delivering DEV resource {}", file.getAbsoluteFile());
        staticResource = new DevModeResource(requestPath, mimeSupport.guessMimeTypeFromPath(file.getName()), file);
      }
    }
    // 2) second, look at "ordinary" static resources, but only if devResource did not hit anything
    if (staticResource == null) {
      staticResource = staticResources.get(requestPath);
    }

    // 3) third, look into WAR embedded resources
    if (staticResource == null) {
      final URL resourceUrl = getServletContext().getResource(requestPath);
      if (resourceUrl != null) {
        staticResource = new DefaultStaticResource(resourceUrl, requestPath,
            mimeSupport.guessMimeTypeFromPath(requestPath));
      }
    }

    // deliver it, if we have anything
    if (staticResource != null) {
      doGetResource(request, response, staticResource);
    }
    else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
    }
  }

  /**
   * Delegates to {@link IndexPageRenderer} to render index page.
   */
  protected void doGetIndex(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    if (indexPageRenderer != null) {
      indexPageRenderer.render(request, response, getAppRootUrl(request));
    }
    else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Index page renderer not found");
    }
  }

  /**
   * Handles a file response, all the conditional request cases, and eventually the content serving of the file item.
   */
  protected void doGetResource(final HttpServletRequest request, final HttpServletResponse response,
                               final StaticResource resource) throws IOException
  {
    // content-type
    response.setHeader("Content-Type", resource.getContentType());
    // last-modified
    response.setDateHeader("Last-Modified", resource.getLastModified());
    // content-length
    response.setHeader("Content-Length", String.valueOf(resource.getSize()));
    // cache-control
    if (resource instanceof CacheControl && ((CacheControl) resource).shouldCache()) {
      // default cache for 30 days
      response.setHeader("Cache-Control", "max-age=2592000");
    }
    else {
      // do not cache
      response.setHeader("Pragma", "no-cache"); // HTTP/1.0
      response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"); // HTTP/1.1
      response.setHeader("Cache-Control", "post-check=0, pre-check=0"); // MS IE
      response.setHeader("Expires", "0"); // No caching on Proxies in between client and Nexus
    }

    // honor if-modified-since GETs
    final long ifModifiedSince = request.getDateHeader("if-modified-since");
    // handle conditional GETs
    if (ifModifiedSince > -1 && resource.getLastModified() <= ifModifiedSince) {
      // this is a conditional GET using time-stamp, and resource is not modified
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }
    else {
      // NEXUS-5023 disable IE for sniffing into response content
      response.setHeader("X-Content-Type-Options", "nosniff");
      // send the content only if needed (this method will be called for HEAD requests too)
      final boolean contentNeeded = "GET".equalsIgnoreCase(request.getMethod());
      if (contentNeeded) {
        try (final InputStream in = resource.getInputStream()) {
          sendContent(in, response);
        }
      }
    }
  }

  /**
   * Sends content by copying all bytes from the input stream to the output stream while setting the preferred buffer
   * size. At the end, it flushes response buffer.
   */
  private void sendContent(final InputStream from, final HttpServletResponse response) throws IOException {
    int bufferSize = BUFFER_SIZE;
    if (bufferSize < 1) {
      // if no user override, ask container for bufferSize
      bufferSize = response.getBufferSize();
      if (bufferSize < 1) {
        bufferSize = 8192;
        response.setBufferSize(bufferSize);
      }
    }
    else {
      // user override present, tell container what buffer size we'd like
      response.setBufferSize(bufferSize);
    }
    try (final OutputStream to = response.getOutputStream()) {
      StreamSupport.copy(from, to, bufferSize);
    }
    response.flushBuffer();
  }
}
