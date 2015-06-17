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
package org.sonatype.nexus.transaction;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.annotation.Nullable;

import com.google.common.base.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Utility class that gives any contained transactional methods access to transactions.
 *
 * <pre>
 * try {
 *   UnitOfWork.begin(transactionSupplier);
 *   // ... do some transactional work
 * }
 * finally {
 *   UnitOfWork.end();
 * }
 * </pre>
 *
 * @since 3.0
 */
public class UnitOfWork
{
  private static final ThreadLocal<Deque<Supplier<? extends Transaction>>> WORK = new ThreadLocal<>();

  private static final ThreadLocal<Transaction> TX = new ThreadLocal<>();

  /**
   * Begins a new unit-of-work which can prepare transactions for transactional methods.
   */
  public static <T extends Transaction> void begin(final Supplier<T> work) {
    checkNotNull(work);
    checkState(TX.get() == null, "Transaction already in progress");
    Deque<Supplier<? extends Transaction>> workItems = WORK.get();
    if (workItems == null) {
      workItems = new ArrayDeque<>();
      WORK.set(workItems);
    }
    workItems.push(work);
  }

  @SuppressWarnings("unchecked")
  public static @Nullable <T extends Transaction> T currentTransaction() {
    return (T) TX.get();
  }

  /**
   * Ends the current unit-of-work.
   */
  public static void end() {
    checkState(TX.get() == null, "Transaction still in progress");
    final Deque<Supplier<? extends Transaction>> workItems = WORK.get();
    checkState(workItems != null, "Unit of work has not been set");
    workItems.pop();
    if (workItems.isEmpty()) {
      WORK.remove();
    }
  }

  /**
   * @return Transaction prepared by the current unit-of-work
   */
  static Transaction prepareTransaction() {
    final Deque<Supplier<? extends Transaction>> workItems = WORK.get();
    checkState(workItems != null, "Unit of work has not been set");
    final Transaction tx = checkNotNull(workItems.peek().get());
    TX.set(tx);
    return tx;
  }

  /**
   * Clears the current transaction from the thread-context.
   */
  static void clearTransaction() {
    TX.remove();
  }
}
