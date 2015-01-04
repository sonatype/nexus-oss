/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.repository.util;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nullable;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Throwables;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Simple state guard.
 *
 * @since 3.0
 */
public class SimpleStateGuard<T extends Enum>
    extends ComponentSupport
{
  private final Lock lock;

  private T current;

  @Nullable
  private T failure;

  public SimpleStateGuard(final Lock lock,
                          final T initial,
                          final @Nullable T failure)
  {
    this.lock = checkNotNull(lock);
    this.current = checkNotNull(initial);
    this.failure = failure;
  }

  private void lock() {
    try {
      // TODO: Expose for configuration, or hard-code reasonable values?
      lock.tryLock(1, TimeUnit.MINUTES);
    }
    catch (InterruptedException e) {
      throw Throwables.propagate(e);
    }
  }

  private void unlock() {
    lock.unlock();
  }

  /**
   * Returns the current state.
   */
  public T getCurrent() {
    lock();
    try {
      return current;
    }
    finally {
      unlock();
    }
  }

  /**
   * Check if the current state is given state.
   */
  public boolean is(final T state) {
    lock();
    try {
      return current == state;
    }
    finally {
      unlock();
    }
  }

  /**
   * Ensure the current state is one of allowed.
   */
  public void ensure(final T... allowed) {
    checkNotNull(allowed);
    checkArgument(allowed.length != 0);

    lock();
    try {
      _ensure(allowed);
    }
    finally {
      unlock();
    }
  }

  private void _ensure(final T[] allowed) {
    for (T allow : allowed) {
      if (current == allow) {
        return;
      }
    }

    throw new IllegalStateException("Invalid state: " + current.name() + "; allowed: " + Arrays.toString(allowed));
  }

  /**
   * Create a transition to given state.
   */
  public Transition transition(final T to) {
    return new Transition(to);
  }

  /**
   * Create a guard which allows execution in the given states.
   */
  public Guard guard(final T... allowed) {
    return new Guard(allowed);
  }

  //
  // Transition
  //

  /**
   * Transition from current state to target state and execute an action.
   */
  public class Transition
  {
    private final T to;

    @Nullable
    private T[] allowed;

    public Transition(final T to) {
      this.to = checkNotNull(to);
    }

    public Transition from(final T... allowed) {
      checkNotNull(allowed);
      checkArgument(allowed.length != 0);
      this.allowed = allowed;
      return this;
    }

    public void run(final Action action) throws Exception {
      lock();
      try {
        if (allowed != null) {
          _ensure(allowed);
        }

        try {
          log.trace("Transitioning: {} -> {}", current, to);

          action.run();
          current = to;

          log.trace("Transitioned: {}", to);
        }
        catch (Throwable t) {
          log.error("Failed transition: {} -> {}", current, to, t);

          // maybe set failure state
          if (failure != null) {
            current = failure;
          }

          Throwables.propagateIfPossible(t, Exception.class, Error.class);
          throw Throwables.propagate(t);
        }
      }
      finally {
        unlock();
      }
    }
  }

  //
  // Guard
  //

  /**
   * Execute an action if current state is allowed.
   */
  public class Guard
  {
    private final T[] allowed;

    public Guard(final T[] allowed) {
      checkNotNull(allowed);
      checkArgument(allowed.length != 0);
      this.allowed = allowed;
    }

    public void run(final Action action) throws Exception {
      lock();
      try {
        _ensure(allowed);
        action.run();
      }
      finally {
        unlock();
      }
    }
  }

  //
  // Action
  //

  public static interface Action
  {
    void run() throws Exception;
  }

  //
  // Builder
  //

  public static class Builder<T extends Enum>
  {
    private Lock lock;

    private T initial;

    private T failure;

    // final states?

    public Builder<T> lock(final Lock lock) {
      this.lock = lock;
      return this;
    }

    public Builder<T> initial(final T state) {
      this.initial = state;
      return this;
    }

    public Builder<T> failure(final T state) {
      this.failure = state;
      return this;
    }

    public SimpleStateGuard<T> create() {
      return new SimpleStateGuard<>(lock, initial, failure);
    }
  }
}
