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

package org.sonatype.nexus.coreui

import com.softwarementors.extjs.djn.config.annotations.DirectAction
import com.softwarementors.extjs.djn.config.annotations.DirectMethod
import org.apache.shiro.authz.annotation.RequiresAuthentication
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.hibernate.validator.constraints.NotEmpty
import org.sonatype.configuration.validation.InvalidConfigurationException
import org.sonatype.configuration.validation.ValidationMessage
import org.sonatype.configuration.validation.ValidationResponse
import org.sonatype.nexus.extdirect.DirectComponent
import org.sonatype.nexus.extdirect.DirectComponentSupport
import org.sonatype.nexus.formfields.Selectable
import org.sonatype.nexus.scheduling.NexusScheduler
import org.sonatype.nexus.scheduling.NexusTask
import org.sonatype.nexus.scheduling.TaskUtils
import org.sonatype.nexus.tasks.ScheduledTaskDescriptor
import org.sonatype.nexus.validation.Create
import org.sonatype.nexus.validation.Update
import org.sonatype.nexus.validation.Validate
import org.sonatype.scheduling.ScheduledTask
import org.sonatype.scheduling.TaskState
import org.sonatype.scheduling.schedules.AbstractSchedule
import org.sonatype.scheduling.schedules.CronSchedule
import org.sonatype.scheduling.schedules.DailySchedule
import org.sonatype.scheduling.schedules.HourlySchedule
import org.sonatype.scheduling.schedules.ManualRunSchedule
import org.sonatype.scheduling.schedules.MonthlySchedule
import org.sonatype.scheduling.schedules.OnceSchedule
import org.sonatype.scheduling.schedules.RunNowSchedule
import org.sonatype.scheduling.schedules.Schedule
import org.sonatype.scheduling.schedules.WeeklySchedule

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.groups.Default

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
  NexusScheduler nexusScheduler

  @Inject
  Set<ScheduledTaskDescriptor> descriptors

  /**
   * Retrieve a list of scheduled tasks.
   */
  @DirectMethod
  @RequiresPermissions('nexus:tasks:read')
  List<TaskXO> read() {
    return nexusScheduler.getAllTasks().values().flatten().findAll { ScheduledTask task ->
      def exposed = true
      def schedulerTask = task.schedulerTask
      if (schedulerTask instanceof NexusTask) {
        exposed = schedulerTask.exposed
      }
      return exposed ? task : null
    }.collect { ScheduledTask task ->
      TaskXO result = asTaskXO(task)
      return result
    }
  }

  /**
   * Retrieve available task types.
   * @return a list of task types
   */
  @DirectMethod
  @RequiresPermissions('nexus:tasktypes:read')
  List<TaskTypeXO> readTypes() {
    return descriptors.findAll { descriptor ->
      descriptor.exposed ? descriptor : null
    }.collect { descriptor ->
      def result = new TaskTypeXO(
          id: descriptor.id,
          name: descriptor.name,
          formFields: descriptor.formFields()?.collect { formField ->
            def formFieldXO = new FormFieldXO(
                id: formField.id,
                type: formField.type,
                label: formField.label,
                helpText: formField.helpText,
                required: formField.required,
                regexValidation: formField.regexValidation,
                initialValue: formField.initialValue
            )
            if (formField instanceof Selectable) {
              formFieldXO.storeApi = formField.storeApi
              formFieldXO.storeFilters = formField.storeFilters
              formFieldXO.idMapping = formField.idMapping
              formFieldXO.nameMapping = formField.nameMapping
            }
            return formFieldXO
          }
      )
      return result
    }
  }

  /**
   * Creates a task.
   * @param taskXO to be created
   * @return created task
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:tasks:create')
  @Validate(groups = [Create.class, Default.class])
  TaskXO create(final @NotNull(message = '[taskXO] may not be null') @Valid TaskXO taskXO) {
    Schedule schedule = asSchedule(taskXO)

    NexusTask nexusTask = nexusScheduler.createTaskInstance(taskXO.typeId)
    taskXO.properties.each { key, value ->
      nexusTask.addParameter(key, value)
    }
    TaskUtils.setAlertEmail(nexusTask, taskXO.alertEmail)
    TaskUtils.setId(nexusTask, taskXO.id)
    TaskUtils.setName(nexusTask, taskXO.name)

    ScheduledTask task = nexusScheduler.schedule(taskXO.name, nexusTask, schedule)
    task.enabled = taskXO.enabled
    nexusScheduler.updateSchedule(task)

    log.debug "Created task with type '${nexusTask.class}': ${nexusTask.name} (${nexusTask.id})"
    return asTaskXO(task)
  }

  /**
   * Updates a task.
   * @param taskXO to be updated
   * @return updated task
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:tasks:update')
  @Validate(groups = [Update.class, Default.class])
  TaskXO update(final @NotNull(message = '[taskXO] may not be null') @Valid TaskXO taskXO) {
    ScheduledTask task = nexusScheduler.getTaskById(taskXO.id);
    validateState(task)
    task.enabled = taskXO.enabled
    task.name = taskXO.name
    task.taskParams.putAll(taskXO.properties)
    TaskUtils.setAlertEmail(task, taskXO.alertEmail)
    TaskUtils.setId(task, taskXO.id)
    TaskUtils.setName(task, taskXO.name)
    task.reset()
    nexusScheduler.updateSchedule(task)

    return asTaskXO(task)
  }

  /**
   * Updates a task schedule.
   * @param taskXO to be updated
   * @return updated task
   */
  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:tasks:update')
  @Validate(groups = [Schedule.class, Default.class])
  TaskXO updateSchedule(final @NotNull(message = '[taskXO] may not be null') @Valid TaskXO taskXO) {
    ScheduledTask task = nexusScheduler.getTaskById(taskXO.id);
    validateState(task)
    task.schedule = asSchedule(taskXO)
    task.reset()
    nexusScheduler.updateSchedule(task)

    return asTaskXO(task)
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:tasks:delete')
  @Validate
  void delete_(final @NotEmpty(message = '[id] may not be empty') String id) {
    nexusScheduler.getTaskById(id)?.cancel()
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:tasksrun:get')
  @Validate
  void run(final @NotEmpty(message = '[id] may not be empty') String id) {
    nexusScheduler.getTaskById(id)?.runNow()
  }

  @DirectMethod
  @RequiresAuthentication
  @RequiresPermissions('nexus:tasksrun:delete')
  @Validate
  void stop(final @NotEmpty(message = '[id] may not be empty') String id) {
    nexusScheduler.getTaskById(id)?.cancelOnly()
  }

  static String getStatusDescription(final TaskState taskState) {
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

  static String getSchedule(final Schedule schedule) {
    if (ManualRunSchedule.class.isAssignableFrom(schedule.class)) {
      return 'manual'
    }
    else if (RunNowSchedule.class.isAssignableFrom(schedule.class)) {
      return 'internal'
    }
    else if (OnceSchedule.class.isAssignableFrom(schedule.class)) {
      return 'once'
    }
    else if (HourlySchedule.class.isAssignableFrom(schedule.class)) {
      return 'hourly'
    }
    else if (DailySchedule.class.isAssignableFrom(schedule.class)) {
      return 'daily'
    }
    else if (WeeklySchedule.class.isAssignableFrom(schedule.class)) {
      return 'weekly'
    }
    else if (MonthlySchedule.class.isAssignableFrom(schedule.class)) {
      return 'monthly'
    }
    else if (CronSchedule.class.isAssignableFrom(schedule.class)) {
      return 'advanced'
    }
    else {
      return schedule.getClass().getName()
    }
  }

  static Date getNextRun(final ScheduledTask<?> task) {
    Date nextRunTime = null

    // Run now type tasks should never have a next run time
    if (task.enabled && !task.schedule.class.isAssignableFrom(RunNowSchedule.class) && task.nextRun) {
      nextRunTime = task.nextRun
    }

    return nextRunTime
  }

  static String getLastRunResult(final ScheduledTask<?> task) {
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

  TaskXO asTaskXO(final ScheduledTask task) {
    def result = new TaskXO(
        id: task.id,
        enabled: task.enabled,
        name: task.name,
        typeId: task.type,
        typeName: (descriptors.find { it.id == task.type })?.name,
        status: task.taskState,
        statusDescription: task.enabled ? getStatusDescription(task.taskState) : 'Disabled',
        schedule: getSchedule(task.schedule),
        lastRun: task.lastRun,
        lastRunResult: getLastRunResult(task),
        nextRun: getNextRun(task),
        runnable: task.taskState in [TaskState.SUBMITTED, TaskState.WAITING],
        stoppable: task.taskState in [TaskState.RUNNING, TaskState.SLEEPING],
        alertEmail: TaskUtils.getAlertEmail(task),
        properties: task.taskParams
    )
    def schedule = task.schedule
    if (schedule instanceof AbstractSchedule) {
      result.startDate = schedule.startDate
    }
    if (schedule instanceof WeeklySchedule) {
      result.recurringDays = schedule.daysToRun
    }
    if (schedule instanceof MonthlySchedule) {
      result.recurringDays = schedule.daysToRun
    }
    if (schedule instanceof CronSchedule) {
      result.cronExpression = schedule.cronString
    }
    result
  }

  static Schedule asSchedule(final TaskXO taskXO) {
    if (taskXO.schedule == 'advanced') {
      try {
        return new CronSchedule(taskXO.cronExpression)
      }
      catch (Exception e) {
        def response = new ValidationResponse()
        response.addValidationError(new ValidationMessage('cronExpression', e.getMessage()))
        throw new InvalidConfigurationException(response)
      }
    }
    if (taskXO.schedule != 'manual') {
      if (!taskXO.startDate) {
        def response = new ValidationResponse()
        response.addValidationError(new ValidationMessage('startDate', 'May not be null'))
        throw new InvalidConfigurationException(response)
      }
      def date = Calendar.instance
      date.setTimeInMillis(taskXO.startDate.time)
      date.set(Calendar.SECOND, 0)
      date.set(Calendar.MILLISECOND, 0)
      switch (taskXO.schedule) {
        case 'once':
          def currentDate = Calendar.instance
          if (currentDate.after(date)) {
            def response = new ValidationResponse()
            if (currentDate.get(Calendar.YEAR) == date.get(Calendar.YEAR)
                && currentDate.get(Calendar.MONTH) == date.get(Calendar.MONTH)
                && currentDate.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)) {
              response.addValidationError(new ValidationMessage('startTime', 'Time is in the past'))
            }
            else {
              response.addValidationError(new ValidationMessage('startDate', 'Date is in the past'))
            }
            throw new InvalidConfigurationException(response)
          }
          return new OnceSchedule(date.time)
        case 'hourly':
          return new HourlySchedule(date.time, null)
        case 'daily':
          return new DailySchedule(date.time, null)
        case 'weekly':
          return new WeeklySchedule(date.time, null, taskXO.recurringDays as Set<Integer>)
        case 'monthly':
          return new MonthlySchedule(date.time, null, taskXO.recurringDays as Set<Integer>)
      }
    }
    return new ManualRunSchedule()
  }

  private static void validateState(final ScheduledTask<?> task) {
    TaskState state = task.taskState;
    if (TaskState.RUNNING == state || TaskState.CANCELLING == state || TaskState.SLEEPING == state) {
      throw new Exception('Task can\'t be edited while it is being executed or it is in line to be executed');
    }
  }

}
