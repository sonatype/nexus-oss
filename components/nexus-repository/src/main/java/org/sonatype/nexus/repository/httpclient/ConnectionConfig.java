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

  private Boolean blocked;

  private Boolean autoBlock;

  private Boolean useTrustStore;

  public String getUserAgentCustomisation() {
    return userAgentCustomisation;
  }

  public void setUserAgentCustomisation(final String userAgentCustomisation) {
    this.userAgentCustomisation = userAgentCustomisation;
  }

  public String getUrlParameters() {
    return urlParameters;
  }

  public void setUrlParameters(final String urlParameters) {
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
}
