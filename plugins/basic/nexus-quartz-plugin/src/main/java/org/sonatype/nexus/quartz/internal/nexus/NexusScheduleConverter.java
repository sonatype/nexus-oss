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

import java.util.Map;

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

import com.google.common.collect.Sets;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import static org.quartz.TriggerBuilder.newTrigger;

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
{
  /**
   * Creates Quartz trigger from schedule.
   */
  public Trigger toTrigger(final Schedule schedule) {
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
      // TODO: create cron expr would be the best
      triggerBuilder = newTrigger().startAt(s.getStartAt()).withSchedule(CronScheduleBuilder.cronSchedule(""));
    }
    else if (schedule instanceof Monthly) {
      final Monthly s = (Monthly) schedule;
      // TODO: create cron expr would be the best
      triggerBuilder = newTrigger().startAt(s.getStartAt()).withSchedule(CronScheduleBuilder.cronSchedule(""));
    }
    else if (schedule instanceof Manual) {
      final Manual s = (Manual) schedule;
      triggerBuilder = newTrigger().startAt(null); // TODO: a far future?
    }
    else {
      throw new IllegalArgumentException("Schedule unknown:" + schedule.getType());
    }
    // make type as description
    triggerBuilder.withDescription(schedule.getType());

    final Trigger trigger = triggerBuilder.build();

    // store all the schedule properties for opposite conversion
    for (Map.Entry<String, String> entry : schedule.asMap().entrySet()) {
      trigger.getJobDataMap().put(entry.getKey(), entry.getValue());
    }

    return triggerBuilder.build();
  }

  /**
   * Converts Quartz Trigger into NX Schedule. It can convert only triggers that are created from Schedules in the
   * first
   * place.
   */
  public Schedule toSchedule(final Trigger trigger) {
    final String type = trigger.getJobDataMap().getString("schedule.type");
    if ("cron".equals(type)) {
      return new Cron(trigger.getStartTime(), ((CronTrigger) trigger).getCronExpression());
    }
    else if ("now".equals(type)) {
      return new Now();
    }
    else if ("once".equals(type)) {
      return new Once(trigger.getStartTime());
    }
    else if ("daily".equals(type)) {
      return new Daily(trigger.getStartTime());
    }
    else if ("weekly".equals(type)) {
      // TODO: ?
      return new Weekly(trigger.getStartTime(), Sets.<Integer>newHashSet());
    }
    else if ("monthly".equals(type)) {
      // TODO: ?
      return new Monthly(trigger.getStartTime(), Sets.<Integer>newHashSet());
    }
    else if ("manual".equals(type)) {
      return new Manual();
    }
    else {
      throw new IllegalArgumentException("Trigger unknown:" + trigger.getKey());
    }
  }
}