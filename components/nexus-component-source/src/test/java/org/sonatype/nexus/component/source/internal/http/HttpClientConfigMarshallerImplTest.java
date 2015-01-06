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
package org.sonatype.nexus.component.source.internal.http;

import java.util.Map;

import org.sonatype.nexus.component.source.http.AuthenticationConfigMarshaller;
import org.sonatype.nexus.component.source.http.ConnectionConfig;
import org.sonatype.nexus.component.source.http.HttpClientConfig;
import org.sonatype.nexus.component.source.http.HttpProxyConfig;
import org.sonatype.nexus.component.source.http.NtlmAuthenticationConfig;
import org.sonatype.nexus.component.source.http.ProxyConfig;
import org.sonatype.nexus.component.source.http.UsernameAuthenticationConfig;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * {@link HttpClientConfigMarshallerImpl} UTs.
 *
 * @since 3.0
 */
public class HttpClientConfigMarshallerImplTest
    extends TestSupport
{

  private Map<String, AuthenticationConfigMarshaller> authMarshallers = Maps.newHashMap();

  @Before
  public void setUp() {
    authMarshallers.put("username", new UsernameAuthenticationConfigMarshaller());
    authMarshallers.put("ntlm", new NtlmAuthenticationConfigMarshaller());
  }

  /**
   * Verify config marshalling to map.
   */
  @Test
  public void toMap() {
    Map<String, Object> map = new HttpClientConfigMarshallerImpl(authMarshallers).toMap(new HttpClientConfig()
        .withConnectionConfig(new ConnectionConfig()
            .withTimeout(1)
            .withRetries(2)
            .withUrlParameters("params")
            .withUserAgentCustomisation("ua")
            .withUseTrustStore(true)
        )
        .withAuthenticationConfig(new UsernameAuthenticationConfig()
            .withUsername("foo")
            .withPassword("bar")
        )
        .withProxyConfig(new ProxyConfig()
            .withHttpProxyConfig(new HttpProxyConfig()
                .withHostname("httpHost")
                .withPort(3)
                .withAuthenticationConfig(new UsernameAuthenticationConfig()
                    .withUsername("foo1")
                    .withPassword("bar1")
                )
            )
            .withHttpsProxyConfig(new HttpProxyConfig()
                .withHostname("httpsHost")
                .withPort(4)
                .withAuthenticationConfig(new NtlmAuthenticationConfig()
                    .withUsername("foo2")
                    .withPassword("bar2")
                    .withNtlmHost("ntlmH")
                    .withNtlmDomain("ntlmD")
                )
            )
            .withNonProxyHosts(new String[]{"h1", "h2"})
        ));

    assertThat((Integer) map.get("http.connection.timeout"), is(1));
    assertThat((Integer) map.get("http.connection.retries"), is(2));
    assertThat((String) map.get("http.connection.urlParameters"), is("params"));
    assertThat((String) map.get("http.connection.userAgentCustomisation"), is("ua"));
    assertThat((Boolean) map.get("http.connection.useTrustStore"), is(true));

    assertThat((String) map.get("http.authentication.type"), is("username"));
    assertThat((String) map.get("http.authentication.username"), is("foo"));
    assertThat((String) map.get("http.authentication.password"), is("bar"));
    assertThat(map.get("http.authentication.ntlmHost"), is(nullValue()));
    assertThat(map.get("http.authentication.ntlmDomain"), is(nullValue()));

    assertThat((String) map.get("http.proxy.http.hostname"), is("httpHost"));
    assertThat((Integer) map.get("http.proxy.http.port"), is(3));
    assertThat((String) map.get("http.proxy.http.authentication.type"), is("username"));
    assertThat((String) map.get("http.proxy.http.authentication.username"), is("foo1"));
    assertThat((String) map.get("http.proxy.http.authentication.password"), is("bar1"));
    assertThat(map.get("http.proxy.http.authentication.ntlmHost"), is(nullValue()));
    assertThat(map.get("http.proxy.http.authentication.ntlmDomain"), is(nullValue()));

    assertThat((String) map.get("http.proxy.https.hostname"), is("httpsHost"));
    assertThat((Integer) map.get("http.proxy.https.port"), is(4));
    assertThat((String) map.get("http.proxy.https.authentication.type"), is("ntlm"));
    assertThat((String) map.get("http.proxy.https.authentication.username"), is("foo2"));
    assertThat((String) map.get("http.proxy.https.authentication.password"), is("bar2"));
    assertThat((String) map.get("http.proxy.https.authentication.ntlmHost"), is("ntlmH"));
    assertThat((String) map.get("http.proxy.https.authentication.ntlmDomain"), is("ntlmD"));

    assertThat((String) map.get("http.proxy.nonProxyHosts"), is("h1,h2"));
  }

  /**
   * Verify config un-marshalling from map.
   */
  @Test
  public void fromMap() {
    Map<String, Object> map = Maps.newHashMap();
    map.put("http.connection.timeout", 1);
    map.put("http.connection.retries", 2);
    map.put("http.connection.urlParameters", "params");
    map.put("http.connection.userAgentCustomization", "ua");
    map.put("http.connection.useTrustStore", Boolean.TRUE);

    map.put("http.authentication.type", "username");
    map.put("http.authentication.username", "foo");
    map.put("http.authentication.password", "bar");

    map.put("http.proxy.http.hostname", "httpHost");
    map.put("http.proxy.http.port", 3);
    map.put("http.proxy.http.authentication.type", "username");
    map.put("http.proxy.http.authentication.username", "foo1");
    map.put("http.proxy.http.authentication.password", "bar1");

    map.put("http.proxy.https.hostname", "httpsHost");
    map.put("http.proxy.https.port", 4);
    map.put("http.proxy.https.authentication.type", "ntlm");
    map.put("http.proxy.https.authentication.username", "foo2");
    map.put("http.proxy.https.authentication.password", "bar2");
    map.put("http.proxy.https.authentication.ntlmHost", "ntlmH");
    map.put("http.proxy.https.authentication.ntlmDomain", "ntlmD");

    map.put("http.proxy.nonProxyHosts", "h1,h2");

    HttpClientConfig config = new HttpClientConfigMarshallerImpl(authMarshallers).fromMap(map);

    assertThat(config.getConnectionConfig().getTimeout(), is(1));
    assertThat(config.getConnectionConfig().getRetries(), is(2));
    assertThat(config.getConnectionConfig().getUrlParameters(), is("params"));
    assertThat(config.getConnectionConfig().getUserAgentCustomisation(), is("ua"));
    assertThat(config.getConnectionConfig().getUseTrustStore(), is(true));

    UsernameAuthenticationConfig a1 = (UsernameAuthenticationConfig) config.getAuthenticationConfig();
    assertThat(a1.getUsername(), is("foo"));
    assertThat(a1.getPassword(), is("bar"));

    assertThat(config.getProxyConfig().getHttpProxyConfig().getHostname(), is("httpHost"));
    assertThat(config.getProxyConfig().getHttpProxyConfig().getPort(), is(3));
    UsernameAuthenticationConfig a2 = (UsernameAuthenticationConfig) config.getProxyConfig().getHttpProxyConfig()
        .getAuthenticationConfig();
    assertThat(a2.getUsername(), is("foo1"));
    assertThat(a2.getPassword(), is("bar1"));

    assertThat(config.getProxyConfig().getHttpsProxyConfig().getHostname(), is("httpsHost"));
    assertThat(config.getProxyConfig().getHttpsProxyConfig().getPort(), is(4));
    NtlmAuthenticationConfig a3 = (NtlmAuthenticationConfig) config.getProxyConfig().getHttpsProxyConfig()
        .getAuthenticationConfig();
    assertThat(a3.getUsername(), is("foo2"));
    assertThat(a3.getPassword(), is("bar2"));
    assertThat(a3.getNtlmHost(), is("ntlmH"));
    assertThat(a3.getNtlmDomain(), is("ntlmD"));

    assertThat(config.getProxyConfig().getNonProxyHosts(), is(new String[]{"h1", "h2"}));
  }

}
