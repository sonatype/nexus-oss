/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.docker.internal;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.common.app.BaseUrlHolder;
import org.sonatype.nexus.common.app.BaseUrlManager;

import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.proxy.ProxyServlet;

@Named
@Singleton
public class RepositoryReverseProxyFilter
    implements Filter
{

  private final BaseUrlManager baseUrlManager;

  private Map<Integer, String> portMappings = new ConcurrentHashMap<>();

  private TransparentReverseProxyServlet reverseProxy = new TransparentReverseProxyServlet();

  @Inject
  public RepositoryReverseProxyFilter(final BaseUrlManager baseUrlManager) {this.baseUrlManager = baseUrlManager;}

  @Override
  public void init(final FilterConfig config) throws ServletException {
    reverseProxy.init(new ServletConfig()
    {
      @Override
      public String getServletName() {
        return "reverse-proxy";
      }

      @Override
      public ServletContext getServletContext() {
        return config.getServletContext();
      }

      @Override
      public String getInitParameter(final String name) {
        return null;
      }

      @Override
      public Enumeration<String> getInitParameterNames() {
        return null;
      }
    });
  }

  @Override
  public void destroy() {
    // ignore
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
      throws IOException, ServletException
  {
    if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      String repositoryName = portMappings.get(request.getLocalPort());
      if (repositoryName != null) {
        String uri = httpRequest.getRequestURI();
        if (httpRequest.getQueryString() != null) {
          uri = uri + "?" + httpRequest.getQueryString();
        }
        if (!uri.startsWith("/static")
            && !uri.startsWith("/error.html")
            && !uri.startsWith("/repository/" + repositoryName)) {
          reverseProxy.service(request, response);
          return;
        }
      }
    }
    chain.doFilter(request, response);
  }

  void addRepository(Integer port, String repositoryName) {
    portMappings.put(port, repositoryName);
  }

  void removeRepository(Integer port) {
    portMappings.remove(port);
  }

  private class TransparentReverseProxyServlet
      extends ProxyServlet
  {
    @Override
    protected String rewriteTarget(final HttpServletRequest httpRequest) {
      String repositoryName = portMappings.get(httpRequest.getLocalPort());
      if (repositoryName != null) {
        String uri = httpRequest.getRequestURI();
        if (httpRequest.getQueryString() != null) {
          uri = uri + "?" + httpRequest.getQueryString();
        }
        if (!uri.startsWith("/repository/" + repositoryName)) {
          String baseUrl = baseUrlManager.getUrl();
          if (baseUrl == null) {
            baseUrl = BaseUrlHolder.get();
          }
          return baseUrl + "/repository/" + repositoryName + uri;
        }
      }
      return null;
    }

    @Override
    protected void addProxyHeaders(final HttpServletRequest clientRequest, final Request proxyRequest) {
      // avoid adding X-Forwarded headers
    }
  }

}
