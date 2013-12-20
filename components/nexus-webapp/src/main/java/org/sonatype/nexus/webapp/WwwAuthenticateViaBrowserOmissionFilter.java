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
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import eu.bitwalker.useragentutils.UserAgent;

/**
 * Strips out {@code WWW-Authenticate} response headers if requested via a browser (as detected by parsing User-Agent).
 *
 * This will prevent browser BASIC authentication dialogs for all browser requests.
 * Needs to be bound very early in the filter pipeline to be effective.
 *
 * @since 2.8
 */
@Named
@Singleton
public class WwwAuthenticateViaBrowserOmissionFilter
    extends ComponentSupport
    implements Filter
{
  @VisibleForTesting
  static final String WWW_AUTHENTICATE = "WWW-Authenticate";

  @VisibleForTesting
  static final String USER_AGENT = "User-Agent";

  private final Cache<String, UserAgent> cache = CacheBuilder.newBuilder()
      .maximumSize(100)
      .expireAfterWrite(2, TimeUnit.HOURS)
      .build();

  @Override
  public void init(final FilterConfig config) throws ServletException {
    // empty
  }

  @Override
  public void destroy() {
    // empty
  }

  @VisibleForTesting
  boolean shouldOmit(final String headerName, final HttpServletRequest request) {
    if (WWW_AUTHENTICATE.equalsIgnoreCase(headerName)) {
      UserAgent userAgent = parseUserAgent(request.getHeader(USER_AGENT));
      if (userAgent != null) {
        // omit for browsers
        switch (userAgent.getBrowser().getBrowserType()) {
          case WEB_BROWSER:
          case MOBILE_BROWSER:
          case TEXT_BROWSER:
            return true;
        }
      }
    }

    return false;
  }

  @Nullable
  private UserAgent parseUserAgent(final String headerValue) {
    if (headerValue == null) {
      return null;
    }

    UserAgent userAgent = cache.getIfPresent(headerValue);
    if (userAgent == null) {
      userAgent = UserAgent.parseUserAgentString(headerValue);
      cache.put(headerValue, userAgent);
    }
    return userAgent;
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
          if (shouldOmit(name, httpRequest)) {
            log.trace("Omitting header: {}={}", name, value);
          }
          else {
            super.setHeader(name, value);
          }
        }

        @Override
        public void addHeader(final String name, final String value) {
          if (shouldOmit(name, httpRequest)) {
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
