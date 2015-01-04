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

import javax.annotation.Nullable;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Simple state guard.
 *
 * @since 3.0
 */
public class SimpleStateGuard<T extends Enum>
  extends ComponentSupport
{
  private T current;

  public SimpleStateGuard(final T initial) {
    this.current = checkNotNull(initial);
  }

  // TODO: Sort out synchronization needed to ensure state sanity

  public T getCurrent() {
    return current;
  }

  public Transition transition(final T to) {
    return new Transition(to);
  }

  public Guard guard(final T... allowed) {
    return new Guard(allowed);
  }

  /**
   * Ensure the current state is one of allowed.
   */
  private void ensure(final T... allowed) {
    for (T allow : allowed) {
      if (current == allow) {
        return;
      }
    }

    throw new IllegalStateException("Invalid state: " + current.name() + "; allowed: " + Arrays.toString(allowed));
  }

  @Override
  public String toString() {
    return current.name();
  }

  //
  // Transition
  //

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

    public void run(final Action action) {
      if (allowed != null) {
        ensure(allowed);
      }

      try {
        log.trace("Transitioning to: {}, from: {}", to, current);

        action.run();
        current = to;

        log.trace("Transitioned to: {}", to);
      }
      catch (Exception e) { // Throwable?
        log.error("Failed transition to: {}", to, e);
        // TODO: Support failed state
      }
    }
  }

  //
  // Guard
  //

  public class Guard
  {
    private final T[] allowed;

    public Guard(final T[] allowed) {
      this.allowed = checkNotNull(allowed);
    }

    public void run(final Action action) throws Exception {
      ensure(allowed);
      action.run();
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
    private T initial;

    // error state?

    // final states?

    // lock

    public Builder<T> initial(final T initial) {
      this.initial = initial;
      return this;
    }

    public SimpleStateGuard<T> create() {
      checkState(initial != null);
      return new SimpleStateGuard<>(initial);
    }
  }
}
