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
package org.sonatype.nexus.repository.view;

import java.util.ListIterator;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Maps;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * View context.
 *
 * @since 3.0
 */
public class Context
    extends ComponentSupport
{
  private final Request request;

  private final Map<String, Object> attributes = Maps.newHashMap();

  private ListIterator<Handler> handlers;

  public Context(final Request request) {
    this.request = checkNotNull(request);
  }

  public Request getRequest() {
    return request;
  }

  @Nullable
  public Object get(final String key) {
    checkNotNull(key);
    return attributes.get(key);
  }

  @Nullable
  @SuppressWarnings("unchecked")
  public <T> T get(final Class<T> type) {
    checkNotNull(type);
    return (T) get(type.getName());
  }

  public Object require(final String key) {
    Object value = get(key);
    checkState(value != null, "Missing context attribute: %s", key);
    return value;
  }

  public <T> T require(final Class<T> type) {
    T value = get(type);
    checkState(value != null, "Missing context attribute: %s", type.getName());
    return value;
  }

  public Object set(final String key, final Object value) {
    checkNotNull(key);
    checkNotNull(value);
    log.trace("Set: {}={}", key, value);
    return attributes.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T set(final Class<T> type, final T value) {
    checkNotNull(type);
    return (T) set(type.getName(), value);
  }

  public void remove(final String key) {
    checkNotNull(key);
    log.trace("Remove: {}", key);
    attributes.remove(key);
  }

  public void remove(final Class type) {
    checkNotNull(type);
    remove(type.getName());
  }

  /**
   * Invokes the next handler in the handler chain.
   *
   * Interceptor-style handlers should invoke this from their {@link Handler#handle(Context)}
   * method and return the result.
   */
  @Nonnull
  public Response proceed() throws Exception {
    checkState(handlers != null, "Context not started");
    checkState(handlers.hasNext(), "End of handler chain");

    // Invoke next handler
    Handler handler = handlers.next();
    try {
      log.debug("Proceeding: {}", handler);
      return handler.handle(this);
    }
    finally {
      // retain handler position in-case of re-proceed
      if (handlers.hasPrevious()) {
        handlers.previous();
      }
    }
  }

  //
  // Framework internal
  //

  /**
   * Start route.
   */
  @Nonnull
  Response start(final Route route) throws Exception {
    checkNotNull(route);
    checkState(handlers == null, "Already started");
    log.debug("Starting: {}", route);
    handlers = route.getHandlers().listIterator();
    return proceed();
  }
}
