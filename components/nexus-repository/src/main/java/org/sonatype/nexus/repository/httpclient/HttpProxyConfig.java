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
 * HTTP client HTTP proxy configuration.
 *
 * @since 3.0
 */
public class HttpProxyConfig
{
  private String hostname;

  private Integer port;

  private AuthenticationConfig authenticationConfig;

  public String getHostname() {
    return hostname;
  }

  public void setHostname(final String hostname) {
    this.hostname = hostname;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(final Integer port) {
    this.port = port;
  }

  public AuthenticationConfig getAuthenticationConfig() {
    return authenticationConfig;
  }

  public void setAuthenticationConfig(final AuthenticationConfig authenticationConfig) {
    this.authenticationConfig = authenticationConfig;
  }
}
