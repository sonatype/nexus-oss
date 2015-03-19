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

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.common.collect.NestedAttributesMap;

import com.google.common.collect.Lists;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;

import static com.google.common.base.Preconditions.checkNotNull;
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

  private String username;

  private String password;

  private String ntlmHost;

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

  public String getNtlmHost() {
    return ntlmHost;
  }

  public void setNtlmHost(final String ntlmHost) {
    this.ntlmHost = ntlmHost;
  }

  public String getNtlmDomain() {
    return ntlmDomain;
  }

  public void setNtlmDomain(final String ntlmDomain) {
    this.ntlmDomain = ntlmDomain;
  }

  public Credentials getCredentials() {
    return new NTCredentials(username, password, ntlmHost, ntlmDomain);
  }

  //
  // Marshaller
  //

  /**
   * {@link NtlmAuthenticationConfig} marshaller.
   */
  @Named(TYPE)
  @Singleton
  public static class MarshallerImpl
      implements Marshaller
  {
    @Override
    public void marshall(final AuthenticationConfig config, final NestedAttributesMap attributes) {
      checkNotNull(config);
      checkNotNull(attributes);
      NtlmAuthenticationConfig cfg = (NtlmAuthenticationConfig) config;
      attributes.set("username", cfg.getUsername());
      attributes.set("password", cfg.getPassword());
      attributes.set("ntlmHost", cfg.getNtlmHost());
      attributes.set("ntlmDomain", cfg.getNtlmDomain());
    }

    @Override
    public AuthenticationConfig unmarshall(final NestedAttributesMap attributes) {
      checkNotNull(attributes);
      NtlmAuthenticationConfig result = new NtlmAuthenticationConfig();
      result.setUsername(attributes.get("username", String.class));
      result.setPassword(attributes.get("password", String.class));
      result.setNtlmHost(attributes.get("ntlmHost", String.class));
      result.setNtlmDomain(attributes.get("ntlmDomain", String.class));
      return result;
    }
  }
}
