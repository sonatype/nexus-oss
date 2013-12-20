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
package org.sonatype.nexus.webapp;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.annotations.VisibleForTesting;

/**
 * Strips out {@code WWW-Authenticate} response headers if requested via Ajax.
 *
 * This will prevent browser BASIC authentication dialogs for Ajax requests.
 *
 * @since 2.8
 */
@Named
@Singleton
public class WwwAuthenticateViaAjaxOmissionFilter
    extends ComponentSupport
    implements Filter
{
  private static final String WWW_AUTHENTICATE = "WWW-Authenticate";

  private static final String X_REQUESTED_WITH = "X-Requested-With";

  private static final String XML_HTTP_REQUEST = "XMLHttpRequest";

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    // empty
  }

  @Override
  public void destroy() {
    // empty
  }

  @VisibleForTesting
  boolean isOmit(final String headerName, final HttpServletRequest request) {
    if (WWW_AUTHENTICATE.equalsIgnoreCase(headerName)) {
      String value = request.getHeader(X_REQUESTED_WITH);
      return value != null && value.equalsIgnoreCase(XML_HTTP_REQUEST);
    }
    return false;
  }

  @Override
  public void doFilter(final ServletRequest request, ServletResponse response, final FilterChain chain)
      throws IOException, ServletException
  {
    if (response instanceof HttpServletResponse) {
      final HttpServletRequest httpRequest = (HttpServletRequest) request;

      response = new HttpServletResponseWrapper((HttpServletResponse) response)
      {
        @Override
        public void setHeader(final String name, final String value) {
          if (isOmit(name, httpRequest)) {
            log.trace("Omitting header: {}={}", name, value);
          }
          else {
            super.setHeader(name, value);
          }
        }

        @Override
        public void addHeader(final String name, final String value) {
          if (isOmit(name, httpRequest)) {
            log.trace("Omitting header: {}={}", name, value);
          }
          else {
            super.addHeader(name, value);
          }
        }
      };
    }

    chain.doFilter(request, response);
  }
}
