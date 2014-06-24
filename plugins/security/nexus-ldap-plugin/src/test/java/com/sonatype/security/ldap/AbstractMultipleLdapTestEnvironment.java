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
package com.sonatype.security.ldap;

import java.util.Map;
import java.util.Map.Entry;

import org.sonatype.ldaptestsuite.LdapServer;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;

public abstract class AbstractMultipleLdapTestEnvironment
    extends AbstractEnterpriseLdapTest
{

  /**
   * The logger.
   */
  private Logger logger;

  /**
   * The ldap server.
   */
  private Map<String, LdapServer> ldapServerMap;

  /**
   * Gets the logger.
   *
   * @return the logger
   */
  public Logger getLogger() {
    return logger;
  }

  /**
   * Gets the ldap server.
   *
   * @return the ldap server
   */
  public Map<String, LdapServer> getLdapServerMap() {
    return ldapServerMap;
  }

  public LdapServer getLdapServer(String hint) {
    return this.ldapServerMap.get(hint);
  }

  /*
   * (non-Javadoc)
   * @see org.codehaus.plexus.PlexusTestCase#setUp()
   */
  @Override
  public void setUp()
      throws Exception
  {
    super.setUp();

    LoggerManager loggerManager = (LoggerManager) lookup(LoggerManager.ROLE);

    logger = loggerManager.getLoggerForComponent(this.getClass().toString());

    ldapServerMap = this.getContainer().lookupMap(LdapServer.class);

    for (Entry<String, LdapServer> entry : this.ldapServerMap.entrySet()) {
      if (!entry.getValue().isStarted()) {
        entry.getValue().start();
      }
    }

  }

  /*
   * (non-Javadoc)
   * @see org.codehaus.plexus.PlexusTestCase#tearDown()
   */
  @Override
  public void tearDown()
      throws Exception
  {
    for (Entry<String, LdapServer> entry : this.ldapServerMap.entrySet()) {
      LdapServer ldapServer = entry.getValue();
      logger.info("Stopping LDAP server: " + entry.getKey());
      ldapServer.stop();
      ldapServer = null;
    }
    this.ldapServerMap = null;

    super.tearDown();
  }
}
