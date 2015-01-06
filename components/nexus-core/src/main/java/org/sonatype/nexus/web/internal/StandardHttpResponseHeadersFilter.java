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
package org.sonatype.nexus.web.internal;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.SystemStatus;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Adds standard HTTP response headers:
 *
 * <ul>
 *   <li>Server</li>
 *   <li>X-Frame-Options</li>
 *   <li>X-Content-Type-Options</li>
 * </ul>
 *
 * @since 2.8
 */
@Singleton
public class StandardHttpResponseHeadersFilter
    implements Filter
{
  private final String serverValue;

  @Inject
  public StandardHttpResponseHeadersFilter(final Provider<SystemStatus> systemStatusProvider) {
    // cache "Server" header value
    SystemStatus status = checkNotNull(systemStatusProvider).get();
    this.serverValue = String.format("Nexus/%s (%s)", status.getVersion(), status.getEditionShort());
  }

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
    HttpServletResponse httpResponse = (HttpServletResponse)response;

    httpResponse.setHeader("Server", serverValue);

    // NEXUS-6569 Add X-Frame-Options header
    httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");

    // NEXUS-5023 disable IE for sniffing into response content
    httpResponse.setHeader("X-Content-Type-Options", "nosniff");

    chain.doFilter(request, response);
  }
}
