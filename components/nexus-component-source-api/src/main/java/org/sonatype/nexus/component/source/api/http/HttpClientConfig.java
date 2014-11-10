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
package org.sonatype.nexus.component.source.api.http;

/**
 * HTTP client configuration.
 *
 * @since 3.0
 */
public class HttpClientConfig
{

  private ConnectionConfig connectionConfig;

  private AuthenticationConfig authenticationConfig;

  private ProxyConfig proxyConfig;

  public ConnectionConfig getConnectionConfig() {
    return connectionConfig;
  }

  public AuthenticationConfig getAuthenticationConfig() {
    return authenticationConfig;
  }

  public ProxyConfig getProxyConfig() {
    return proxyConfig;
  }

  public HttpClientConfig withConnectionConfig(final ConnectionConfig connectionConfig) {
    this.connectionConfig = connectionConfig;
    return this;
  }

  public HttpClientConfig withAuthenticationConfig(final AuthenticationConfig authenticationConfig) {
    this.authenticationConfig = authenticationConfig;
    return this;
  }

  public HttpClientConfig withProxyConfig(final ProxyConfig proxyConfig) {
    this.proxyConfig = proxyConfig;
    return this;
  }

}
