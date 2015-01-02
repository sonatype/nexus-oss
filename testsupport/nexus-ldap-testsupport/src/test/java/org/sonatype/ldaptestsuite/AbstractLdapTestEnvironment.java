/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
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

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;

public abstract class AbstractLdapTestEnvironment
    extends PlexusTestCase
{
  /**
   * The logger.
   */
  private Logger logger;

  /**
   * The ldap server.
   */
  private LdapServer ldapServer;

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
  public LdapServer getLdapServer() {
    return ldapServer;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.codehaus.plexus.PlexusTestCase#setUp()
   */
  public void setUp()
      throws Exception
  {
    super.setUp();

    LoggerManager loggerManager = (LoggerManager) lookup(LoggerManager.ROLE);

    logger = loggerManager.getLoggerForComponent(this.getClass().toString());

    ldapServer = (LdapServer) lookup(LdapServer.ROLE);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.codehaus.plexus.PlexusTestCase#tearDown()
   */
  public void tearDown()
      throws Exception
  {
    ldapServer.stop();
    ldapServer = null;

    super.tearDown();
  }
}
