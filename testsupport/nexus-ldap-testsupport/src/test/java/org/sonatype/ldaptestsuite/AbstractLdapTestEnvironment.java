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
package org.sonatype.ldaptestsuite;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLdapTestEnvironment
    extends TestSupport
{
  protected Logger log = LoggerFactory.getLogger(getClass());

  private LdapServer ldapServer;

  /**
   * Gets the ldap server.
   *
   * @return the ldap server
   */
  public LdapServer getLdapServer() {
    return ldapServer;
  }

  @Before
  public void startLdap()
      throws Exception
  {
    this.ldapServer = new LdapServer(buildConfiguration()).start();
  }

  protected abstract LdapServerConfiguration buildConfiguration();

  @After
  public void stopLdap()
      throws Exception
  {
    ldapServer.stop();
    ldapServer = null;
  }
}
