/*
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

package org.sonatype.nexus.quartz.internal.store;

import java.util.List;
import java.util.Set;

import org.sonatype.nexus.quartz.internal.store.TriggerWrapper.State;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import io.kazuki.v0.store.KazukiException;
import io.kazuki.v0.store.keyvalue.KeyValuePair;
import org.quartz.JobKey;
import org.quartz.Matcher;
import org.quartz.TriggerKey;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Static helper for common predicates.
 *
 * @since 3.0
 */
public class Predicates
{
  private Predicates() {
    // no instances
  }

  // ==

  public static <T> KeyValuePair<T> single(final List<KeyValuePair<T>> list) throws KazukiException {
    if (list.isEmpty()) {
      return null;
    }
    else if (list.size() == 1) {
      return list.get(0);
    }
    else {
      throw new KazukiException(
          "Artificial key mismatch? List expected to have one element, but it's size: " + list.size());
    }
  }

  // JobDetailRecord

  public static Predicate<JobDetailRecord> byJobKey(final JobKey key) {
    checkNotNull(key);
    return new Predicate<JobDetailRecord>()
    {
      @Override
      public boolean apply(final JobDetailRecord input) {
        return key.getName().equals(input.getName()) && key.getGroup().equals(input.getGroup());
      }
    };
  }

  public static Predicate<JobDetailRecord> byJobDetailGroup(final String group) {
    checkNotNull(group);
    return new Predicate<JobDetailRecord>()
    {
      @Override
      public boolean apply(final JobDetailRecord input) {
        return group.equals(input.getGroup());
      }
    };
  }

  public static Predicate<JobDetailRecord> byJobKeyMatcher(final Matcher<JobKey> matcher) {
    checkNotNull(matcher);
    return new Predicate<JobDetailRecord>()
    {
      @Override
      public boolean apply(final JobDetailRecord input) {
        return matcher.isMatch(JobKey.jobKey(input.getName(), input.getGroup()));
      }
    };
  }

  public static Predicate<JobDetailRecord> byJobClass(final String jobClass) {
    checkNotNull(jobClass);
    return new Predicate<JobDetailRecord>()
    {
      @Override
      public boolean apply(final JobDetailRecord input) {
        return jobClass.equals(input.getJobClass());
      }
    };
  }

  // TriggerRecord

  public static Predicate<TriggerRecord> byTriggerKey(final TriggerKey key) {
    checkNotNull(key);
    return new Predicate<TriggerRecord>()
    {
      @Override
      public boolean apply(final TriggerRecord input) {
        return key.getName().equals(input.getName()) && key.getGroup().equals(input.getGroup());
      }
    };
  }

  public static Predicate<TriggerRecord> byTriggerKeys(final Set<TriggerKey> keys) {
    checkNotNull(keys);
    return new Predicate<TriggerRecord>()
    {
      @Override
      public boolean apply(final TriggerRecord input) {
        return keys.contains(TriggerKey.triggerKey(input.getName(), input.getGroup()));
      }
    };
  }

  public static Predicate<TriggerRecord> byTriggerGroup(final String group) {
    checkNotNull(group);
    return new Predicate<TriggerRecord>()
    {
      @Override
      public boolean apply(final TriggerRecord input) {
        return group.equals(input.getGroup());
      }
    };
  }

  public static Predicate<TriggerRecord> byTriggerKeyMatcher(final Matcher<TriggerKey> matcher) {
    checkNotNull(matcher);
    return new Predicate<TriggerRecord>()
    {
      @Override
      public boolean apply(final TriggerRecord input) {
        return matcher.isMatch(TriggerKey.triggerKey(input.getName(), input.getGroup()));
      }
    };
  }

  public static Predicate<TriggerRecord> byJobDetailRef(final JobKey key) {
    checkNotNull(key);
    return new Predicate<TriggerRecord>()
    {
      @Override
      public boolean apply(final TriggerRecord input) {
        return key.getName().equals(input.getJobName()) && key.getGroup().equals(input.getJobGroup());
      }
    };
  }

  public static Predicate<TriggerRecord> byCalendarRef(final String calendarName) {
    checkNotNull(calendarName);
    return new Predicate<TriggerRecord>()
    {
      @Override
      public boolean apply(final TriggerRecord input) {
        return calendarName.equals(input.getCalendarName());
      }
    };
  }

  public static Predicate<TriggerRecord> inAnyStateOf(final State... states) {
    final Set<State> stateSet = Sets.newHashSet(states);
    return new Predicate<TriggerRecord>()
    {
      @Override
      public boolean apply(final TriggerRecord input) {
        return stateSet.contains(input.getState());
      }
    };
  }

  // CalendarRecord

  public static Predicate<CalendarRecord> byCalendarName(final String calendarName) {
    checkNotNull(calendarName);
    return new Predicate<CalendarRecord>()
    {
      @Override
      public boolean apply(final CalendarRecord input) {
        return calendarName.equals(input.getName()) && CalendarRecord.GROUP.equals(input.getGroup());
      }
    };
  }
}
