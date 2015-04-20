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

import java.util.Arrays;

import javax.validation.Valid;

/**
 * HTTP client proxy configuration.
 *
 * @since 3.0
 */
public class ProxyConfig
{
  @Valid
  private HttpProxyConfig http;

  @Valid
  private HttpProxyConfig https;

  private String[] nonProxyHosts;

  public HttpProxyConfig getHttp() {
    return http;
  }

  public void setHttp(final HttpProxyConfig http) {
    this.http = http;
  }

  public HttpProxyConfig getHttps() {
    return https;
  }

  public void setHttps(final HttpProxyConfig https) {
    this.https = https;
  }

  public String[] getNonProxyHosts() {
    return nonProxyHosts;
  }

  public void setNonProxyHosts(final String[] nonProxyHosts) {
    this.nonProxyHosts = nonProxyHosts;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "http=" + http +
        ", https=" + https +
        ", nonProxyHosts=" + Arrays.toString(nonProxyHosts) +
        '}';
  }
}
