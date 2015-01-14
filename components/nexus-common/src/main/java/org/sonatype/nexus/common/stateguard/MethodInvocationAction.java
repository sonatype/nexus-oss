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

package org.sonatype.nexus.common.stateguard;

import javax.annotation.Nullable;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Adapts {@link MethodInvocation} to {@link Action}.
 *
 * @since 3.0
 */
class MethodInvocationAction
    implements Action<Object>
{
  private final MethodInvocation invocation;

  private Throwable failure;

  public MethodInvocationAction(final MethodInvocation invocation) {
    this.invocation = invocation;
  }

  @Nullable
  @Override
  public Object run() throws Exception {
    try {
      return invocation.proceed();
    }
    catch (Throwable throwable) {
      failure = throwable;
      return null;
    }
  }

  public void maybeThrow() throws Throwable {
    if (failure != null) {
      throw failure;
    }
  }
}
