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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.sonatype.nexus.validation.constraint.PortNumber;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * HTTP client HTTP proxy configuration.
 *
 * @since 3.0
 */
public class HttpProxyConfig
{
  @NotEmpty
  private String hostname;

  @NotNull
  @PortNumber
  private Integer port;

  @Valid
  private AuthenticationConfig authentication;

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

  public AuthenticationConfig getAuthentication() {
    return authentication;
  }

  public void setAuthentication(final AuthenticationConfig authentication) {
    this.authentication = authentication;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "hostname='" + hostname + '\'' +
        ", port=" + port +
        ", authentication=" + authentication +
        '}';
  }
}
