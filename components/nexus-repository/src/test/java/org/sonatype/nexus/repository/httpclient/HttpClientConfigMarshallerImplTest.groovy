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
package org.sonatype.nexus.repository.httpclient

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
    Map<String, AuthenticationConfig.Marshaller> marshallers = [:]
    marshallers[UsernameAuthenticationConfig.TYPE] = new UsernameAuthenticationConfig.MarshallerImpl()
    marshallers[NtlmAuthenticationConfig.TYPE] = new NtlmAuthenticationConfig.MarshallerImpl()
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

    NestedAttributesMap attributes = new NestedAttributesMap('key', [:])
    underTest.marshall(config, attributes)

    log attributes

    NestedAttributesMap connection = attributes.child('connection')
    assertThat(connection.get('timeout', Integer.class), is(1))
    assertThat(connection.get('retries', Integer.class), is(2))
    assertThat(connection.get('urlParameters', String.class), is('params'))
    assertThat(connection.get('userAgentCustomisation', String.class), is('ua'))
    assertThat(connection.get('useTrustStore', Boolean.class), is(true))

    NestedAttributesMap auth = attributes.child('authentication')
    assertThat(auth.get('type', String.class), is('username'))
    assertThat(auth.get('username', String.class), is('foo'))
    assertThat(auth.get('password', String.class), is('bar'))
    assertThat(auth.get('ntlmHost', String.class), is(nullValue()))
    assertThat(auth.get('ntlmDomain', String.class), is(nullValue()))

    NestedAttributesMap proxy = attributes.child('proxy')
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
    NestedAttributesMap attributes = new NestedAttributesMap('key', [:])

    NestedAttributesMap connection = attributes.child('connection')
    connection.set('timeout', 1)
    connection.set('retries', 2)
    connection.set('urlParameters', 'params')
    connection.set('userAgentCustomization', 'ua')
    connection.set('useTrustStore', Boolean.TRUE)

    NestedAttributesMap httpAuth = attributes.child('authentication')
    httpAuth.set('type', 'username')
    httpAuth.set('username', 'foo')
    httpAuth.set('password', 'bar')

    NestedAttributesMap proxy = attributes.child('proxy')
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

    log attributes

    HttpClientConfig config = underTest.unmarshall(attributes)

    assertThat(config.connectionConfig.timeout, is(1))
    assertThat(config.connectionConfig.retries, is(2))
    assertThat(config.connectionConfig.urlParameters, is('params'))
    assertThat(config.connectionConfig.userAgentCustomisation, is('ua'))
    assertThat(config.connectionConfig.useTrustStore, is(true))

    def a1 = (UsernameAuthenticationConfig) config.authenticationConfig
    assertThat(a1.username, is('foo'))
    assertThat(a1.password, is('bar'))

    assertThat(config.proxyConfig.httpProxyConfig.hostname, is('httpHost'))
    assertThat(config.proxyConfig.httpProxyConfig.port, is(3))

    def a2 = (UsernameAuthenticationConfig)config.proxyConfig.httpProxyConfig.authenticationConfig
    assertThat(a2.username, is('foo1'))
    assertThat(a2.password, is('bar1'))

    assertThat(config.proxyConfig.httpsProxyConfig.hostname, is('httpsHost'))
    assertThat(config.proxyConfig.httpsProxyConfig.port, is(4))

    def a3 = (NtlmAuthenticationConfig) config.proxyConfig.httpsProxyConfig.authenticationConfig
    assertThat(a3.username, is('foo2'))
    assertThat(a3.password, is('bar2'))
    assertThat(a3.ntlmHost, is('ntlmH'))
    assertThat(a3.ntlmDomain, is('ntlmD'))

    def nonProxyHosts = config.proxyConfig.nonProxyHosts
    assert nonProxyHosts.size() == 2
    assert nonProxyHosts[0] == 'h1'
    assert nonProxyHosts[1] == 'h2'
  }
}