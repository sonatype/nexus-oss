/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.componentviews;

import java.util.List;
import java.util.ListIterator;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Supports processing of a {@link ViewRequest} as it proceeds along a chain of {@link Handler}s.
 *
 * @since 3.0
 */
public class HandlerContext
    extends ComponentSupport
{
  private final ViewRequest request;

  private final ListIterator<Handler> handlers;

  public HandlerContext(final ViewRequest request, final List<Handler> handlers) {
    this.request = checkNotNull(request);
    this.handlers = checkNotNull(handlers).listIterator();
  }

  public ViewRequest getRequest() {
    return request;
  }

  /**
   * Invokes the next handler in the handler chain. Interceptor-style handlers should invoke this from their {@link
   * Handler#handle(HandlerContext)} method and return the result.
   */
  public ViewResponse proceed() throws Exception {
    log.debug("Proceed");

    if (handlers.hasNext()) {
      Handler handler = handlers.next();
      log.debug("Handler: {}", handler);

      try {
        return handler.handle(this);
      }
      finally {
        if (handlers.hasPrevious()) {
          handlers.previous();
        }
      }
    }
    else {
      throw new Exception("Unable to proceed");
    }
  }
}
