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

package org.sonatype.sisu.jetty.mangler;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Mangler that sets UnavailableOnStartupException on all WebAppContexts to true (or to defined value). It does so by
 * "crawling" all the defined contexts in Jetty instance. Note: use of this handler is limited only before Jetty is
 * started (for obvious reasons)! It returns the count of affected WebAppContext encountered during it's work being
 * done
 * (in other words, returns the count of the setter invocations it did).
 *
 * @author cstamas
 * @since 1.2
 */
public class UnavailableOnStartupExceptionContextMangler
    implements ServerMangler<Integer>
{
  private final boolean throwUnavailableOnStartupException;

  public UnavailableOnStartupExceptionContextMangler() {
    this(true);
  }

  public UnavailableOnStartupExceptionContextMangler(final boolean throwUnavailableOnStartupException) {
    this.throwUnavailableOnStartupException = throwUnavailableOnStartupException;
  }

  public Integer mangle(Server server) {
    return setUnavailableOnStartupException(server.getHandlers());
  }

  // ==

  protected int setUnavailableOnStartupException(final Handler[] handlers) {
    int setCount = 0;
    for (int i = 0; i < handlers.length; i++) {
      if (handlers[i] instanceof ContextHandler) {
        final ContextHandler ctx = (ContextHandler) handlers[i];

        if (ctx instanceof WebAppContext) {
          final WebAppContext wctx = (WebAppContext) ctx;
          wctx.setThrowUnavailableOnStartupException(throwUnavailableOnStartupException);
          setCount++;
        }
      }
      else if (handlers[i] instanceof HandlerCollection) {
        final Handler[] handlerList = ((HandlerCollection) handlers[i]).getHandlers();
        setCount = setCount + setUnavailableOnStartupException(handlerList);
      }
    }
    return setCount;
  }
}
