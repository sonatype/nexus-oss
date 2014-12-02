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
package org.sonatype.nexus.component.source.http;

/**
 * HTTP client connection configuration.
 *
 * @since 3.0
 */
public class ConnectionConfig
{
  private String userAgentCustomisation;

  private String urlParameters;

  private Integer timeout;

  private Integer retries;

  private Boolean useTrustStore;

  public String getUserAgentCustomisation() {
    return userAgentCustomisation;
  }

  public String getUrlParameters() {
    return urlParameters;
  }

  public Integer getTimeout() {
    return timeout;
  }

  public Integer getRetries() {
    return retries;
  }

  public Boolean getUseTrustStore() {
    return useTrustStore;
  }

  public ConnectionConfig withUserAgentCustomisation(final String userAgentCustomisation) {
    this.userAgentCustomisation = userAgentCustomisation;
    return this;
  }

  public ConnectionConfig withUrlParameters(final String urlParameters) {
    this.urlParameters = urlParameters;
    return this;
  }

  public ConnectionConfig withTimeout(final Integer timeout) {
    this.timeout = timeout;
    return this;
  }

  public ConnectionConfig withRetries(final Integer retries) {
    this.retries = retries;
    return this;
  }

  public ConnectionConfig withUseTrustStore(final Boolean useTrustStore) {
    this.useTrustStore = useTrustStore;
    return this;
  }

}
