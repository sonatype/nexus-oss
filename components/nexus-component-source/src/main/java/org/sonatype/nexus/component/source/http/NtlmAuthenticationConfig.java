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
package org.sonatype.nexus.component.source.http;

import java.util.List;

import com.google.common.collect.Lists;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.config.AuthSchemes;

/**
 * HTTP client NTLM authentication configuration.
 *
 * @since 3.0
 */
public class NtlmAuthenticationConfig
    implements AuthenticationConfig
{

  public static final String TYPE = "ntlm";

  private String username;

  private String password;

  private String ntlmHost;

  private String ntlmDomain;

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getNtlmHost() {
    return ntlmHost;
  }

  public String getNtlmDomain() {
    return ntlmDomain;
  }

  public NtlmAuthenticationConfig withUsername(final String username) {
    this.username = username;
    return this;
  }

  public NtlmAuthenticationConfig withPassword(final String password) {
    this.password = password;
    return this;
  }

  public NtlmAuthenticationConfig withNtlmHost(final String ntlmHost) {
    this.ntlmHost = ntlmHost;
    return this;
  }

  public NtlmAuthenticationConfig withNtlmDomain(final String ntlmDomain) {
    this.ntlmDomain = ntlmDomain;
    return this;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  public Credentials getCredentials() {
    return new NTCredentials(username, password, ntlmHost, ntlmDomain);
  }

  @Override
  public List<String> getPreferredAuthSchemes() {
    return Lists.newArrayList(AuthSchemes.NTLM, AuthSchemes.DIGEST, AuthSchemes.BASIC);
  }

  @Override
  public String toString() {
    return "NtlmAuthenticationConfig{" +
        "ntlmDomain='" + ntlmDomain + '\'' +
        ", username='" + username + '\'' +
        '}';
  }

}
