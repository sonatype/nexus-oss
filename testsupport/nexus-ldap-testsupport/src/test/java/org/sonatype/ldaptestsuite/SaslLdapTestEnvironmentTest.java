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
package org.sonatype.ldaptestsuite;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class SaslLdapTestEnvironmentTest
    extends AbstractLdapTestEnvironment
{


  /**
   * Tests to make sure DIGEST-MD5 binds below the RootDSE work.
   */
  public void testSaslDigestMd5Bind() throws Exception {

    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, "ldap://localhost:12345");

    env.put(Context.SECURITY_AUTHENTICATION, "DIGEST-MD5");
    //        env.put( Context.SECURITY_PRINCIPAL, "admin" );
    env.put(Context.SECURITY_PRINCIPAL, "tstevens");
    env.put(Context.SECURITY_CREDENTIALS, "tstevens123");

    // Specify realm
    env.put("java.naming.security.sasl.realm", "localhost");

    // Request privacy protection
    env.put("javax.security.sasl.qop", "auth-conf");

    DirContext context = new InitialDirContext(env);

    String[] attrIDs =
        {"uid"};

    Attributes attrs = context.getAttributes("uid=tstevens,ou=people,o=sonatype", attrIDs);

    String uid = null;

    if (attrs.get("uid") != null) {
      uid = (String) attrs.get("uid").get();
    }

    assertEquals(uid, "tstevens");
  }

}
