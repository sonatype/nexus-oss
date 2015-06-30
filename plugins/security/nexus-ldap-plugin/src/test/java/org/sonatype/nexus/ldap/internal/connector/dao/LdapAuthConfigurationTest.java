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
package org.sonatype.nexus.ldap.internal.connector.dao;

import java.util.Arrays;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * Tests for LdapAuthConfiguration
 */
public class LdapAuthConfigurationTest
    extends TestSupport
{
  @Test
  public void testGetUserAttributes() {
    LdapAuthConfiguration ldapAuthConfiguration = new LdapAuthConfiguration();
    ldapAuthConfiguration.setEmailAddressAttribute("emailAddressAttribute");
    ldapAuthConfiguration.setPasswordAttribute(null);
    // unset the defaults (using a mix of empty strings and nulls
    ldapAuthConfiguration.setUserIdAttribute("");
    ldapAuthConfiguration.setUserRealNameAttribute(null);
    ldapAuthConfiguration.setUserMemberOfAttribute("");
    ldapAuthConfiguration.setWebsiteAttribute(null);

    String[] userAttributes = ldapAuthConfiguration.getUserAttributes();
    assertThat(Arrays.asList(userAttributes), hasSize(1));
    //only non null attributes should be added to the list
    assertThat("emailAddressAttribute", equalTo(userAttributes[0]));

    // set a few more then check the count
    ldapAuthConfiguration.setPasswordAttribute("passwordAttribute");
    ldapAuthConfiguration.setUserIdAttribute("userIdAttribute");

    userAttributes = ldapAuthConfiguration.getUserAttributes();
    assertThat(Arrays.asList(userAttributes), hasSize(3));
  }
}
