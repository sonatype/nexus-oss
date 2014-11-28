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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Schedule support class.
 */
public abstract class Schedule
{
  protected final Map<String, String> properties;

  public Schedule(final String type) {
    checkNotNull(type);
    this.properties = Maps.newHashMap();
    this.properties.put("schedule.type", type);
  }

  public String getType() {
    return properties.get("schedule.type");
  }

  public Map<String, String> asMap() {
    return Collections.unmodifiableMap(properties);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "properties=" + properties +
        '}';
  }

  // ==

  public static String dateToString(final Date date) {
    return new DateTime(date).toString();
  }

  public static Date stringToDate(final String string) {
    return DateTime.parse(string).toDate();
  }

  public static String setToList(Set<Integer> set) {
    return Joiner.on(',').join(Collections2.transform(set, new Function<Integer, String>()
    {
      @Override
      public String apply(final Integer input) {
        return input.toString();
      }
    }));
  }

  public static Set<Integer> listToSet(String value) {
    final Collection<Integer> enums = Collections2.transform(Splitter.on(',').splitToList(value),
        new Function<String, Integer>()
        {
          @Override
          public Integer apply(final String input) {
            return Integer.parseInt(input);
          }
        });
    return Sets.newHashSet(enums);
  }
}
