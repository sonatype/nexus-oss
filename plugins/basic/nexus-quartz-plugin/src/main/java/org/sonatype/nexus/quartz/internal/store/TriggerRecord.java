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

import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nullable;

import org.sonatype.nexus.quartz.internal.store.TriggerWrapper.State;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import io.kazuki.v0.store.schema.TypeValidation;
import io.kazuki.v0.store.schema.model.Attribute;
import io.kazuki.v0.store.schema.model.Attribute.Type;
import io.kazuki.v0.store.schema.model.Schema;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * OperableTrigger record.
 *
 * @since 3.0
 */
public class TriggerRecord
{
  public static final TypedSchema<TriggerRecord> SCHEMA = new TypedSchema<>(
      "quartz-trigger",
      TypeValidation.STRICT,
      TriggerRecord.class,
      new Schema(ImmutableList.of(
          new Attribute("name", Type.UTF8_SMALLSTRING, null, false),
          new Attribute("group", Type.UTF8_SMALLSTRING, null, false),
          new Attribute("quartzType", Type.UTF8_SMALLSTRING, null, false),
          new Attribute("data", Type.MAP, null, false),
          new Attribute("state", Type.ENUM, Arrays.<Object>asList(State.values()), false),
          new Attribute("jobName", Type.UTF8_SMALLSTRING, null, true),
          new Attribute("jobGroup", Type.UTF8_SMALLSTRING, null, true),
          new Attribute("calendarName", Type.UTF8_SMALLSTRING, null, true)
      ), null)
  );

  private final String name;

  private final String group;

  private final String quartzType;

  private final Map<String, Object> data;

  private final State state;

  private final String jobName;

  private final String jobGroup;

  private final String calendarName;

  @JsonCreator
  public TriggerRecord(@JsonProperty("name") final String name,
                       @JsonProperty("group") final String group,
                       @JsonProperty("quartzType") final String quartzType,
                       @JsonProperty("data") final Map<String, Object> data,
                       @JsonProperty("state") final State state,
                       @Nullable @JsonProperty("jobName") final String jobName,
                       @Nullable @JsonProperty("jobGroup") final String jobGroup,
                       @Nullable @JsonProperty("calendarName") final String calendarName)
  {
    this.name = checkNotNull(name);
    this.group = checkNotNull(group);
    this.quartzType = checkNotNull(quartzType);
    this.data = checkNotNull(data);
    this.state = checkNotNull(state);
    this.jobName = jobName;
    this.jobGroup = jobGroup;
    this.calendarName = calendarName;
  }

  public String getName() {
    return name;
  }

  public String getGroup() {
    return group;
  }

  public String getQuartzType() {
    return quartzType;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public State getState() { return state; }

  public String getJobName() {
    return jobName;
  }

  public String getJobGroup() {
    return jobGroup;
  }

  public String getCalendarName() {
    return calendarName;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TriggerRecord)) {
      return false;
    }

    TriggerRecord that = (TriggerRecord) o;

    if (!group.equals(that.group)) {
      return false;
    }
    if (!name.equals(that.name)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + group.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "name='" + name + '\'' +
        ", group='" + group + '\'' +
        ", quartzType='" + quartzType + '\'' +
        ", state=" + state +
        ", jobName='" + jobName + '\'' +
        ", jobGroup='" + jobGroup + '\'' +
        ", calendarName='" + calendarName + '\'' +
        '}';
  }
}
