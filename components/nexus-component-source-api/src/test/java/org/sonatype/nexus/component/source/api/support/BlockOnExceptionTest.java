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

import org.sonatype.nexus.util.sequence.FibonacciNumberSequence;
import org.sonatype.nexus.util.time.TimeSource;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Test the BlockOnException auto-block strategy.
 */
public class BlockOnExceptionTest
{
  private static final String SOURCE_NAME = "testSource";

  @Test
  public void blocksOnException() {
    final BlockOnException strategy = new BlockOnException(SOURCE_NAME, fibonacci(1, 1));

    assertThat(strategy.getAutoBlockState(), is(equalTo(AutoBlockState.NOT_BLOCKED)));

    strategy.handleConnectionFailure(new RuntimeException("pretend exception"));

    assertThat(strategy.getAutoBlockState(), is(equalTo(AutoBlockState.AUTOBLOCKED)));
  }

  @Test
  public void unblocksWhenToldTo() {
    final BlockOnException strategy = new BlockOnException(SOURCE_NAME, fibonacci(1, 1));

    strategy.handleConnectionFailure(new RuntimeException("pretend exception"));

    strategy.handleConnectionSuccess();

    assertThat(strategy.getAutoBlockState(), is(equalTo(AutoBlockState.NOT_BLOCKED)));
  }

  @Test
  public void autoblocksBecomeStale() {
    final BlockOnException strategy = new BlockOnException(SOURCE_NAME, fibonacci(1, 1));
    final StubTimeSource timeSource = new StubTimeSource();
    strategy.setTimeSource(timeSource);

    strategy.handleConnectionFailure(new RuntimeException("pretend exception"));

    assertThat(strategy.getAutoBlockState(), is(equalTo(AutoBlockState.AUTOBLOCKED)));

    timeSource.plusMinutes(2);

    assertThat(strategy.getAutoBlockState(), is(equalTo(AutoBlockState.AUTOBLOCKED_STALE)));
  }

  @Test
  public void testEscalatingTime() {
    final StubTimeSource stubTimeSource = new StubTimeSource();

    final int delayOne = 5;
    final int delayTwo = 7;

    final BlockOnException strategy = new BlockOnException(SOURCE_NAME,
        fibonacci(delayOne, delayTwo));
    strategy.setTimeSource(stubTimeSource);

    strategy.handleConnectionFailure(new RuntimeException());

    // Expect to be blocked until the first delay elapses
    final DateTime blockedUntil = new DateTime(0).plusMinutes(delayOne);
    assertThat(strategy.getBlockedUntil(), is(equalTo(blockedUntil)));

    // Before then, additional exceptions don't cause additional delays.
    strategy.handleConnectionFailure(new RuntimeException());
    assertThat(strategy.getBlockedUntil(), is(equalTo(blockedUntil)));

    // Once that time has passed, however, new exceptions will increment the time
    stubTimeSource.plusMinutes(delayOne + 1);
    strategy.handleConnectionFailure(new RuntimeException());

    // We should be okay to check again after ANOTHER 14-minute delay
    final DateTime secondCheckTime = new DateTime(0).plusMinutes(delayOne + delayTwo);

    assertThat(strategy.getBlockedUntil(), is(equalTo(secondCheckTime)));
  }

  private FibonacciNumberSequence fibonacci(final int delayOne, final int delayTwo) {
    return new FibonacciNumberSequence(delayOne, delayTwo);
  }

  private static class StubTimeSource
      implements TimeSource
  {
    public DateTime time = new DateTime(0L);

    public void plusMinutes(int minutes) {
      time = time.plusMinutes(minutes);
    }

    @Override
    public DateTime currentTime() {
      return time;
    }

    @Override
    public long currentTimeMillis() {
      return time.getMillis();
    }
  }
}