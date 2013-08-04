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

package org.sonatype.nexus.web;

import java.io.IOException;
import java.util.Enumeration;

import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.plexus.rest.PlexusServerServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An {@link PlexusServerServlet} that has an hardcoded name of "nexus" as required by plexus init param lookup. Guice
 * servlet extension does not allow servlet name setup while binding.
 *
 * @author adreghiciu
 */
@Singleton
class NexusRestletServlet
    extends PlexusServerServlet
{

  private static final long serialVersionUID = -840934203229475592L;

  private static final Logger log = LoggerFactory.getLogger(NexusRestletServlet.class);

  /**
   * Original servlet context delegate.
   */
  private DelegatingServletConfig servletConfig;

  NexusRestletServlet() {
    servletConfig = new DelegatingServletConfig();
  }

  @Override
  public void init()
      throws ServletException
  {
    final ClassLoader original = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    try {
      super.init();
    }
    finally {
      Thread.currentThread().setContextClassLoader(original);
    }
  }

  @Override
  public void service(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException,
             IOException
  {
    checkNotNull(request);
    checkNotNull(response);

    // Log the request URI+URL muck
    String uri = request.getRequestURI();
    if (request.getQueryString() != null) {
      uri = String.format("%s?%s", uri, request.getQueryString());
    }

    if (log.isDebugEnabled()) {
      log.debug("Processing: {} {} ({})", request.getMethod(), uri, request.getRequestURL());
    }

    MDC.put(getClass().getName(), uri);
    try {
      super.service(request, response);
    }
    finally {
      MDC.remove(getClass().getName());
    }
  }

  @Override
  public ServletConfig getServletConfig() {
    return servletConfig;
  }

  /**
   * An {@link ServletConfig} delegate that has an hardcoded servlet name.
   */
  private class DelegatingServletConfig
      implements ServletConfig
  {

    public String getServletName() {
      return "nexus";
    }

    public ServletContext getServletContext() {
      return NexusRestletServlet.super.getServletConfig().getServletContext();
    }

    public String getInitParameter(String name) {
      return NexusRestletServlet.super.getServletConfig().getInitParameter(name);
    }

    @SuppressWarnings("rawtypes")
    public Enumeration getInitParameterNames() {
      return NexusRestletServlet.super.getServletConfig().getInitParameterNames();
    }
  }

}
