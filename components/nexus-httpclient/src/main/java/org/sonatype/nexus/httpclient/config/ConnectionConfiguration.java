/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.httpclient.config;

import javax.annotation.Nullable;

import org.sonatype.sisu.goodies.common.Time;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Connection configuration.
 *
 * @since 3.0
 */
public class ConnectionConfiguration
    implements Cloneable
{
  @Nullable
  @JsonDeserialize(using = SecondsDeserializer.class)
  private Time timeout;

  @Nullable
  private Integer maximumRetries;

  @Nullable
  private String userAgentSuffix;

  // TODO: query-string

  @Nullable
  private Boolean useTrustStore;

  @Nullable
  public Time getTimeout() {
    return timeout;
  }

  public void setTimeout(@Nullable final Time timeout) {
    this.timeout = timeout;
  }

  @Nullable
  public Integer getMaximumRetries() {
    return maximumRetries;
  }

  public void setMaximumRetries(@Nullable final Integer maximumRetries) {
    this.maximumRetries = maximumRetries;
  }

  @Nullable
  public String getUserAgentSuffix() { return userAgentSuffix; }

  public void setUserAgentSuffix(@Nullable final String userAgentSuffix) { this.userAgentSuffix = userAgentSuffix; }

  @Nullable
  public Boolean getUseTrustStore() { return useTrustStore; }

  public void setUseTrustStore(@Nullable final Boolean useTrustStore) { this.useTrustStore = useTrustStore; }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "timeout=" + timeout +
        ", maximumRetries=" + maximumRetries +
        ", userAgentSuffix=" + userAgentSuffix +
        ", useTrustStore=" + useTrustStore +
        '}';
  }
}
