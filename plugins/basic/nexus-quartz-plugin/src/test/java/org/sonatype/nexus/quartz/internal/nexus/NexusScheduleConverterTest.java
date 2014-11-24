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

import java.util.Date;

import org.sonatype.nexus.scheduling.schedule.Monthly;
import org.sonatype.nexus.scheduling.schedule.Weekly;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.quartz.CronTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * UT of {@link NexusScheduleConverter}
 */
public class NexusScheduleConverterTest
    extends TestSupport
{
  final NexusScheduleConverter converter = new NexusScheduleConverter();

  @Test
  public void weekly1() {
    final Weekly weekly = new Weekly(new Date(), Sets.newSet(Weekly.SAT));
    final TriggerBuilder triggerBuilder = converter.toTrigger(weekly);
    final Trigger trigger = triggerBuilder.build();
    assertThat(trigger, instanceOf(CronTrigger.class));
    final String cronExpression = ((CronTrigger) trigger).getCronExpression();
    assertThat(cronExpression, equalTo("0 0 0 ? * SAT"));
  }

  @Test
  public void weekly2() {
    final Weekly weekly = new Weekly(new Date(), Sets.newSet(Weekly.SAT, Weekly.FRI));
    final TriggerBuilder triggerBuilder = converter.toTrigger(weekly);
    final Trigger trigger = triggerBuilder.build();
    assertThat(trigger, instanceOf(CronTrigger.class));
    final String cronExpression = ((CronTrigger) trigger).getCronExpression();
    assertThat(cronExpression, equalTo("0 0 0 ? * FRI,SAT"));
  }

  @Test
  public void montlhy1() {
    final Monthly monthly = new Monthly(new Date(), Sets.newSet(2));
    final TriggerBuilder triggerBuilder = converter.toTrigger(monthly);
    final Trigger trigger = triggerBuilder.build();
    assertThat(trigger, instanceOf(CronTrigger.class));
    final String cronExpression = ((CronTrigger) trigger).getCronExpression();
    assertThat(cronExpression, equalTo("0 0 0 2 * ?"));
  }

  @Test
  public void montlhy2() {
    final Monthly monthly = new Monthly(new Date(), Sets.newSet(1, 2, 3, 10, 11, 12));
    final TriggerBuilder triggerBuilder = converter.toTrigger(monthly);
    final Trigger trigger = triggerBuilder.build();
    assertThat(trigger, instanceOf(CronTrigger.class));
    final String cronExpression = ((CronTrigger) trigger).getCronExpression();
    assertThat(cronExpression, equalTo("0 0 0 1,2,3,10,11,12 * ?"));
  }
}
