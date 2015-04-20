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

import org.sonatype.nexus.common.text.Strings2;

import com.google.common.collect.Lists;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.hibernate.validator.constraints.NotEmpty;

import static org.apache.http.client.config.AuthSchemes.BASIC;
import static org.apache.http.client.config.AuthSchemes.DIGEST;
import static org.apache.http.client.config.AuthSchemes.NTLM;

/**
 * NTLM authentication configuration.
 *
 * @since 3.0
 */
public class NtlmAuthenticationConfig
    extends AuthenticationConfig
{
  public static final String TYPE = "ntlm";

  @NotEmpty
  private String username;

  @NotEmpty
  private String password;

  @Nullable
  private String ntlmHost;

  @Nullable
  private String ntlmDomain;

  public NtlmAuthenticationConfig() {
    super(TYPE, Lists.newArrayList(NTLM, DIGEST, BASIC));
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  @Nullable
  public String getNtlmHost() {
    return ntlmHost;
  }

  public void setNtlmHost(final @Nullable String ntlmHost) {
    this.ntlmHost = ntlmHost;
  }

  @Nullable
  public String getNtlmDomain() {
    return ntlmDomain;
  }

  public void setNtlmDomain(final @Nullable String ntlmDomain) {
    this.ntlmDomain = ntlmDomain;
  }

  @Override
  public Credentials getCredentials() {
    return new NTCredentials(username, password, ntlmHost, ntlmDomain);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "username='" + username + '\'' +
        ", password='" + Strings2.mask(password) + '\'' +
        ", ntlmHost='" + ntlmHost + '\'' +
        ", ntlmDomain='" + ntlmDomain + '\'' +
        '}';
  }
}
