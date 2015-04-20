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
package org.sonatype.nexus.repository.httpclient;

import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * HTTP client connection configuration.
 *
 * @since 3.0
 */
public class ConnectionConfig
{
  @Nullable
  private String userAgentCustomisation;

  @Nullable
  private String urlParameters;

  /**
   * Timeout milliseconds.
   */
  @Min(0L)
  @Max(3600000L) // 1 hour
  private Integer timeout;

  @Min(0L)
  @Max(10L)
  private Integer retries;

  private Boolean blocked;

  private Boolean autoBlock;

  private Boolean useTrustStore;

  @Nullable
  public String getUserAgentCustomisation() {
    return userAgentCustomisation;
  }

  public void setUserAgentCustomisation(final @Nullable String userAgentCustomisation) {
    this.userAgentCustomisation = userAgentCustomisation;
  }

  @Nullable
  public String getUrlParameters() {
    return urlParameters;
  }

  public void setUrlParameters(final @Nullable String urlParameters) {
    this.urlParameters = urlParameters;
  }

  public Integer getTimeout() {
    return timeout;
  }

  public void setTimeout(final Integer timeout) {
    this.timeout = timeout;
  }

  public Integer getRetries() {
    return retries;
  }

  public void setRetries(final Integer retries) {
    this.retries = retries;
  }

  public Boolean isBlocked() {
    return blocked;
  }

  public void setBlocked(final Boolean blocked) {
    this.blocked = blocked;
  }

  public Boolean shouldAutoBlock() {
    return autoBlock;
  }

  public void setAutoBlock(final Boolean autoBlock) {
    this.autoBlock = autoBlock;
  }

  public Boolean getUseTrustStore() {
    return useTrustStore;
  }

  public void setUseTrustStore(final Boolean useTrustStore) {
    this.useTrustStore = useTrustStore;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "userAgentCustomisation='" + userAgentCustomisation + '\'' +
        ", urlParameters='" + urlParameters + '\'' +
        ", timeout=" + timeout +
        ", retries=" + retries +
        ", blocked=" + blocked +
        ", autoBlock=" + autoBlock +
        ", useTrustStore=" + useTrustStore +
        '}';
  }
}
