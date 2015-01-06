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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
public class SimpleStateGuard<StateT extends Enum>
    extends ComponentSupport
{
  private final ReadWriteLock lock;

  @Nullable
  private final StateT failure;

  private StateT current;

  SimpleStateGuard(final ReadWriteLock lock,
                   final StateT initial,
                   final @Nullable StateT failure)
  {
    this.lock = checkNotNull(lock);
    this.current = checkNotNull(initial);
    this.failure = failure;
  }

  private Lock tryLock(final Lock lock) {
    try {
      lock.tryLock(1, TimeUnit.MINUTES);
    }
    catch (InterruptedException e) {
      throw Throwables.propagate(e);
    }
    return lock;
  }

  private Lock readLock() {
    return tryLock(lock.readLock());
  }

  private Lock writeLock() {
    return tryLock(lock.writeLock());
  }

  /**
   * Returns the current state.
   */
  public StateT getCurrent() {
    Lock lock = readLock();
    try {
      return current;
    }
    finally {
      lock.unlock();
    }
  }

  /**
   * Check if the current state is given state.
   */
  public boolean is(final StateT state) {
    Lock lock = readLock();
    try {
      return current == state;
    }
    finally {
      lock.unlock();
    }
  }

  /**
   * Ensure the current state is one of allowed.
   */
  @SafeVarargs
  public final void ensure(final StateT... allowed) {
    checkNotNull(allowed);
    checkArgument(allowed.length != 0);

    Lock lock = readLock();
    try {
      _ensure(allowed);
    }
    finally {
      lock.unlock();
    }
  }

  private void _ensure(final StateT[] allowed) {
    for (StateT allow : allowed) {
      if (current == allow) {
        return;
      }
    }

    throw new IllegalStateException("Invalid state: " + current.name() + "; allowed: " + Arrays.toString(allowed));
  }

  /**
   * Create a transition to given state.
   */
  public Transition transition(final StateT to) {
    return new Transition(to);
  }

  /**
   * Create a guard which allows execution in the given states.
   */
  @SafeVarargs
  public final Guard guard(final StateT... allowed) {
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
    private final StateT to;

    @Nullable
    private StateT[] allowed;

    Transition(final StateT to) {
      this.to = checkNotNull(to);
    }

    @SafeVarargs
    public final Transition from(final StateT... allowed) {
      checkNotNull(allowed);
      checkArgument(allowed.length != 0);
      this.allowed = allowed;
      return this;
    }

    public void run(final Action action) throws Exception {
      Lock lock = writeLock();
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
        lock.unlock();
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
    private final StateT[] allowed;

    Guard(final StateT[] allowed) {
      checkNotNull(allowed);
      checkArgument(allowed.length != 0);
      this.allowed = allowed;
    }

    public void run(final Action action) throws Exception {
      Lock lock = readLock();
      try {
        _ensure(allowed);
        action.run();
      }
      finally {
        lock.unlock();
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

  public static class Builder<StateT extends Enum>
  {
    private ReadWriteLock lock;

    private StateT initial;

    private StateT failure;

    // final states?

    public Builder<StateT> lock(final ReadWriteLock lock) {
      this.lock = lock;
      return this;
    }

    public Builder<StateT> initial(final StateT state) {
      this.initial = state;
      return this;
    }

    public Builder<StateT> failure(final StateT state) {
      this.failure = state;
      return this;
    }

    public SimpleStateGuard<StateT> create() {
      return new SimpleStateGuard<>(lock != null ? lock : new ReentrantReadWriteLock(), initial, failure);
    }
  }
}
