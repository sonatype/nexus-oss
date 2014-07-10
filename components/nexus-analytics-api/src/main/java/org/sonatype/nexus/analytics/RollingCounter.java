package org.sonatype.nexus.analytics;

import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Rolling (zero-based) long counter.
 *
 * @since 3.0
 */
class RollingCounter
{
  private final AtomicLong value = new AtomicLong(-1);

  private final long max;

  RollingCounter(final long max) {
    checkArgument(max > 0);
    this.max = max + 1; // inclusive
  }

  public long next() {
    long current, next;
    do {
      current = value.get();
      next = (current + 1) % max;
    }
    while (!value.compareAndSet(current, next));
    return next;
  }
}
