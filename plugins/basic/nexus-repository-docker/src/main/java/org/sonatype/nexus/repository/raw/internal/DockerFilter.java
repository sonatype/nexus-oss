/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.repository.raw.internal;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

@Named
@Singleton
public class DockerFilter
    implements Filter
{

  private Map<Integer, Config> configs = new ConcurrentHashMap<>();

  @Override
  public void init(final FilterConfig config) throws ServletException {
    // ignore
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
      Config config = configs.get(request.getLocalPort());
      if (config != null) {
        String uri = httpRequest.getRequestURI();
        if (httpRequest.getQueryString() != null) {
          uri = uri + "?" + httpRequest.getQueryString();
        }
        if (!uri.startsWith("/repository/" + config.repositoryName)) {
          if (Strategy.REDIRECT == config.strategy) {
            ((HttpServletResponse) response).sendRedirect(((HttpServletResponse) response).encodeRedirectURL(
                "/repository/" + config.repositoryName + uri
            ));
          }
          else {
            RequestDispatcher requestDispatcher = httpRequest.getRequestDispatcher(
                "/repository/" + config.repositoryName + uri
            );
            requestDispatcher.forward(new RequestHack(httpRequest), response);
          }
          return;
        }
      }
    }
    chain.doFilter(request, response);
  }

  void addRepository(Integer port, String repositoryName, final Strategy strategy) {
    configs.put(port, new Config(repositoryName, strategy));
  }

  void removeRepository(Integer port) {
    configs.remove(port);
  }

  private static class Config
  {
    private final String repositoryName;

    private final Strategy strategy;

    private Config(final String repositoryName, final Strategy strategy) {
      this.repositoryName = repositoryName;
      this.strategy = strategy;
    }
  }

  private static class RequestHack
      extends HttpServletRequestWrapper
  {

    public RequestHack(final HttpServletRequest request) {
      super(request);
    }

    @Override
    public void setAttribute(final String name, final Object o) {
      if ("javax.servlet.forward.servlet_path".equals(name)) {
        return;
      }
      super.setAttribute(name, o);
    }
  }


}
