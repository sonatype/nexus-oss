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
package com.sonatype.security.ldap.persist;

import java.io.File;

import com.sonatype.security.ldap.realms.persist.model.CLdapConfiguration;
import com.sonatype.security.ldap.realms.persist.model.CLdapServerConfiguration;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

public class DefaultLdapConfigurationSourceTest
    extends TestSupport
{
  @Mock
  private ApplicationConfiguration applicationConfiguration;

  @Mock
  private PasswordHelper passwordHelper;

  @Mock
  private org.sonatype.security.ldap.realms.persist.PasswordHelper legacyPasswordHelper;

  /**
   * OSS release.
   */
  @Test
  public void upgradeOss() throws Exception {
    when(applicationConfiguration.getConfigurationDirectory())
        .thenReturn(util.resolveFile("target/test-classes/upgrade-oss"));
    final DefaultLdapConfigurationSource testSubject = new DefaultLdapConfigurationSource(applicationConfiguration,
        passwordHelper, legacyPasswordHelper);

    final CLdapConfiguration cnf = testSubject.load();
    assertThat(cnf.getVersion(), notNullValue());
    assertThat(cnf.getVersion(), equalTo(CLdapConfiguration.MODEL_VERSION));
    assertThat(cnf.getServers(), hasSize(1));
    final CLdapServerConfiguration server = cnf.getServers().get(0);
    assertThat(server.getConnectionInfo().getSearchBase(), equalTo("o=sonatype"));
    assertThat(server.getUserAndGroupConfig().getGroupMemberFormat(), equalTo("uid=${username},ou=people,o=sonatype"));
  }

  /**
   * Empty LDAP XML from 2.7.2 Pro release.
   *
   * @see <a href="https://issues.sonatype.org/browse/NEXUS-6348">NEXUS-6348</a>
   */
  @Test
  public void upgradeNexus6348() throws Exception {
    when(applicationConfiguration.getConfigurationDirectory()).thenReturn(util.resolveFile("target/test-classes/upgrade-nexus6348"));
    final DefaultLdapConfigurationSource testSubject = new DefaultLdapConfigurationSource(applicationConfiguration,
        passwordHelper, legacyPasswordHelper);

    final CLdapConfiguration cnf = testSubject.load();
    assertThat(cnf.getVersion(), notNullValue());
    assertThat(cnf.getVersion(), equalTo(CLdapConfiguration.MODEL_VERSION));
    assertThat(cnf.getServers(), empty());
  }
}
