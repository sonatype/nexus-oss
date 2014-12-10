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
package org.sonatype.nexus.rest.schedules_;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.validator.ApplicationValidationResponse;
import org.sonatype.nexus.rest.formfield.AbstractFormFieldResource;
import org.sonatype.nexus.rest.model.ScheduledServiceAdvancedResource;
import org.sonatype.nexus.rest.model.ScheduledServiceBaseResource;
import org.sonatype.nexus.rest.model.ScheduledServiceDailyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceHourlyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceMonthlyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceOnceResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.rest.model.ScheduledServiceWeeklyResource;
import org.sonatype.nexus.rest.schedules.ScheduledServiceBaseResourceConverter;
import org.sonatype.nexus.scheduling.TaskScheduler;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskInfo.LastRunState;
import org.sonatype.nexus.scheduling.TaskInfo.State;
import org.sonatype.nexus.scheduling.schedule.Cron;
import org.sonatype.nexus.scheduling.schedule.Daily;
import org.sonatype.nexus.scheduling.schedule.Hourly;
import org.sonatype.nexus.scheduling.schedule.Manual;
import org.sonatype.nexus.scheduling.schedule.Monthly;
import org.sonatype.nexus.scheduling.schedule.Monthly.CalendarDay;
import org.sonatype.nexus.scheduling.schedule.Now;
import org.sonatype.nexus.scheduling.schedule.Once;
import org.sonatype.nexus.scheduling.schedule.Schedule;
import org.sonatype.nexus.scheduling.schedule.Weekly;
import org.sonatype.nexus.scheduling.schedule.Weekly.Weekday;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractScheduledServicePlexusResource
    extends AbstractFormFieldResource
{
  private TaskScheduler nexusScheduler;

  public static final String SCHEDULED_SERVICE_ID_KEY = "scheduledServiceId";

  private DateFormat timeFormat = new SimpleDateFormat("HH:mm");

  @Inject
  public void setNexusScheduler(final TaskScheduler nexusScheduler) {
    this.nexusScheduler = nexusScheduler;
  }

  protected TaskScheduler getNexusScheduler() {
    return nexusScheduler;
  }

  protected String getScheduleShortName(Schedule schedule) {
    if (schedule instanceof Manual) {
      return ScheduledServiceBaseResourceConverter.SCHEDULE_TYPE_MANUAL;
    }
    else if (schedule instanceof Now) {
      return ScheduledServiceBaseResourceConverter.SCHEDULE_TYPE_RUN_NOW;
    }
    else if (schedule instanceof Once) {
      return ScheduledServiceBaseResourceConverter.SCHEDULE_TYPE_ONCE;
    }
    else if (schedule instanceof Hourly) {
      return ScheduledServiceBaseResourceConverter.SCHEDULE_TYPE_HOURLY;
    }
    else if (schedule instanceof Daily) {
      return ScheduledServiceBaseResourceConverter.SCHEDULE_TYPE_DAILY;
    }
    else if (schedule instanceof Weekly) {
      return ScheduledServiceBaseResourceConverter.SCHEDULE_TYPE_WEEKLY;
    }
    else if (schedule instanceof Monthly) {
      return ScheduledServiceBaseResourceConverter.SCHEDULE_TYPE_MONTHLY;
    }
    else if (schedule instanceof Cron) {
      return ScheduledServiceBaseResourceConverter.SCHEDULE_TYPE_ADVANCED;
    }
    else {
      return schedule.getClass().getName();
    }
  }

  protected String formatDate(Date date) {
    return Long.toString(date.getTime());
  }

  protected String formatTime(Date date) {
    return timeFormat.format(date);
  }

  protected List<ScheduledServicePropertyResource> formatServiceProperties(Map<String, String> map) {
    List<ScheduledServicePropertyResource> list = new ArrayList<ScheduledServicePropertyResource>();

    for (String key : map.keySet()) {
      if (!TaskConfiguration.isPrivateProperty(key)) {
        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setKey(key);
        prop.setValue(map.get(key));
        list.add(prop);
      }
    }

    return list;
  }

  protected List<String> formatRecurringDayOfWeek(Set<Weekday> days) {
    final List<String> list = Lists.newArrayList();
    for (Weekday day : days) {
      switch (day) {
        case SUN: {
          list.add("sunday");
          break;
        }
        case MON: {
          list.add("monday");
          break;
        }
        case TUE: {
          list.add("tuesday");
          break;
        }
        case WED: {
          list.add("wednesday");
          break;
        }
        case THU: {
          list.add("thursday");
          break;
        }
        case FRI: {
          list.add("friday");
          break;
        }
        case SAT: {
          list.add("saturday");
          break;
        }
      }
    }
    return list;
  }

  protected Set<Weekday> formatRecurringDayOfWeek(List<String> days) {
    final Set<Weekday> set = Sets.newHashSet();
    for (String day : days) {
      if ("sunday".equals(day)) {
        set.add(Weekday.SUN);
      }
      else if ("monday".equals(day)) {
        set.add(Weekday.MON);
      }
      else if ("tuesday".equals(day)) {
        set.add(Weekday.TUE);
      }
      else if ("wednesday".equals(day)) {
        set.add(Weekday.WED);
      }
      else if ("thursday".equals(day)) {
        set.add(Weekday.THU);
      }
      else if ("friday".equals(day)) {
        set.add(Weekday.FRI);
      }
      else if ("saturday".equals(day)) {
        set.add(Weekday.SAT);
      }
    }
    return set;
  }

  protected List<String> formatRecurringDayOfMonth(Set<CalendarDay> days) {
    final List<String> list = Lists.newArrayList();
    for (CalendarDay day : days) {
      if (day.isLastDayOfMonth()) {
        list.add("last");
      }
      else {
        list.add(String.valueOf(day.getDay()));
      }
    }
    return list;
  }

  protected Set<CalendarDay> formatRecurringDayOfMonth(List<String> days) {
    final Set<CalendarDay> set = Sets.newHashSet();
    for (String day : days) {
      if ("last".equals(day)) {
        set.add(CalendarDay.lastDay());
      }
      else {
        set.add(CalendarDay.day(Integer.valueOf(day)));
      }
    }
    return set;
  }

  protected Date parseDate(String date, String time) {
    Calendar cal = Calendar.getInstance();
    Calendar timeCalendar = Calendar.getInstance();

    try {
      timeCalendar.setTime(timeFormat.parse(time));

      cal.setTime(new Date(Long.parseLong(date)));
      cal.add(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
      cal.add(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));

      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Parsed date from task creation request: " + cal.getTime());
      }
    }
    catch (ParseException e) {
      cal = null;
    }

    return cal == null ? null : cal.getTime();
  }

  public String getModelName(ScheduledServiceBaseResource model) {
    return model.getName();
  }

  public TaskConfiguration getModelNexusTask(ScheduledServiceBaseResource model, Request request)
      throws IllegalArgumentException, ResourceException
  {
    try {
      TaskConfiguration task = getNexusScheduler().createTaskConfigurationInstance(model.getTypeId());

      for (Iterator iter = model.getProperties().iterator(); iter.hasNext(); ) {
        ScheduledServicePropertyResource prop = (ScheduledServicePropertyResource) iter.next();
        // TODO: for some reason, null=null mapping is in here too!
        if (prop.getKey() != null) {
          task.setString(prop.getKey(), prop.getValue());
        }
      }

      task.setAlertEmail(model.getAlertEmail());
      // new tasks will have this empty
      if (!Strings.isNullOrEmpty(model.getId())) {
        task.setId(model.getId());
      }
      task.setName(model.getName());
      task.setEnabled(model.isEnabled());

      return task;
    }
    catch (IllegalArgumentException e) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
    }
  }

  public void validateStartDate(String date)
      throws InvalidConfigurationException
  {
    Calendar cal = Calendar.getInstance();
    Date startDate = new Date(Long.parseLong(date));
    cal.setTime(startDate);

    Calendar nowCal = Calendar.getInstance();
    nowCal.add(Calendar.DAY_OF_YEAR, -1);
    nowCal.set(Calendar.HOUR, 0);
    nowCal.set(Calendar.MINUTE, 0);
    nowCal.set(Calendar.SECOND, 0);
    nowCal.set(Calendar.MILLISECOND, 0);

    // This is checking just the year/month/day, time isn't of concern right now
    // basic check that the day timestamp is roughly in the correct range
    if (cal.before(nowCal)) {
      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Validation error for startDate: " + startDate.toString());
      }

      ValidationResponse vr = new ApplicationValidationResponse();
      ValidationMessage vm = new ValidationMessage("startDate", "Date cannot be in the past.");
      vr.addValidationError(vm);
      throw new InvalidConfigurationException(vr);
    }
  }

  public void validateTime(String key, Date date)
      throws InvalidConfigurationException
  {
    if (date.before(new Date())) {
      ValidationResponse vr = new ApplicationValidationResponse();
      ValidationMessage vm = new ValidationMessage(key, "Time cannot be in the past.");
      vr.addValidationError(vm);
      throw new InvalidConfigurationException(vr);
    }
  }

  public Schedule getModelSchedule(ScheduledServiceBaseResource model)
      throws ParseException, InvalidConfigurationException
  {
    Schedule schedule = null;

    if (ScheduledServiceAdvancedResource.class.isAssignableFrom(model.getClass())) {
      schedule = new Cron(new Date(), ((ScheduledServiceAdvancedResource) model).getCronCommand());
    }
    else if (ScheduledServiceMonthlyResource.class.isAssignableFrom(model.getClass())) {
      Date date =
          parseDate(((ScheduledServiceMonthlyResource) model).getStartDate(),
              ((ScheduledServiceMonthlyResource) model).getRecurringTime());

      schedule =
          new Monthly(date,
              formatRecurringDayOfMonth(((ScheduledServiceMonthlyResource) model).getRecurringDay()));
    }
    else if (ScheduledServiceWeeklyResource.class.isAssignableFrom(model.getClass())) {
      Date date =
          parseDate(((ScheduledServiceWeeklyResource) model).getStartDate(),
              ((ScheduledServiceWeeklyResource) model).getRecurringTime());

      schedule =
          new Weekly(date,
              formatRecurringDayOfWeek(((ScheduledServiceWeeklyResource) model).getRecurringDay()));
    }
    else if (ScheduledServiceDailyResource.class.isAssignableFrom(model.getClass())) {
      Date date =
          parseDate(((ScheduledServiceDailyResource) model).getStartDate(),
              ((ScheduledServiceDailyResource) model).getRecurringTime());

      schedule = new Daily(date);
    }
    else if (ScheduledServiceHourlyResource.class.isAssignableFrom(model.getClass())) {
      Date date =
          parseDate(((ScheduledServiceHourlyResource) model).getStartDate(),
              ((ScheduledServiceHourlyResource) model).getStartTime());

      schedule = new Hourly(date);
    }
    else if (ScheduledServiceOnceResource.class.isAssignableFrom(model.getClass())) {
      Date date =
          parseDate(((ScheduledServiceOnceResource) model).getStartDate(),
              ((ScheduledServiceOnceResource) model).getStartTime());

      validateStartDate(((ScheduledServiceOnceResource) model).getStartDate());
      validateTime("startTime", date);

      schedule =
          new Once(parseDate(((ScheduledServiceOnceResource) model).getStartDate(),
              ((ScheduledServiceOnceResource) model).getStartTime()));
    }
    else {
      schedule = new Manual();
    }

    return schedule;
  }

  public <T> ScheduledServiceBaseResource getServiceRestModel(TaskConfiguration taskConfiguration, Schedule schedule) {
    ScheduledServiceBaseResource resource = null;
    if (Now.class.isAssignableFrom(schedule.getClass())
        || Manual.class.isAssignableFrom(schedule.getClass())) {
      resource = new ScheduledServiceBaseResource();
    }
    else if (Once.class.isAssignableFrom(schedule.getClass())) {
      resource = new ScheduledServiceOnceResource();

      Once taskSchedule = (Once) schedule;
      ScheduledServiceOnceResource res = (ScheduledServiceOnceResource) resource;

      res.setStartDate(formatDate(taskSchedule.getStartAt()));
      res.setStartTime(formatTime(taskSchedule.getStartAt()));
    }
    else if (Hourly.class.isAssignableFrom(schedule.getClass())) {
      resource = new ScheduledServiceHourlyResource();

      Hourly taskSchedule = (Hourly) schedule;
      ScheduledServiceHourlyResource res = (ScheduledServiceHourlyResource) resource;

      res.setStartDate(formatDate(taskSchedule.getStartAt()));
      res.setStartTime(formatTime(taskSchedule.getStartAt()));
    }
    else if (Daily.class.isAssignableFrom(schedule.getClass())) {
      resource = new ScheduledServiceDailyResource();

      Daily taskSchedule = (Daily) schedule;
      ScheduledServiceDailyResource res = (ScheduledServiceDailyResource) resource;

      res.setStartDate(formatDate(taskSchedule.getStartAt()));
      res.setRecurringTime(formatTime(taskSchedule.getStartAt()));
    }
    else if (Weekly.class.isAssignableFrom(schedule.getClass())) {
      resource = new ScheduledServiceWeeklyResource();

      Weekly taskSchedule = (Weekly) schedule;
      ScheduledServiceWeeklyResource res = (ScheduledServiceWeeklyResource) resource;

      res.setStartDate(formatDate(taskSchedule.getStartAt()));
      res.setRecurringTime(formatTime(taskSchedule.getStartAt()));
      res.setRecurringDay(formatRecurringDayOfWeek(taskSchedule.getDaysToRun()));
    }
    else if (Monthly.class.isAssignableFrom(schedule.getClass())) {
      resource = new ScheduledServiceMonthlyResource();

      Monthly taskSchedule = (Monthly) schedule;
      ScheduledServiceMonthlyResource res = (ScheduledServiceMonthlyResource) resource;

      res.setStartDate(formatDate(taskSchedule.getStartAt()));
      res.setRecurringTime(formatTime(taskSchedule.getStartAt()));
      res.setRecurringDay(formatRecurringDayOfMonth(taskSchedule.getDaysToRun()));
    }
    else if (Cron.class.isAssignableFrom(schedule.getClass())) {
      resource = new ScheduledServiceAdvancedResource();

      Cron taskSchedule = (Cron) schedule;
      ScheduledServiceAdvancedResource res = (ScheduledServiceAdvancedResource) resource;

      res.setCronCommand(taskSchedule.getCronExpression());
    }

    if (resource != null) {
      resource.setId(taskConfiguration.getId());
      resource.setEnabled(taskConfiguration.isEnabled());
      resource.setName(taskConfiguration.getName());
      resource.setSchedule(getScheduleShortName(schedule));
      resource.setTypeId(taskConfiguration.getTypeId());
      resource.setProperties(formatServiceProperties(taskConfiguration.asMap()));
      resource.setAlertEmail(taskConfiguration.getAlertEmail());
    }

    return resource;
  }

  /**
   * Returns last run time (when task started). Restlet1x API has slight difference of how this value is interpreted
   * from current Tasks API, as it includes the current run if task in question is running.
   */
  protected <T> Date getLastRunTime(final TaskInfo<T> task) {
    if (State.RUNNING == task.getCurrentState().getState()) {
      return task.getCurrentState().getRunStarted();
    }
    else if (task.getLastRunState() != null) {
      return task.getLastRunState().getRunStarted();
    }
    return null;
  }

  /**
   * Returns next run time of the task.
   */
  @Nullable
  protected <T> Date getNextRunTime(final TaskInfo<T> task) {
    return task.getCurrentState().getNextRun();
  }

  protected String getLastRunResult(TaskInfo<?> task) {
    String lastRunResult = "n/a";
    try {
      LastRunState lastRunState = task.getLastRunState();

      if (lastRunState != null) {
        lastRunResult = "n/a";
        switch (lastRunState.getEndState()) {
          case OK:
            lastRunResult = "Ok";
            break;
          case CANCELED:
            lastRunResult = "Canceled";
            break;
          case FAILED:
            lastRunResult = "Error";
        }
        if (lastRunState.getRunDuration() != 0) {
          long milliseconds = lastRunState.getRunDuration();
          int hours = (int) ((milliseconds / 1000) / 3600);
          int minutes = (int) ((milliseconds / 1000) / 60 - hours * 60);
          int seconds = (int) ((milliseconds / 1000) % 60);

          lastRunResult += " [";
          if (hours != 0) {
            lastRunResult += hours;
            lastRunResult += "h";
          }
          if (minutes != 0 || hours != 0) {
            lastRunResult += minutes;
            lastRunResult += "m";
          }
          lastRunResult += seconds;
          lastRunResult += "s";
          lastRunResult += "]";
        }
      }
    } catch (IllegalStateException e) {
      // nop
    }
    return lastRunResult;
  }

  protected String getState(final TaskInfo info) {
    if (State.RUNNING != info.getCurrentState().getState()) {
      return info.getCurrentState().getState().name();
    }
    else {
      // is running
      switch (info.getCurrentState().getRunState()) {
        case BLOCKED:
          return "SLEEPING";
        case CANCELED:
          return "CANCELLING";
        default:
          return "RUNNING";
      }
    }
  }

  protected String getReadableState(final TaskInfo info) {
    switch (info.getCurrentState().getState()) {
      case WAITING:
        return "Waiting";
      case DONE:
        return "Done";
      case RUNNING: {
        switch (info.getCurrentState().getRunState()) {
          case BLOCKED:
            return "Blocked";
          case CANCELED:
            return "Cancelling";
          default:
            return "Running";
        }
      }
      default:
        return String.valueOf(info.getCurrentState().getState());
    }
  }

}
