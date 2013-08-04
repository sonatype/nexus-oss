/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.client.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.client.internal.util.Check;

/**
 * @since 2.1
 */
public class ConnectionInfo
{

  private final BaseUrl baseUrl;

  private final AuthenticationInfo authenticationInfo;

  private final Map<Protocol, ProxyInfo> proxyInfos;

  public ConnectionInfo(final BaseUrl baseUrl, final AuthenticationInfo authenticationInfo,
                        final Map<Protocol, ProxyInfo> proxyInfos)
  {
    this.baseUrl = Check.notNull(baseUrl, "Base URL is null!");
    this.authenticationInfo = authenticationInfo;
    HashMap<Protocol, ProxyInfo> proxies = new HashMap<Protocol, ProxyInfo>();
    if (proxyInfos != null) {
      proxies.putAll(proxyInfos);
    }
    this.proxyInfos = Collections.unmodifiableMap(proxies);
  }

  public BaseUrl getBaseUrl() {
    return baseUrl;
  }

  public AuthenticationInfo getAuthenticationInfo() {
    return authenticationInfo;
  }

  public Map<Protocol, ProxyInfo> getProxyInfos() {
    return proxyInfos;
  }

  // ==

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("(");
    sb.append("url=").append(getBaseUrl());
    if (getAuthenticationInfo() != null) {
      sb.append(",authc=").append(getAuthenticationInfo());
    }
    if (!getProxyInfos().isEmpty()) {
      sb.append(",proxy=").append(getProxyInfos());
    }
    sb.append(")");
    return sb.toString();
  }
}
