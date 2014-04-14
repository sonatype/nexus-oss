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
package org.sonatype.nexus.analytics;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Analytics event data.
 *
 * @since 3.0
 */
public class EventData
{
  private String type;

  private Long timestamp;

  private String userId;

  private String sessionId;

  private final Map<String,Object> attributes = Maps.newHashMap();

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(final Long timestamp) {
    this.timestamp = timestamp;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(final String userId) {
    this.userId = userId;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(final String sessionId) {
    this.sessionId = sessionId;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "type='" + type + '\'' +
        ", timestamp=" + timestamp +
        ", userId='" + userId + '\'' +
        ", sessionId='" + sessionId + '\'' +
        ", attributes=" + attributes +
        '}';
  }
}
