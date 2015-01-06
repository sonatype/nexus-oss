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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;

import org.apache.directory.server.constants.ServerDNConstants;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class LdapTestEnvironmentTest
    extends AbstractLdapTestEnvironment
{
  @Override
  protected LdapServerConfiguration buildConfiguration() {
    return LdapServerConfiguration.builder()
        .withWorkingDirectory(util.createTempDir())
        .withPartitions(
            Partition.builder()
                .withNameAndSuffix("sonatype", "o=sonatype")
                .withIndexedAttributes("objectClass", "o")
                .withRootEntryClasses("top", "organization")
                .withLdifFile(util.resolveFile("src/test/resources/sonatype.ldif")).build())
        .build();
  }

  @Test
  public void smoke() throws Exception {
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, "ldap://localhost:" + getLdapServer().getPort() + "/o=sonatype");
    env.put(Context.SECURITY_PRINCIPAL, ServerDNConstants.ADMIN_SYSTEM_DN);
    env.put(Context.SECURITY_CREDENTIALS, "secret");
    env.put(Context.SECURITY_AUTHENTICATION, "simple");

    // Let's open a connection on this partition
    InitialContext initialContext = new InitialLdapContext(env, null);

    // We should be able to read it
    DirContext appRoot = (DirContext) initialContext.lookup("");
    assertThat(appRoot, notNullValue());

    // Let's get the entry associated to the top level
    Attributes attributes = appRoot.getAttributes("");
    assertThat(attributes, notNullValue());
    assertThat((String) attributes.get("o").get(), equalTo("sonatype"));

    Attribute attribute = attributes.get("objectClass");
    assertThat(attribute, notNullValue());
    assertThat(attribute.contains("top"), is(true));
    assertThat(attribute.contains("organization"), is(true));
  }
}
