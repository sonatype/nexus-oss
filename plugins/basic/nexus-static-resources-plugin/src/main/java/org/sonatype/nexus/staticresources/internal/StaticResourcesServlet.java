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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.internal.DevModeResources;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.plugins.rest.CacheControl;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;
import org.sonatype.nexus.util.SystemPropertiesHelper;
import org.sonatype.nexus.util.io.StreamSupport;

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

  private final String serverString;

  private final Map<String, StaticResource> staticResources;

  @Inject
  public StaticResourcesServlet(final List<NexusResourceBundle> nexusResourceBundles,
                                final MimeSupport mimeSupport,
                                final ApplicationStatusSource applicationStatusSource)
  {
    this.nexusResourceBundles = checkNotNull(nexusResourceBundles);
    this.mimeSupport = checkNotNull(mimeSupport);
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
    StaticResource staticResource = null;
    // locate it
    // first "dev" resources if enabled
    if (DevModeResources.hasResourceLocations()) {
      final File file = DevModeResources.getFileIfOnFileSystem(requestPath);
      if (file != null) {
        // directly implementing StaticResource and not CacheControl as with dev
        // resources WE DO NOT WANT Caching to happen at all
        logger.info("Delivering DEV resource {}", file.getAbsoluteFile());
        staticResource = new StaticResource()
        {
          @Override
          public String getPath() {
            return requestPath;
          }

          @Override
          public String getContentType() {
            return mimeSupport.guessMimeTypeFromPath(file.getName());
          }

          @Override
          public long getSize() {
            return file.length();
          }

          @Override
          public Long getLastModified() {
            return file.lastModified();
          }

          @Override
          public InputStream getInputStream() throws IOException {
            return new FileInputStream(file);
          }
        };
      }
    }
    // second, look at "ordinary" static resources, but only if devResource did not hit anything
    if (staticResource == null) {
      staticResource = staticResources.get(requestPath);
    }

    // deliver it, if we have anything
    if (staticResource != null) {
      doGetResource(request, response, staticResource);
    }
    else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
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
