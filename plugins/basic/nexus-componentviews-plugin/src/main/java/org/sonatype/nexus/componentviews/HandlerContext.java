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
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Supports processing of a {@link ViewRequest} as it proceeds along a chain of {@link Handler}s.
 *
 * @since 3.0
 */
public class HandlerContext
    extends ComponentSupport
{
  private final ViewRequest request;

  private ListIterator<Handler> handlers;

  private final Map<Object, Object> attributes = Maps.newHashMap();

  public HandlerContext(final ViewRequest request) {
    this.request = checkNotNull(request);
  }

  public ViewRequest getRequest() {
    return request;
  }

  public void setHandlers(final List<Handler> handlers) {
    checkState(this.handlers == null, "Handlers can only be set once.");
    this.handlers = checkNotNull(handlers).listIterator();
  }

  public Object getAttribute(final Object key) {
    checkNotNull(key);
    return attributes.get(key);
  }

  public <T> T getAttribute(final Class<T> type) {
    checkNotNull(type);
    return type.cast(attributes.get(type));
  }

  @Nullable
  public Object setAttribute(final Object key, final @Nullable Object value) {
    checkNotNull(key);
    if (value == null) {
      return attributes.remove(key);
    }
    return attributes.put(key, value);
  }

  @Nullable
  public <T> T setAttribute(final Class<T> type, final @Nullable T value) {
    checkNotNull(type);
    if (value == null) {
      return type.cast(attributes.remove(type));
    }
    return type.cast(attributes.put(type, value));
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
