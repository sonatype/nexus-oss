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
package org.sonatype.nexus.scheduling.schedule;

import java.util.Date;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Schedule that repeats on same days of a week repeatedly.
 */
public class Weekly
    extends Schedule
{
  // TODO: make this type safe by using enum?
  public static final Integer SUN = Integer.valueOf(1);

  public static final Integer MON = Integer.valueOf(2);

  public static final Integer TUE = Integer.valueOf(3);

  public static final Integer WED = Integer.valueOf(4);

  public static final Integer THU = Integer.valueOf(5);

  public static final Integer FRI = Integer.valueOf(6);

  public static final Integer SAT = Integer.valueOf(7);

  public Weekly(final Date startAt, final Set<Integer> daysToRun) {
    super("weekly");
    checkNotNull(startAt);
    checkNotNull(daysToRun);
    checkArgument(!daysToRun.isEmpty(), "No days of week set to run");
    for (Integer integer : daysToRun) {
      checkArgument(integer >= SUN && integer <= SAT, "Invalid weekly argument: %s", daysToRun);
    }
    properties.put("schedule.startAt", dateToString(startAt));
    properties.put("schedule.daysToRun", setToCsv(daysToRun));
  }

  public Date getStartAt() {
    return stringToDate(properties.get("schedule.startAt"));
  }

  public Set<Integer> getDaysToRun() {
    return csvToSet(properties.get("schedule.daysToRun"));
  }
}