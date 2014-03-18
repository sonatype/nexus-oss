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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @since 3.0
 */
public class DelegatingFilter
    implements Filter
{
  private static Filter delegate;

  private static FilterConfig deferredConfig;

  public static synchronized void setDelegate(final Filter delegate) {
    DelegatingFilter.delegate = delegate;
    if (deferredConfig != null) {
      try {
        delegate.init(deferredConfig);
      }
      catch (final ServletException e) {
        throw new RuntimeException(e);
      }
      deferredConfig = null;
    }
  }

  public void init(final FilterConfig filterConfig) throws ServletException {
    synchronized (DelegatingFilter.class) {
      if (delegate != null) {
        delegate.init(filterConfig);
      }
      else {
        deferredConfig = filterConfig;
      }
    }
  }

  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
      throws IOException, ServletException
  {
    if (delegate != null) {
      delegate.doFilter(request, response, chain);
    }
  }

  public void destroy() {
    if (delegate != null) {
      delegate.destroy();
    }
  }
}
