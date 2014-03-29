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
package org.sonatype.nexus.timeline.internal;

import java.util.Collections;
import java.util.Map;

import org.sonatype.nexus.timeline.Entry;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.kazuki.v0.store.schema.model.Attribute;
import io.kazuki.v0.store.schema.model.Attribute.Type;
import io.kazuki.v0.store.schema.model.Schema;

/**
 * Timeline entry persisted into KZ.
 *
 * @since 3.0
 */
public final class EntryRecord
    implements Entry
{
  public static final String SCHEMA_NAME = "timeline-entry";

  public static final Schema SCHEMA = new Schema(
      ImmutableList.of(
          new Attribute("timestamp", Type.U64, null, false),
          new Attribute("type", Type.UTF8_SMALLSTRING, null, false),
          new Attribute("subType", Type.UTF8_SMALLSTRING, null, false),
          new Attribute("data", Type.MAP, null, false)
      )
  );

  private final long timestamp;

  private final String type;

  private final String subType;

  private final Map<String, String> data;

  @JsonCreator
  public EntryRecord(@JsonProperty("timestamp") final long timestamp,
                     @JsonProperty("type") final String type,
                     @JsonProperty("subType") final String subType,
                     @JsonProperty("data") final Map<String, String> data)
  {
    if (type == null || type.trim().length() == 0 || subType == null || subType.trim().length() == 0) {
      throw new IllegalArgumentException("Entry type or subType must not be blank/null");
    }
    this.timestamp = timestamp;
    this.type = type;
    this.subType = subType;
    this.data = Maps.newHashMap();
    if (data != null) {
      this.data.putAll(data);
    }
  }

  @Override
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getSubType() {
    return subType;
  }

  @Override
  public Map<String, String> getData() {
    return data;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "timestamp=" + timestamp +
        ", type='" + type + '\'' +
        ", subType='" + subType + '\'' +
        ", data=" + data +
        '}';
  }
}
