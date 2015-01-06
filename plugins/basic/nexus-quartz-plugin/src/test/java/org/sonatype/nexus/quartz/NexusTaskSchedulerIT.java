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
package org.sonatype.nexus.quartz;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskInfo.State;
import org.sonatype.nexus.scheduling.schedule.Manual;
import org.sonatype.nexus.scheduling.schedule.Weekly;
import org.sonatype.nexus.scheduling.schedule.Weekly.Weekday;
import org.sonatype.nexus.tasks.EmptyTrashTask;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

/**
 * IT for NexusTaskScheduler when backed by real Quartz provider.
 */
public class NexusTaskSchedulerIT
    extends QuartzITSupport
{
  @Test
  public void taskRemove() throws Exception {
    final TaskConfiguration configuration = taskScheduler.createTaskConfigurationInstance(EmptyTrashTask.class);
    taskScheduler.scheduleTask(configuration, new Manual());

    String id;
    {
      List<TaskInfo<?>> taskInfos = taskScheduler.listsTasks();
      assertThat(taskInfos, hasSize(1));
      assertThat(taskInfos.get(0).getConfiguration().getTypeId(), equalTo(configuration.getTypeId()));
      id = taskInfos.get(0).getId();
    }

    {
      TaskInfo<?> taskInfo = taskScheduler.getTaskById("foo-bar-not-exists");
      assertThat(taskInfo, nullValue());

      taskInfo = taskScheduler.getTaskById(id);
      assertThat(taskInfo, notNullValue());
      assertThat(taskInfo.getSchedule(), instanceOf(Manual.class));
      assertThat(taskInfo.getCurrentState().getState(), is(State.WAITING));
      assertThat(taskInfo.getCurrentState().getNextRun(), nullValue());
      // reschedule it (but in future)
      taskScheduler.rescheduleTask(id,
          new Weekly(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1L)), ImmutableSet.of(Weekday.FRI)));
    }

    {
      TaskInfo<?> taskInfo = taskScheduler.getTaskById(id);
      assertThat(taskInfo, notNullValue());
      assertThat(taskInfo.getSchedule(), instanceOf(Weekly.class));
      assertThat(taskInfo.getCurrentState().getState(), is(State.WAITING));
      assertThat(taskInfo.getCurrentState().getNextRun(), notNullValue());
      // remove it
      assertThat(taskInfo.remove(), is(true));
    }

    {
      TaskInfo<?> taskInfo = taskScheduler.getTaskById(id);
      assertThat(taskInfo, nullValue());

      List<TaskInfo<?>> taskInfos = taskScheduler.listsTasks();
      assertThat(taskInfos, hasSize(0));
    }
  }
}
