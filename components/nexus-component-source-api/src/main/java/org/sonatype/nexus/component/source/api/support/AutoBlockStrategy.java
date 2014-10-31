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
package org.sonatype.nexus.component.source.api.support;

import javax.annotation.Nullable;

import org.joda.time.DateTime;

/**
 * A pluggable strategy for handling auto-blocking.
 *
 * @since 3.0
 */
public interface AutoBlockStrategy
{
  /**
   * Are we auto-blocked right now?
   */
  AutoBlockState getAutoBlockState();

  /**
   * Report that a successful call was made to the remote source, whether during normal operation or a connection test.
   */
  void handleConnectionSuccess();

  /**
   * Indicate that during a call to the remote source, an exception arose.
   */
  void handleConnectionFailure(Exception e);

  /**
   * If the source is blocked, this will return the earliest DateTime at which the source should be checked for
   * service to be restored, {@code null} otherwise. This is the date at which {@link #getAutoBlockState()} transitions
   * to {@link AutoBlockState#AUTOBLOCKED_STALE}, at least until {@link #handleConnectionSuccess()} or {@link
   * #handleConnectionFailure(Exception)} is called.
   */
  @Nullable
  DateTime getBlockedUntil();
}
