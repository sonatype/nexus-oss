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
package org.sonatype.nexus.repository.http

import org.junit.Before
import org.junit.Test
import org.sonatype.nexus.repository.util.NestedAttributesMap
import org.sonatype.sisu.litmus.testsupport.TestSupport

import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.nullValue
import static org.junit.Assert.assertThat

/**
 * Tests for {@link HttpClientConfigMarshallerImpl}.
 */
public class HttpClientConfigMarshallerImplTest
    extends TestSupport
{
  private HttpClientConfigMarshallerImpl underTest

  @Before
  void setUp() {
    Map<String, AuthenticationConfigMarshaller> marshallers = [:]
    marshallers[UsernameAuthenticationConfig.TYPE] = new UsernameAuthenticationConfigMarshaller()
    marshallers[NtlmAuthenticationConfig.TYPE] = new NtlmAuthenticationConfigMarshaller()
    underTest = new HttpClientConfigMarshallerImpl(marshallers)
  }

  /**
   * Verify config marshalling to NestedAttributesMap.
   */
  @Test
  void toMap() {
    HttpClientConfig config = new HttpClientConfig(
        connectionConfig: new ConnectionConfig(
            timeout: 1,
            retries: 2,
            urlParameters: 'params',
            userAgentCustomisation: 'ua',
            useTrustStore: true
        ),
        authenticationConfig: new UsernameAuthenticationConfig(
            username: 'foo',
            password: 'bar'
        ),
        proxyConfig: new ProxyConfig(
            httpProxyConfig: new HttpProxyConfig(
                hostname: 'httpHost',
                port: 3,
                authenticationConfig: new UsernameAuthenticationConfig(
                    username: 'foo1',
                    password: 'bar1'
                )
            ),
            httpsProxyConfig: new HttpProxyConfig(
                hostname: 'httpsHost',
                port: 4,
                authenticationConfig: new NtlmAuthenticationConfig(
                    username: 'foo2',
                    password: 'bar2',
                    ntlmHost: 'ntlmH',
                    ntlmDomain: 'ntlmD'
                )
            ),
            nonProxyHosts: [ 'h1', 'h2' ]
        )
    )

    NestedAttributesMap map = new NestedAttributesMap('key', [:])
    underTest.toMap(config, map)

    log map

    NestedAttributesMap http = map.child('http')
    NestedAttributesMap connection = http.child('connection')
    assertThat(connection.get('timeout', Integer.class), is(1))
    assertThat(connection.get('retries', Integer.class), is(2))
    assertThat(connection.get('urlParameters', String.class), is('params'))
    assertThat(connection.get('userAgentCustomisation', String.class), is('ua'))
    assertThat(connection.get('useTrustStore', Boolean.class), is(true))

    NestedAttributesMap auth = http.child('authentication')
    assertThat(auth.get('type', String.class), is('username'))
    assertThat(auth.get('username', String.class), is('foo'))
    assertThat(auth.get('password', String.class), is('bar'))
    assertThat(auth.get('ntlmHost', String.class), is(nullValue()))
    assertThat(auth.get('ntlmDomain', String.class), is(nullValue()))

    NestedAttributesMap proxy = http.child('proxy')
    NestedAttributesMap proxyHttp = proxy.child('http')
    assertThat(proxyHttp.get('hostname', String.class), is('httpHost'))
    assertThat(proxyHttp.get('port', Integer.class), is(3))
    NestedAttributesMap proxyHttpAuth = proxyHttp.child('authentication')
    assertThat(proxyHttpAuth.get('type', String.class), is('username'))
    assertThat(proxyHttpAuth.get('username', String.class), is('foo1'))
    assertThat(proxyHttpAuth.get('password', String.class), is('bar1'))
    assertThat(proxyHttpAuth.get('ntlmHost', String.class), is(nullValue()))
    assertThat(proxyHttpAuth.get('ntlmDomain', String.class), is(nullValue()))

    NestedAttributesMap proxyHttps = proxy.child('https')
    assertThat(proxyHttps.get('hostname', String.class), is('httpsHost'))
    assertThat(proxyHttps.get('port', Integer.class), is(4))
    NestedAttributesMap proxyHttpsAuth = proxyHttps.child('authentication')
    assertThat(proxyHttpsAuth.get('type', String.class), is('ntlm'))
    assertThat(proxyHttpsAuth.get('username', String.class), is('foo2'))
    assertThat(proxyHttpsAuth.get('password', String.class), is('bar2'))
    assertThat(proxyHttpsAuth.get('ntlmHost', String.class), is('ntlmH'))
    assertThat(proxyHttpsAuth.get('ntlmDomain', String.class), is('ntlmD'))

    assertThat(proxy.get('nonProxyHosts', String.class), is('h1,h2'))
  }

  /**
   * Verify config un-marshalling from NestedAttributesMap.
   */
  @Test
  void fromMap() {
    Map<String, Object> innerMap = [:]
    NestedAttributesMap map = new NestedAttributesMap('key', innerMap)

    NestedAttributesMap http = map.child('http')
    NestedAttributesMap connection = http.child('connection')
    connection.set('timeout', 1)
    connection.set('retries', 2)
    connection.set('urlParameters', 'params')
    connection.set('userAgentCustomization', 'ua')
    connection.set('useTrustStore', Boolean.TRUE)

    NestedAttributesMap httpAuth = http.child('authentication')
    httpAuth.set('type', 'username')
    httpAuth.set('username', 'foo')
    httpAuth.set('password', 'bar')

    NestedAttributesMap proxy = http.child('proxy')
    NestedAttributesMap proxyHttp = proxy.child('http')
    proxyHttp.set('hostname', 'httpHost')
    proxyHttp.set('port', 3)
    proxyHttp.child('authentication').set('type', 'username')
    proxyHttp.child('authentication').set('username', 'foo1')
    proxyHttp.child('authentication').set('password', 'bar1')

    NestedAttributesMap proxyHttps = proxy.child('https')
    proxyHttps.set('hostname', 'httpsHost')
    proxyHttps.set('port', 4)
    NestedAttributesMap proxyHttpsAuth = proxyHttps.child('authentication')
    proxyHttpsAuth.set('type', 'ntlm')
    proxyHttpsAuth.set('username', 'foo2')
    proxyHttpsAuth.set('password', 'bar2')
    proxyHttpsAuth.set('ntlmHost', 'ntlmH')
    proxyHttpsAuth.set('ntlmDomain', 'ntlmD')

    proxy.set('nonProxyHosts', 'h1,h2')

    log map

    HttpClientConfig config = underTest.fromMap(map)

    assertThat(config.getConnectionConfig().getTimeout(), is(1))
    assertThat(config.getConnectionConfig().getRetries(), is(2))
    assertThat(config.getConnectionConfig().getUrlParameters(), is('params'))
    assertThat(config.getConnectionConfig().getUserAgentCustomisation(), is('ua'))
    assertThat(config.getConnectionConfig().getUseTrustStore(), is(true))

    UsernameAuthenticationConfig a1 = (UsernameAuthenticationConfig) config.getAuthenticationConfig()
    assertThat(a1.getUsername(), is('foo'))
    assertThat(a1.getPassword(), is('bar'))

    assertThat(config.getProxyConfig().getHttpProxyConfig().getHostname(), is('httpHost'))
    assertThat(config.getProxyConfig().getHttpProxyConfig().getPort(), is(3))
    UsernameAuthenticationConfig a2 = (UsernameAuthenticationConfig) config.getProxyConfig().getHttpProxyConfig()
        .getAuthenticationConfig()
    assertThat(a2.getUsername(), is('foo1'))
    assertThat(a2.getPassword(), is('bar1'))

    assertThat(config.getProxyConfig().getHttpsProxyConfig().getHostname(), is('httpsHost'))
    assertThat(config.getProxyConfig().getHttpsProxyConfig().getPort(), is(4))
    NtlmAuthenticationConfig a3 = (NtlmAuthenticationConfig) config.getProxyConfig().getHttpsProxyConfig()
        .getAuthenticationConfig()
    assertThat(a3.getUsername(), is('foo2'))
    assertThat(a3.getPassword(), is('bar2'))
    assertThat(a3.getNtlmHost(), is('ntlmH'))
    assertThat(a3.getNtlmDomain(), is('ntlmD'))

    def nonProxyHosts = config.getProxyConfig().getNonProxyHosts()
    assert nonProxyHosts.size() == 2
    assert nonProxyHosts[0] == 'h1'
    assert nonProxyHosts[1] == 'h2'
  }
}