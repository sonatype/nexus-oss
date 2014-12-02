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
 * HTTP client proxy configuration.
 *
 * @since 3.0
 */
public class ProxyConfig
{

  private HttpProxyConfig httpProxyConfig;

  private HttpProxyConfig httpsProxyConfig;

  private String[] nonProxyHosts;

  public HttpProxyConfig getHttpProxyConfig() {
    return httpProxyConfig;
  }

  public HttpProxyConfig getHttpsProxyConfig() {
    return httpsProxyConfig;
  }

  public String[] getNonProxyHosts() {
    return nonProxyHosts;
  }

  public ProxyConfig withHttpProxyConfig(final HttpProxyConfig httpProxyConfig) {
    this.httpProxyConfig = httpProxyConfig;
    return this;
  }

  public ProxyConfig withHttpsProxyConfig(final HttpProxyConfig httpsProxyConfig) {
    this.httpsProxyConfig = httpsProxyConfig;
    return this;
  }

  public ProxyConfig withNonProxyHosts(final String[] nonProxyHosts) {
    this.nonProxyHosts = nonProxyHosts;
    return this;
  }
}
