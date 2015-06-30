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
package org.sonatype.nexus.ldap.internal.realms;


import org.sonatype.nexus.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.sisu.litmus.testsupport.ldap.LdapServer;

public class CramMd5AuthLdapSchemaIT
    extends LdapSchemaTestSupport
{
  @Override
  protected LdapServer createLdapServer(String name) {
    LdapServer ldapServer = super.createLdapServer(name);
    ldapServer.setAuthenticationSasl("CRAM-MD5");
    return ldapServer;
  }

  @Override
  protected LdapConfiguration createLdapClientConfigurationForServer(final String name, final int order,
                                                                     final LdapServer ldapServer)
  {
    final LdapConfiguration ldapConfiguration = super.createLdapClientConfigurationForServer(name, order, ldapServer);
    ldapConfiguration.getConnection().setAuthScheme("CRAM-MD5");
    ldapConfiguration.getConnection().setSaslRealm("localhost");
    ldapConfiguration.getConnection().setSystemUsername("admin");
    ldapConfiguration.getConnection().setSystemPassword("secret");
    return ldapConfiguration;
  }
}
