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

import java.util.concurrent.Future;

import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskInfo.CurrentState;
import org.sonatype.nexus.scheduling.TaskInfo.RunState;
import org.sonatype.nexus.scheduling.TaskInfo.State;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

/**
 * IT that tests the special case of "run now" scheduling (aka bg jobs). These are executed as soon as possible
 * and are then removed from Scheduler.
 */
public class NowScheduledTaskLifecycleIT
    extends QuartzITSupport
{
  @Test
  public void taskLifecycleRunNow() throws Exception {
    // reset the latch
    SleeperTask.reset();

    // create the task
    final TaskConfiguration taskConfiguration = nexusTaskScheduler
        .createTaskConfigurationInstance(SleeperTask.class);
    final String RESULT = "This is the expected result";
    taskConfiguration.setString(SleeperTask.RESULT_KEY, RESULT);
    final TaskInfo<String> taskInfo = nexusTaskScheduler.submit(taskConfiguration);

    // give it some time to start
    SleeperTask.youWait.await();

    assertThat(taskInfo, notNullValue());
    assertThat(taskInfo.getId(), equalTo(taskConfiguration.getId()));
    assertThat(taskInfo.getName(), equalTo(taskConfiguration.getName()));
    assertThat(taskInfo.getConfiguration().getType(), equalTo(taskConfiguration.getType()));
    assertThat(taskInfo.getConfiguration().getCreated(), notNullValue());
    assertThat(taskInfo.getConfiguration().getUpdated(), notNullValue());
    assertThat(nexusTaskScheduler.getRunningTaskCount(), equalTo(1));

    final CurrentState<String> currentState = taskInfo.getCurrentState();
    assertThat(currentState, notNullValue());
    assertThat(currentState.getState(), equalTo(State.RUNNING));
    assertThat(currentState.getRunState(), equalTo(RunState.RUNNING));
    assertThat(currentState.getRunStarted(), notNullValue());
    assertThat(currentState.getRunStarted().getTime(), lessThan(System.currentTimeMillis()));
    final Future<String> future = currentState.getFuture();
    assertThat(future, notNullValue());

    // make it be done
    SleeperTask.meWait.countDown();
    Thread.yield();

    // and block for the result
    final String result = future.get();
    assertThat(result, equalTo(RESULT));

    // done
    assertThat(nexusTaskScheduler.getRunningTaskCount(), equalTo(0));
    // taskInfo for DONE task is terminal
    final TaskInfo ti = taskInfo.refresh();
    assertThat(ti.getCurrentState().getState(), equalTo(State.DONE));
    assertThat(System.identityHashCode(ti), equalTo(System.identityHashCode(taskInfo)));
  }
}
