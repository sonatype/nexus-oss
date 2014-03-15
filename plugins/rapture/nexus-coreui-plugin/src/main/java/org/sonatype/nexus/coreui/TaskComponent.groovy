/**
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

package org.sonatype.nexus.coreui

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.rest.schedules.ScheduledServiceBaseResourceConverter
import org.sonatype.nexus.scheduling.NexusScheduler
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor
import org.sonatype.scheduling.ScheduledTask
import org.sonatype.scheduling.TaskState
import org.sonatype.scheduling.schedules.*

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Task {@link DirectComponent}.
 *
 * @since 3.0
 */
@Named
@Singleton
@DirectAction(action = 'coreui_Task')
class TaskComponent
extends DirectComponentSupport
{

  @Inject
  private NexusScheduler nexusScheduler

  @Inject
  private Set<ScheduledTaskDescriptor> descriptors

  /**
   * Retrieve a list of scheduled tasks.
   */
  @DirectMethod
  @RequiresPermissions('nexus:tasks:read')
  List<TaskXO> read() {
    def descriptors = this.descriptors
    return nexusScheduler.getAllTasks().values().flatten().collect { input ->
      def result = new TaskXO(
          id: input.id,
          enabled: input.enabled,
          name: input.name,
          typeId: input.type,
          typeName: (descriptors.find { it.id == input.type })?.name,
          status: input.taskState,
          statusDescription: getStatusDescription(input.taskState),
          schedule: getSchedule(input.schedule),
          lastRun: input.lastRun?.time,
          lastRunResult: getLastRunResult(input),
          nextRun: getNextRun(input)?.time,
          runnable: input.taskState in [TaskState.SUBMITTED, TaskState.WAITING],
          stoppable: input.taskState in [TaskState.RUNNING, TaskState.SLEEPING]
      )
      return result
    }
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:tasks:delete')
  void delete(final String id) {
    nexusScheduler.getTaskById(id)?.cancel()
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:tasksrun:get')
  void run(final String id) {
    nexusScheduler.getTaskById(id)?.runNow()
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:tasksrun:delete')
  void stop(final String id) {
    nexusScheduler.getTaskById(id)?.cancelOnly()
  }

  /**
   * Retrieve a list of scheduled tasks types.
   */
  @DirectMethod
  @RequiresPermissions('nexus:tasktypes:read')
  List<TaskTypeXO> types() {
    return descriptors.collect { input ->
      def result = new TaskTypeXO(
          id: input.id,
          name: input.name
      )
      return result
    }
  }

  private static String getStatusDescription(final TaskState taskState) {
    switch (taskState) {
      case TaskState.SUBMITTED:
      case TaskState.WAITING:
      case TaskState.FINISHED:
      case TaskState.BROKEN:
        return 'Waiting'
      case TaskState.RUNNING:
        return 'Running'
      case TaskState.SLEEPING:
        return 'Blocked'
      case TaskState.CANCELLING:
        return 'Cancelling'
      case TaskState.CANCELLED:
        return 'Cancelled'
      default:
        return 'Unknown'
    }
  }

  private static String getSchedule(final Schedule schedule) {
    if (ManualRunSchedule.class.isAssignableFrom(schedule.class)) {
      return ScheduledServiceBaseResourceConverter.SCHEDULE_TYPE_MANUAL
    }
    else if (RunNowSchedule.class.isAssignableFrom(schedule.class)) {
      return ScheduledServiceBaseResourceConverter.SCHEDULE_TYPE_RUN_NOW
    }
    else if (OnceSchedule.class.isAssignableFrom(schedule.class)) {
      return ScheduledServiceBaseResourceConverter.SCHEDULE_TYPE_ONCE
    }
    else if (HourlySchedule.class.isAssignableFrom(schedule.class)) {
      return ScheduledServiceBaseResourceConverter.SCHEDULE_TYPE_HOURLY
    }
    else if (DailySchedule.class.isAssignableFrom(schedule.class)) {
      return ScheduledServiceBaseResourceConverter.SCHEDULE_TYPE_DAILY
    }
    else if (WeeklySchedule.class.isAssignableFrom(schedule.class)) {
      return ScheduledServiceBaseResourceConverter.SCHEDULE_TYPE_WEEKLY
    }
    else if (MonthlySchedule.class.isAssignableFrom(schedule.class)) {
      return ScheduledServiceBaseResourceConverter.SCHEDULE_TYPE_MONTHLY
    }
    else if (CronSchedule.class.isAssignableFrom(schedule.class)) {
      return ScheduledServiceBaseResourceConverter.SCHEDULE_TYPE_ADVANCED
    }
    else {
      return schedule.getClass().getName()
    }
  }

  private static Date getNextRun(final ScheduledTask<?> task) {
    Date nextRunTime = null

    // Run now type tasks should never have a next run time
    if (!task.schedule.class.isAssignableFrom(RunNowSchedule.class) && task.nextRun) {
      nextRunTime = task.nextRun
    }

    return nextRunTime
  }

  protected static String getLastRunResult(final ScheduledTask<?> task) {
    String lastRunResult = null

    if (task.lastStatus != null) {
      lastRunResult = TaskState.BROKEN.equals(task.lastStatus) ? "Error" : "Ok"
      if (task.duration != 0) {
        long milliseconds = task.duration

        int hours = (int) ((milliseconds / 1000) / 3600)
        int minutes = (int) ((milliseconds / 1000) / 60 - hours * 60)
        int seconds = (int) (((long) (milliseconds / 1000)) % 60)

        lastRunResult += " ["
        if (hours != 0) {
          lastRunResult += hours
          lastRunResult += "h"
        }
        if (minutes != 0 || hours != 0) {
          lastRunResult += minutes
          lastRunResult += "m"
        }
        lastRunResult += seconds
        lastRunResult += "s"
        lastRunResult += "]"
      }
    }
    return lastRunResult
  }

}
