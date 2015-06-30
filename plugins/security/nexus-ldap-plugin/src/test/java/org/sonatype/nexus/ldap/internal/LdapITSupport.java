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
package org.sonatype.nexus.ldap.internal;

import java.io.File;
import java.util.Hashtable;

import javax.naming.Context;

import org.sonatype.nexus.ldap.internal.persist.entity.LdapConfiguration;
import org.sonatype.nexus.ldap.internal.persist.entity.Mapping;
import org.sonatype.sisu.litmus.testsupport.ldap.LdapServer;

/**
 * Support for LDAP ITs (slow UTs) with real OrientDB backed configuration source.
 */
public abstract class LdapITSupport
    extends LdapTestSupport
{
  @Override
  protected LdapConfiguration createLdapClientConfigurationForServer(final String name, final int order,
                                                                     final LdapServer ldapServer)
  {
    final LdapConfiguration ldapConfiguration = super.createLdapClientConfigurationForServer(name, order, ldapServer);

    // adjust it, ITs by default uses different groups
    final Mapping mapping = ldapConfiguration.getMapping();
    mapping.setUserMemberOfAttribute(null);
    mapping.setGroupObjectClass("groupOfUniqueNames");
    mapping.setGroupMemberAttribute("uniqueMember");
    return ldapConfiguration;
  }

  @Override
  protected File resolveDefaultLdifFile() {
    return util.resolveFile("src/test/resources/defaults/it/default-ldap.ldif");
  }

  protected Hashtable initialLdapEnvironment() {
    Hashtable<String, Object> env = new Hashtable<>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, "ldap://localhost:" + ldapServers.get("default").getPort() + "/o=sonatype");
    env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
    env.put(Context.SECURITY_CREDENTIALS, "secret");
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    return env;
  }
}
