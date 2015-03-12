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
package org.sonatype.sisu.locks;

import org.sonatype.sisu.litmus.testsupport.group.Slow;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

@Category(Slow.class)
public class MultiThreadedResourceLockIT
{

  @Test
  public void testLocalLocks()
      throws InterruptedException
  {
    // test local JVM locks
    launchThreads(new LocalResourceLockFactory());
  }

  @Test
  public void testHazelcastLocks()
      throws InterruptedException
  {
    // test distributed locks
    launchThreads(new HazelcastResourceLockFactory(null));
  }

  static class TestData
  {
    volatile boolean running;

    Thread[] ts;

    int[] sharedDepth;

    int[] exclusiveDepth;

    Throwable[] errors;
  }

  private static void launchThreads(ResourceLockFactory locks)
      throws InterruptedException
  {
    TestData data = new TestData();

    data.ts = new Thread[128];

    data.sharedDepth = new int[data.ts.length];
    data.exclusiveDepth = new int[data.ts.length];
    data.errors = new Throwable[data.ts.length];

    for (int i = 0; i < data.ts.length; i++) {
      final Locker locker = new Locker(locks, data);
      data.ts[i] = new Thread(locker);
      locker.setIndex(i);
    }

    data.running = true;

    for (final Thread element : data.ts) {
      element.start();
    }

    Thread.sleep(30000);

    data.running = false;

    for (final Thread element : data.ts) {
      element.join(8000);
    }

    boolean failed = false;
    for (final Throwable e : data.errors) {
      if (null != e) {
        e.printStackTrace();
        failed = true;
      }
    }
    assertFalse(failed);
  }

  private static class Locker
      implements Runnable
  {
    private ResourceLockFactory locks;

    private TestData data;

    private int index;

    public Locker(ResourceLockFactory locks, TestData data) {
      this.locks = locks;
      this.data = data;
    }

    public void setIndex(final int index) {
      this.index = index;
    }

    public void run() {
      try {
        final ResourceLock lk = locks.getResourceLock("TEST");
        final Thread self = Thread.currentThread();

        while (data.running) {
          final double transition = Math.random();
          if (0.0 <= transition && transition < 0.2) {
            if (data.sharedDepth[index] < 8) {
              lk.lockShared(self);
              data.sharedDepth[index]++;
            }
          }
          else if (0.2 <= transition && transition < 0.3) {
            if (data.exclusiveDepth[index] < 8) {
              lk.lockExclusive(self);
              data.exclusiveDepth[index]++;
            }
          }
          else if (0.3 <= transition && transition < 0.6) {
            if (data.exclusiveDepth[index] > 0) {
              data.exclusiveDepth[index]--;
              lk.unlockExclusive(self);
            }
            else {
              try {
                lk.unlockExclusive(self);
                fail("Expected IllegalStateException");
              }
              catch (final IllegalStateException e) {
                // expected
              }
            }
          }
          else {
            if (data.sharedDepth[index] > 0) {
              data.sharedDepth[index]--;
              lk.unlockShared(self);
            }
            else {
              try {
                lk.unlockShared(self);
                fail("Expected IllegalStateException");
              }
              catch (final IllegalStateException e) {
                // expected
              }
            }
          }
        }

        while (data.sharedDepth[index] > 0) {
          lk.unlockShared(self);
          data.sharedDepth[index]--;
        }

        while (data.exclusiveDepth[index] > 0) {
          lk.unlockExclusive(self);
          data.exclusiveDepth[index]--;
        }
      }
      catch (final Throwable e) {
        data.errors[index] = e;
      }
    }
  }
}
