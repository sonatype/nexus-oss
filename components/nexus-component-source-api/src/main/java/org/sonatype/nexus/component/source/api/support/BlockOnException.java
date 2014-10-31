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

import org.sonatype.nexus.util.sequence.NumberSequence;
import org.sonatype.nexus.util.time.SystemTimeSource;
import org.sonatype.nexus.util.time.TimeSource;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An {@link AutoBlockStrategy} that blocks any time an exception occurs, and reports itself blocked until after an
 * ever-escalating time delay.
 *
 * @since 3.0
 */
public class BlockOnException
    extends ComponentSupport
    implements AutoBlockStrategy
{
  private final String sourceName;

  private final NumberSequence nextDelay;

  private TimeSource timeSource = new SystemTimeSource();

  private DateTime blockedAtLeastUntil = null;

  public BlockOnException(final String sourceName, final NumberSequence minutesToDelay) {
    this.sourceName = checkNotNull(sourceName);
    this.nextDelay = checkNotNull(minutesToDelay);
  }

  @Override
  public AutoBlockState getAutoBlockState() {
    if (blockedAtLeastUntil == null) {
      return AutoBlockState.NOT_BLOCKED;
    }
    else if (blockedAtLeastUntil.isAfter(timeSource.currentTime())) {
      return AutoBlockState.AUTOBLOCKED;
    }
    return AutoBlockState.AUTOBLOCKED_STALE;
  }

  @Override
  public synchronized void handleConnectionSuccess() {
    if (blockedAtLeastUntil != null) {
      log.info("Source {} is unblocking after a successful connection.", sourceName);
    }
    blockedAtLeastUntil = null;
    nextDelay.reset();
  }

  @Override
  public synchronized void handleConnectionFailure(final Exception e) {
    if (getAutoBlockState().isRequestingAllowed()) {
      log.info("Source {} is auto-blocking due to {} communicating with remote source.", sourceName,
          e.getClass().getSimpleName(), e);
      setNextUnblockCheckTime();
      return;
    }

    if (timeSource.currentTime().isBefore(blockedAtLeastUntil)) {
      // It's too soon to check whether this source should unblock, so ignore the exception
      return;
    }

    setNextUnblockCheckTime();
  }

  @Override
  public DateTime getBlockedUntil() {
    return blockedAtLeastUntil;
  }

  /**
   * An overridable time source for testing purposes.
   */
  void setTimeSource(final TimeSource timeSource) {
    this.timeSource = timeSource;
  }

  private void setNextUnblockCheckTime() {
    int increment = (int) nextDelay.next();

    if (blockedAtLeastUntil == null) {
      blockedAtLeastUntil = timeSource.currentTime().plusMinutes(increment);
    }
    else {
      blockedAtLeastUntil = blockedAtLeastUntil.plusMinutes(increment);
    }

    log.info("Source {} is now auto-blocked until {}.", sourceName, blockedAtLeastUntil);
  }
}
