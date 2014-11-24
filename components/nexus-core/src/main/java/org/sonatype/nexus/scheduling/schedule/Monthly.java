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
 * Schedule that repeats on same days of a month repeatedly.
 */
public class Monthly
    extends Schedule
{
  public static final Integer LAST_DAY_OF_MONTH = 999;

  public Monthly(final Date startAt, final Set<Integer> daysToRun) {
    super("monthly");
    checkNotNull(startAt);
    checkNotNull(daysToRun);
    for (Integer integer : daysToRun) {
      checkArgument((integer >= 1 && integer <= 12) || integer == LAST_DAY_OF_MONTH, "Invalid monthly argument: %s",
          daysToRun);
    }
    properties.put("schedule.startAt", dateToString(startAt));
    properties.put("schedule.daysToRun", setToList(daysToRun));
  }

  public Date getStartAt() {
    return stringToDate(properties.get("schedule.startAt"));
  }

  public Set<Integer> getDaysToRun() {
    return listToSet(properties.get("schedule.daysToRun"));
  }
}