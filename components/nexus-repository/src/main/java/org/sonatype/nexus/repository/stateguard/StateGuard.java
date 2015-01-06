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
package org.sonatype.nexus.repository.stateguard;

import java.util.Arrays;
import java.util.concurrent.Callable;
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
 * State guard.
 *
 * @since 3.0
 */
public class StateGuard
    extends ComponentSupport
{
  private final ReadWriteLock lock;

  @Nullable
  private final String failure;

  private String current;

  StateGuard(final ReadWriteLock lock,
             final String initial,
             final @Nullable String failure)
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
  public String getCurrent() {
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
  public boolean is(final String state) {
    Lock lock = readLock();
    try {
      return current.equals(state);
    }
    finally {
      lock.unlock();
    }
  }

  /**
   * Ensure the current state is one of allowed.
   */
  public void ensure(final String... allowed) {
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

  private void _ensure(final String[] allowed) {
    for (String allow : allowed) {
      if (current.equals(allow)) {
        return;
      }
    }

    throw new IllegalStateException("Invalid state: " + current + "; allowed: " + Arrays.toString(allowed));
  }

  /**
   * Create a transition to given state.
   */
  public Transition transition(final String to) {
    return new Transition(to);
  }

  /**
   * Create a guard which allows execution in the given states.
   */
  public Guard guard(final String... allowed) {
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
    private final String to;

    @Nullable
    private String[] allowed;

    Transition(final String to) {
      this.to = checkNotNull(to);
    }

    public Transition from(final String... allowed) {
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
   * Execute an action or callable if current state is allowed.
   */
  public class Guard
  {
    private final String[] allowed;

    Guard(final String[] allowed) {
      checkNotNull(allowed);
      checkArgument(allowed.length != 0);
      this.allowed = allowed;
    }

    public void run(final Action action) throws Exception {
      checkNotNull(action);

      Lock lock = readLock();
      try {
        _ensure(allowed);
        action.run();
      }
      finally {
        lock.unlock();
      }
    }

    /**
     * Helper to {link #run(Action)} and propagate exceptions.
     */
    public void runX(final Action action) {
      try {
        run(action);
      }
      catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }

    public <V> V run(final Callable<V> callable) throws Exception {
      checkNotNull(callable);

      Lock lock = readLock();
      try {
        _ensure(allowed);
        return callable.call();
      }
      finally {
        lock.unlock();
      }
    }

    /**
     * Helper to {link #run(Callable)} and propagate exceptions.
     */
    public <V> V runX(final Callable<V> callable) {
      try {
        return run(callable);
      }
      catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }
  }

  //
  // Builder
  //

  public static class Builder
  {
    private ReadWriteLock lock;

    private String initial;

    private String failure;

    // final states?

    public Builder lock(final ReadWriteLock lock) {
      this.lock = lock;
      return this;
    }

    public Builder initial(final String state) {
      this.initial = state;
      return this;
    }

    public Builder failure(final String state) {
      this.failure = state;
      return this;
    }

    public StateGuard create() {
      return new StateGuard(lock != null ? lock : new ReentrantReadWriteLock(), initial, failure);
    }
  }
}
