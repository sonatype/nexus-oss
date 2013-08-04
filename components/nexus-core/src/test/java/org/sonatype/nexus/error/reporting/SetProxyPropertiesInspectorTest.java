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

package org.sonatype.nexus.error.reporting;

import java.util.Properties;

import org.sonatype.nexus.configuration.application.GlobalRemoteProxySettings;
import org.sonatype.nexus.configuration.application.events.GlobalRemoteProxySettingsChangedEvent;
import org.sonatype.nexus.proxy.repository.RemoteHttpProxySettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SetProxyPropertiesInspector}.
 */
public class SetProxyPropertiesInspectorTest
    extends TestSupport
{

  @Mock
  private GlobalRemoteProxySettings globalRemoteProxySettings;

  @Mock
  private RemoteHttpProxySettings httpProxySettings;

  @Mock
  private RemoteHttpProxySettings httpsProxySettings;

  @Mock
  private GlobalRemoteProxySettingsChangedEvent evt;

  @Mock
  private UsernamePasswordRemoteAuthenticationSettings auth;

  private SetProxyPropertiesInspector underTest;

  private static Properties sysProps = System.getProperties();

  @Before
  public void setup() {
    when(globalRemoteProxySettings.getHttpProxySettings()).thenReturn(httpProxySettings);
    when(globalRemoteProxySettings.getHttpsProxySettings()).thenReturn(httpsProxySettings);
    underTest = new SetProxyPropertiesInspector(globalRemoteProxySettings);
  }

  @Before
  public void prepareSysProps() {
    System.setProperties(new Properties());
  }

  @After
  public void restoreSysProps() {
    System.setProperties(sysProps);
  }

  @Test
  public void testNoSettings() {
    underTest.inspect(evt);
    assertThat(System.getProperty("http.proxyHost"), nullValue());
  }

  @Test
  public void testBasicSettings() {
    when(httpProxySettings.isEnabled()).thenReturn(true);
    when(httpProxySettings.getHostname()).thenReturn("host");
    when(httpProxySettings.getPort()).thenReturn(1234);

    underTest.inspect(evt);

    assertThat(System.getProperty("http.proxyHost"), is("host"));
    assertThat(System.getProperty("http.proxyPort"), is("1234"));
    assertThat(System.getProperty("http.nonProxyHosts"), is(""));
  }

  @Test
  public void testNoProxyHosts() {
    when(globalRemoteProxySettings.getNonProxyHosts()).thenReturn(
        Sets.newTreeSet(Sets.newHashSet("host1", "host2"))
    );

    when(httpProxySettings.isEnabled()).thenReturn(true);
    when(httpProxySettings.getHostname()).thenReturn("host");
    when(httpProxySettings.getPort()).thenReturn(1234);

    underTest.inspect(evt);

    assertThat(System.getProperty("http.proxyHost"), is("host"));
    assertThat(System.getProperty("http.proxyPort"), is("1234"));
    assertThat(System.getProperty("http.nonProxyHosts"), is("host1|host2"));
  }

  @Test
  public void testAuth() {
    when(httpProxySettings.isEnabled()).thenReturn(true);
    when(httpProxySettings.getHostname()).thenReturn("host");
    when(httpProxySettings.getPort()).thenReturn(1234);
    when(httpProxySettings.getProxyAuthentication()).thenReturn(auth);
    when(auth.getUsername()).thenReturn("user");
    when(auth.getPassword()).thenReturn("password");

    underTest.inspect(evt);

    assertThat(System.getProperty("http.proxyHost"), is("host"));
    assertThat(System.getProperty("http.proxyPort"), is("1234"));
    assertThat(System.getProperty("http.proxyUser"), is("user"));
    assertThat(System.getProperty("http.proxyUserName"), is("user"));
    assertThat(System.getProperty("http.proxyPassword"), is("password"));
  }

  @Test
  public void testAllHttpsProps() {
    when(auth.getUsername()).thenReturn("user");
    when(auth.getPassword()).thenReturn("password");

    when(globalRemoteProxySettings.getNonProxyHosts()).thenReturn(
        Sets.newTreeSet(Sets.newHashSet("host1", "host2"))
    );

    when(httpProxySettings.isEnabled()).thenReturn(true);
    when(httpProxySettings.getHostname()).thenReturn("host1");
    when(httpProxySettings.getPort()).thenReturn(1234);
    when(httpProxySettings.getProxyAuthentication()).thenReturn(auth);

    when(httpsProxySettings.isEnabled()).thenReturn(true);
    when(httpsProxySettings.getHostname()).thenReturn("host2");
    when(httpsProxySettings.getPort()).thenReturn(4321);
    when(httpsProxySettings.getProxyAuthentication()).thenReturn(auth);

    underTest.inspect(evt);

    assertThat(System.getProperty("http.proxyHost"), is("host1"));
    assertThat(System.getProperty("http.proxyPort"), is("1234"));
    assertThat(System.getProperty("http.proxyUser"), is("user"));
    assertThat(System.getProperty("http.proxyUserName"), is("user"));
    assertThat(System.getProperty("http.proxyPassword"), is("password"));

    assertThat(System.getProperty("https.proxyHost"), is("host2"));
    assertThat(System.getProperty("https.proxyPort"), is("4321"));
    assertThat(System.getProperty("https.proxyUser"), is("user"));
    assertThat(System.getProperty("https.proxyUserName"), is("user"));
    assertThat(System.getProperty("https.proxyPassword"), is("password"));

    assertThat(System.getProperty("http.nonProxyHosts"), is("host1|host2"));
  }
}
