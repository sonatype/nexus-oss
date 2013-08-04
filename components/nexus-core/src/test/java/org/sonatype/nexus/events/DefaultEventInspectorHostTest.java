/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.events;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import org.sonatype.nexus.NexusAppTestSupport;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspectorWrapper;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.plexus.appevents.Event;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class DefaultEventInspectorHostTest
    extends NexusAppTestSupport
{
  @Test
  public void testSyncThenAsyncExecution()
      throws Exception
  {
    final InvocationTimestampEventInspector syncEI = new InvocationTimestampEventInspector();
    final InvocationTimestampEventInspector asyncEI = new InvocationTimestampEventInspector();

    final HashMap<String, EventInspector> map = new HashMap<String, EventInspector>(2);
    map.put("sync", syncEI);
    map.put("async", new AsynchronousEventInspectorWrapper(asyncEI));

    final DefaultEventInspectorHost host = new DefaultEventInspectorHost(map);

    host.onEvent(new NexusStartedEvent(this));

    // to handle possible async peculiarites
    syncEI.await();
    asyncEI.await();

    // they both should be invoked
    assertThat(syncEI.getInspectInvoked(), greaterThan(0L));
    assertThat(asyncEI.getInspectInvoked(), greaterThan(0L));

    // sync has to be invoked before async
    assertThat(asyncEI.getInspectInvoked(), greaterThan(syncEI.getInspectInvoked()));
    assertThat(asyncEI.getInspectInvoked() - syncEI.getInspectInvoked(), greaterThanOrEqualTo(100L));
  }

  // ==

  public static class InvocationTimestampEventInspector
      implements EventInspector
  {
    private long inspectInvoked = -1;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public long getInspectInvoked() {
      return inspectInvoked;
    }

    public void await()
        throws InterruptedException
    {
      countDownLatch.await();
    }

    @Override
    public boolean accepts(Event<?> evt) {
      return true;
    }

    @Override
    public void inspect(Event<?> evt) {
      try {
        this.inspectInvoked = System.currentTimeMillis();
        Thread.sleep(100);
      }
      catch (InterruptedException e) {
        // nothing
      }
      finally {
        countDownLatch.countDown();
      }
    }
  }
}
