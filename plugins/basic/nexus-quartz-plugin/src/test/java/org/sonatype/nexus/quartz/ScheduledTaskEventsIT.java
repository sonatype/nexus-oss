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
package org.sonatype.nexus.quartz;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskInfo.EndState;
import org.sonatype.nexus.scheduling.TaskInfo.State;
import org.sonatype.nexus.scheduling.events.NexusTaskEvent;
import org.sonatype.nexus.scheduling.events.NexusTaskEventCanceled;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStarted;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedCanceled;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedDone;
import org.sonatype.nexus.scheduling.events.NexusTaskEventStoppedFailed;
import org.sonatype.nexus.scheduling.schedule.Hourly;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * IT for task eventing.
 */
public class ScheduledTaskEventsIT
    extends QuartzITSupport
{
  @Inject
  protected EventBus eventBus;

  protected Listener listener;

  @Before
  public void addListener() {
    this.listener = new Listener();
    eventBus.register(listener);
  }

  @After
  public void removeListener() {
    eventBus.unregister(listener);
  }

  @Test
  public void goodRun() throws Exception {
    // reset the latch
    SleeperTask.reset();

    // create the task
    final TaskConfiguration taskConfiguration = nexusTaskScheduler
        .createTaskConfigurationInstance(SleeperTask.class);
    final String RESULT = "This is the expected result";
    taskConfiguration.setString(SleeperTask.RESULT_KEY, RESULT);
    final TaskInfo<String> taskInfo = nexusTaskScheduler.scheduleTask(taskConfiguration, new Hourly(new Date()));

    // give it some time to start
    SleeperTask.youWait.await();

    // make it be done
    SleeperTask.meWait.countDown();
    Thread.yield();

    // the fact that future.get returned still does not mean that the pool is done
    // pool maintenance might not be done yet
    // so let's sleep for some
    Thread.sleep(500);
    // done
    assertThat(nexusTaskScheduler.getRunningTaskCount(), equalTo(0));
    assertThat(taskInfo.getCurrentState().getState(), equalTo(State.WAITING));
    assertThat(taskInfo.getLastRunState().getEndState(), equalTo(EndState.OK));

    // started, stoppedDone
    assertThat(listener.arrivedEvents, hasSize(2));
    assertThat(listener.arrivedEvents.get(0), instanceOf(NexusTaskEventStarted.class));
    assertThat(listener.arrivedEvents.get(1), instanceOf(NexusTaskEventStoppedDone.class));
  }

  @Test
  public void failedRunCheckedException() throws Exception {
    // reset the latch
    SleeperTask.reset();
    SleeperTask.exception = new IOException("foo");

    // create the task
    final TaskConfiguration taskConfiguration = nexusTaskScheduler
        .createTaskConfigurationInstance(SleeperTask.class);
    final String RESULT = "This is the expected result";
    taskConfiguration.setString(SleeperTask.RESULT_KEY, RESULT);
    final TaskInfo<String> taskInfo = nexusTaskScheduler.scheduleTask(taskConfiguration, new Hourly(new Date()));

    // give it some time to start
    SleeperTask.youWait.await();

    // make it be done
    SleeperTask.meWait.countDown();
    Thread.yield();

    // the fact that future.get returned still does not mean that the pool is done
    // pool maintenance might not be done yet
    // so let's sleep for some
    Thread.sleep(500);
    // done
    assertThat(nexusTaskScheduler.getRunningTaskCount(), equalTo(0));
    assertThat(taskInfo.getCurrentState().getState(), equalTo(State.WAITING));
    assertThat(taskInfo.getLastRunState().getEndState(), equalTo(EndState.FAILED));

    // started, stoppedDone
    assertThat(listener.arrivedEvents, hasSize(2));
    assertThat(listener.arrivedEvents.get(0), instanceOf(NexusTaskEventStarted.class));
    assertThat(listener.arrivedEvents.get(1), instanceOf(NexusTaskEventStoppedFailed.class));
    assertThat(((NexusTaskEventStoppedFailed) listener.arrivedEvents.get(1)).getFailureCause(),
        instanceOf(IOException.class));
  }

  @Test
  public void failedRunRuntimeException() throws Exception {
    // reset the latch
    SleeperTask.reset();
    SleeperTask.exception = new IllegalArgumentException("foo");

    // create the task
    final TaskConfiguration taskConfiguration = nexusTaskScheduler
        .createTaskConfigurationInstance(SleeperTask.class);
    final String RESULT = "This is the expected result";
    taskConfiguration.setString(SleeperTask.RESULT_KEY, RESULT);
    final TaskInfo<String> taskInfo = nexusTaskScheduler.scheduleTask(taskConfiguration, new Hourly(new Date()));

    // give it some time to start
    SleeperTask.youWait.await();

    // make it be done
    SleeperTask.meWait.countDown();
    Thread.yield();

    // the fact that future.get returned still does not mean that the pool is done
    // pool maintenance might not be done yet
    // so let's sleep for some
    Thread.sleep(500);
    // done
    assertThat(nexusTaskScheduler.getRunningTaskCount(), equalTo(0));
    assertThat(taskInfo.getCurrentState().getState(), equalTo(State.WAITING));
    assertThat(taskInfo.getLastRunState().getEndState(), equalTo(EndState.FAILED));

    // started, stoppedDone
    assertThat(listener.arrivedEvents, hasSize(2));
    assertThat(listener.arrivedEvents.get(0), instanceOf(NexusTaskEventStarted.class));
    assertThat(listener.arrivedEvents.get(1), instanceOf(NexusTaskEventStoppedFailed.class));
    assertThat(((NexusTaskEventStoppedFailed) listener.arrivedEvents.get(1)).getFailureCause(),
        instanceOf(IllegalArgumentException.class));
  }

  @Test
  public void canceledRun() throws Exception {
    // reset the latch
    SleeperTask.reset();

    // create the task
    final TaskConfiguration taskConfiguration = nexusTaskScheduler
        .createTaskConfigurationInstance(SleeperTask.class);
    final String RESULT = "This is the expected result";
    taskConfiguration.setString(SleeperTask.RESULT_KEY, RESULT);
    final TaskInfo<String> taskInfo = nexusTaskScheduler.scheduleTask(taskConfiguration, new Hourly(new Date()));

    // give it some time to start
    SleeperTask.youWait.await();

    taskInfo.getCurrentState().getFuture().cancel(true);

    // make it be done
    SleeperTask.meWait.countDown();
    Thread.yield();

    // the fact that future.get returned still does not mean that the pool is done
    // pool maintenance might not be done yet
    // so let's sleep for some
    Thread.sleep(500);
    // done
    assertThat(nexusTaskScheduler.getRunningTaskCount(), equalTo(0));
    assertThat(taskInfo.getCurrentState().getState(), equalTo(State.WAITING));
    assertThat(taskInfo.getLastRunState().getEndState(), equalTo(EndState.CANCELED));

    // started, stoppedDone
    assertThat(listener.arrivedEvents, hasSize(3));
    assertThat(listener.arrivedEvents.get(0), instanceOf(NexusTaskEventStarted.class));
    assertThat(listener.arrivedEvents.get(1), instanceOf(NexusTaskEventCanceled.class));
    assertThat(listener.arrivedEvents.get(2), instanceOf(NexusTaskEventStoppedCanceled.class));
  }

  static class Listener
  {
    final List<NexusTaskEvent> arrivedEvents = Lists.newArrayList();

    @Subscribe
    public void on(final NexusTaskEvent e) {
      arrivedEvents.add(e);
    }
  }
}
