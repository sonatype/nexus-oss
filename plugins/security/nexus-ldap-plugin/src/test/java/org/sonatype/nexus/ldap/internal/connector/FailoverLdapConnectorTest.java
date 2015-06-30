/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ldap.internal.connector;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for {@link FailoverLdapConnector}
 */
public class FailoverLdapConnectorTest
    extends TestSupport
{

  @Mock
  private LdapConnector original;

  @Mock
  private LdapConnector backup;

  @Test
  public void blacklistWithNoBackup() {
    final FailoverLdapConnector underTest = new FailoverLdapConnector(original, null, 10, 3);

    assertThat(underTest.isOriginalConnectorValid(), is(true));

    underTest.connectionFailed();
    assertThat(underTest.isOriginalConnectorValid(), is(true));
    underTest.connectionFailed();
    assertThat(underTest.isOriginalConnectorValid(), is(true));
    underTest.connectionFailed();

    assertThat(underTest.isOriginalConnectorValid(), is(false));
  }

  @Test
  public void blacklistConnectorOnFailure() {
    final FailoverLdapConnector underTest = new FailoverLdapConnector(original, backup, 10, 3);

    assertThat(underTest.isOriginalConnectorValid(), is(true));

    underTest.connectionFailed();
    assertThat(underTest.isOriginalConnectorValid(), is(true));

    underTest.connectionFailed();
    assertThat(underTest.isOriginalConnectorValid(), is(true));

    underTest.connectionFailed();
    assertThat(underTest.isOriginalConnectorValid(), is(false));

    underTest.resetFailure();
    assertThat(underTest.isOriginalConnectorValid(), is(true));
  }

  @Test
  public void recoverConnectorAfterDelay() {
    final FailoverLdapConnector underTest = new FailoverLdapConnector(original, backup, 10, 3);

    assertThat(underTest.isOriginalConnectorValid(), is(true));

    underTest.connectionFailed();
    assertThat(underTest.isOriginalConnectorValid(), is(true));

    underTest.connectionFailed();
    assertThat(underTest.isOriginalConnectorValid(), is(true));

    underTest.connectionFailed();
    assertThat(underTest.isOriginalConnectorValid(), is(false));

    underTest.setConnectionFailedTime(System.currentTimeMillis() - 12000);
    assertThat(underTest.isOriginalConnectorValid(), is(true));
  }

  @Test
  public void resetIncidentsAfterDelay() {
    final FailoverLdapConnector underTest = new FailoverLdapConnector(original, backup, 10, 3);

    assertThat(underTest.isOriginalConnectorValid(), is(true));

    underTest.connectionFailed();
    assertThat(underTest.isOriginalConnectorValid(), is(true));

    underTest.setConnectionFailedTime(System.currentTimeMillis() - 12000);
    assertThat(underTest.isOriginalConnectorValid(), is(true));

    underTest.connectionFailed();
    assertThat(underTest.isOriginalConnectorValid(), is(true));

    underTest.connectionFailed();
    assertThat(underTest.isOriginalConnectorValid(), is(true));

    underTest.connectionFailed();
    assertThat(underTest.isOriginalConnectorValid(), is(false));
  }
}