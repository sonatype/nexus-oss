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

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Handler that wraps another handler, adding functionality, filtering requests or responses.
 *
 * @since 3.0
 */
public abstract class WrappingHandler
    implements Handler
{
  private final Handler innerHandler;

  public WrappingHandler(final Handler innerHandler) {
    this.innerHandler = checkNotNull(innerHandler);
  }

  protected Handler getInnerHandler() {
    return innerHandler;
  }

  @Override
  public final ViewResponse handle(final ViewRequest request) {
    return around(request);
  }

  /**
   * Override this method to provide custom checks before the request is passed onto the next link in the chain. If
   * this method returns a response, it will be returned to the client and the next link in the chain will never be
   * called.
   *
   * To communicate between handlers, see {@link ViewRequest#setAttribute(String, Object)} and {@link
   * ViewRequest#getAttribute(String)}.
   */
  @Nullable
  protected ViewResponse before(final ViewRequest request) {
    return null;
  }

  /**
   * Override this method to implement custom request-intercepting of the next link in the handler chain.
   */
  protected ViewResponse around(final ViewRequest request) {
    final ViewResponse earlyResponse = before(request);

    if (earlyResponse != null) {
      return earlyResponse;
    }

    return innerHandler.handle(request);
  }

}
