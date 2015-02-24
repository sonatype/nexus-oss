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
package org.sonatype.nexus.internal.web;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.sonatype.nexus.configuration.GlobalRestApiSettings;
import org.sonatype.nexus.web.BaseUrlHolder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Filter to set the value for {@link BaseUrlHolder}.
 *
 * @since 2.8
 */
@Singleton
public class BaseUrlHolderFilter
  implements Filter
{
  private final GlobalRestApiSettings settings;

  @Inject
  public BaseUrlHolderFilter(final GlobalRestApiSettings settings) {
    this.settings = checkNotNull(settings);
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
    String baseUrl = calculateBaseUrl((HttpServletRequest) request);
    BaseUrlHolder.set(baseUrl);
    try {
      chain.doFilter(request, response);
    }
    finally {
      BaseUrlHolder.unset();
    }
  }

  @VisibleForTesting
  String calculateBaseUrl(final HttpServletRequest request) {
    // if settings forces base-url use that
    if (settings.isEnabled() && settings.isForceBaseUrl() && !Strings.isNullOrEmpty(settings.getBaseUrl())) {
      return settings.getBaseUrl();
    }

    // else calculate from request
    return String.format("%s://%s:%d%s",
        request.getScheme(),
        request.getServerName(),
        request.getServerPort(),
        request.getContextPath());
  }
}
