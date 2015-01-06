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

import java.io.File;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;

import org.apache.directory.server.constants.ServerDNConstants;

public class LdapTestEnvironmentTest
    extends AbstractLdapTestEnvironment
{

  /**
   * Test that the partition has been correctly created
   */
  public void testPartition() throws Exception {
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, "ldap://localhost:" + 12345 + "/o=sonatype");
    env.put(Context.SECURITY_PRINCIPAL, ServerDNConstants.ADMIN_SYSTEM_DN);
    env.put(Context.SECURITY_CREDENTIALS, "secret");
    env.put(Context.SECURITY_AUTHENTICATION, "simple");

    // Let's open a connection on this partition
    InitialContext initialContext = new InitialLdapContext(env, null);

    // We should be able to read it
    DirContext appRoot = (DirContext) initialContext.lookup("");
    assertNotNull(appRoot);

    // Let's get the entry associated to the top level
    Attributes attributes = appRoot.getAttributes("");
    assertNotNull(attributes);
    assertEquals("sonatype", (attributes.get("o")).get());

    Attribute attribute = attributes.get("objectClass");
    assertNotNull(attribute);
    assertTrue(attribute.contains("top"));
    assertTrue(attribute.contains("organization"));
    // Ok, everything is fine
  }


  public void testStop() throws Exception {
    // stopping seems to lock file on windows
    this.getLdapServer().stop();
    this.getLdapServer().doDelete(new File("target/apache-ds/"));
  }

}
