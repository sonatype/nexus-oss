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
package org.sonatype.nexus.quartz.internal.nexus;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.scheduling.schedule.Cron;
import org.sonatype.nexus.scheduling.schedule.Daily;
import org.sonatype.nexus.scheduling.schedule.Hourly;
import org.sonatype.nexus.scheduling.schedule.Manual;
import org.sonatype.nexus.scheduling.schedule.Monthly;
import org.sonatype.nexus.scheduling.schedule.Now;
import org.sonatype.nexus.scheduling.schedule.Once;
import org.sonatype.nexus.scheduling.schedule.Schedule;
import org.sonatype.nexus.scheduling.schedule.Weekly;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import org.quartz.CronScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import static org.quartz.TriggerBuilder.newTrigger;
import static org.sonatype.nexus.scheduling.schedule.Schedule.csvToSet;
import static org.sonatype.nexus.scheduling.schedule.Schedule.stringToDate;

/**
 * Converter for NX and QZ schedules. This converter cannot convert ANY QZ trigger, just  those created by this
 * same class as on Schedule to Trigger direction it "hints" extra information into trigger map (that is used to
 * reconstruct schedule).
 *
 * @since 3.0
 */
// TODO: is this really a component?
@Singleton
@Named
public class NexusScheduleConverter
    extends ComponentSupport
{
  private static final Date FAR_FUTURE = new Date(Long.MAX_VALUE);

  /**
   * Creates Quartz trigger from schedule.
   */
  public TriggerBuilder toTrigger(final Schedule schedule) {
    final TriggerBuilder triggerBuilder;
    if (schedule instanceof Cron) {
      final Cron s = (Cron) schedule;
      triggerBuilder = newTrigger().startAt(s.getStartAt())
          .withSchedule(CronScheduleBuilder.cronSchedule(s.getCronExpression()));
    }
    else if (schedule instanceof Now) {
      triggerBuilder = newTrigger().startNow();
    }
    else if (schedule instanceof Once) {
      final Once s = (Once) schedule;
      triggerBuilder = newTrigger().startAt(s.getStartAt());
    }
    else if (schedule instanceof Hourly) {
      final Hourly s = (Hourly) schedule;
      triggerBuilder = newTrigger().startAt(s.getStartAt()).withSchedule(SimpleScheduleBuilder.repeatHourlyForever(1));
    }
    else if (schedule instanceof Daily) {
      final Daily s = (Daily) schedule;
      triggerBuilder = newTrigger().startAt(s.getStartAt()).withSchedule(SimpleScheduleBuilder.repeatHourlyForever(24));
    }
    else if (schedule instanceof Weekly) {
      final Weekly s = (Weekly) schedule;
      final String daysToRun = Joiner.on(",").join(Iterables.transform(s.getDaysToRun(), new Function<Integer, String>()
      {
        @Override
        public String apply(final Integer integer) {
          switch (integer) {
            case 1:
              return "SUN";
            case 2:
              return "MON";
            case 3:
              return "TUE";
            case 4:
              return "WED";
            case 5:
              return "THU";
            case 6:
              return "FRI";
            case 7:
              return "SAT";
            default:
              throw new IllegalArgumentException("Unknown day of week: " + integer);
          }
        }
      }));
      triggerBuilder = newTrigger().startAt(s.getStartAt())
          .withSchedule(CronScheduleBuilder.cronSchedule(cronTimeParts(s.getStartAt()) + " ? * " + daysToRun));
    }
    else if (schedule instanceof Monthly) {
      final Monthly s = (Monthly) schedule;
      final Set<Integer> daysToRun = s.getDaysToRun();
      final boolean lastDayOfMonth = daysToRun.remove(Monthly.LAST_DAY_OF_MONTH);
      // TODO: quartz does not support use of "L" along with days!
      if (!lastDayOfMonth) {
        final String daysToRunStr = Joiner.on(",").join(Iterables.transform(daysToRun, new Function<Integer, String>()
        {
          @Override
          public String apply(final Integer integer) {
            return Integer.toString(integer);
          }
        }));
        triggerBuilder = newTrigger().startAt(s.getStartAt())
            .withSchedule(
                CronScheduleBuilder.cronSchedule(cronTimeParts(s.getStartAt()) + " " + daysToRunStr + " * ?"));
      }
      else {
        triggerBuilder = newTrigger().startAt(s.getStartAt())
            .withSchedule(CronScheduleBuilder.cronSchedule(cronTimeParts(s.getStartAt()) + " L * ?"));
      }
    }
    else if (schedule instanceof Manual) {
      final Manual s = (Manual) schedule;
      // TODO: this looks awkward, but is needed to maintain job:trigger 1:1 ratio
      // TODO: Investigate other solutions? (ie. durable job w/o trigger?)
      // that would introduce lot of exceptional branches handling trigger==null in code
      triggerBuilder = newTrigger().startAt(FAR_FUTURE);
    }
    else {
      throw new IllegalArgumentException("Schedule unknown:" + schedule.getType());
    }
    // make type as description
    triggerBuilder.withDescription(schedule.getType());

    // store all the schedule properties for opposite conversion
    for (Map.Entry<String, String> entry : schedule.asMap().entrySet()) {
      triggerBuilder.usingJobData(entry.getKey(), entry.getValue());
    }

    return triggerBuilder;
  }

  /**
   * Converts Quartz Trigger into NX Schedule. It can convert only triggers that are created from Schedules in the
   * first
   * place.
   */
  public Schedule toSchedule(final Trigger trigger) {
    final String type = trigger.getJobDataMap().getString("schedule.type");
    if ("cron".equals(type)) {
      final Date startAt = stringToDate(trigger.getJobDataMap().getString("schedule.startAt"));
      final String cronExpression = trigger.getJobDataMap().getString("schedule.cronExpression");
      return new Cron(startAt, cronExpression);
    }
    else if ("now".equals(type)) {
      return new Now();
    }
    else if ("once".equals(type)) {
      final Date startAt = stringToDate(trigger.getJobDataMap().getString("schedule.startAt"));
      return new Once(startAt);
    }
    else if ("hourly".equals(type)) {
      final Date startAt = stringToDate(trigger.getJobDataMap().getString("schedule.startAt"));
      return new Hourly(startAt);
    }
    else if ("daily".equals(type)) {
      final Date startAt = stringToDate(trigger.getJobDataMap().getString("schedule.startAt"));
      return new Daily(startAt);
    }
    else if ("weekly".equals(type)) {
      final Date startAt = stringToDate(trigger.getJobDataMap().getString("schedule.startAt"));
      final Set<Integer> daysToRun = csvToSet(trigger.getJobDataMap().getString("schedule.daysToRun"));
      return new Weekly(startAt, daysToRun);
    }
    else if ("monthly".equals(type)) {
      final Date startAt = stringToDate(trigger.getJobDataMap().getString("schedule.startAt"));
      final Set<Integer> daysToRun = csvToSet(trigger.getJobDataMap().getString("schedule.daysToRun"));
      return new Monthly(startAt, daysToRun);
    }
    else if ("manual".equals(type)) {
      return new Manual();
    }
    else {
      throw new IllegalArgumentException("Trigger unknown key: '" + trigger.getKey() + "', type: '" + type + "'");
    }
  }

  private String cronTimeParts(final Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    return "0 " + cal.get(Calendar.MINUTE) + " " + cal.get(Calendar.HOUR_OF_DAY);
  }

}
