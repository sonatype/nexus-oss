/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.security.ldap.realms.test.api;

import org.sonatype.nexus.security.ldap.realms.DefaultLdapManager;
import org.sonatype.security.ldap.realms.LdapManager;
import org.sonatype.security.ldap.realms.persist.LdapConfiguration;

import org.codehaus.plexus.component.annotations.Component;

@Component(role = LdapManager.class, hint = "TestLdapManager")
public class TestLdapManager
    extends DefaultLdapManager
{

  private LdapConfiguration ldapConfiguration;

  public LdapConfiguration getLdapConfiguration() {
    return ldapConfiguration;
  }

  public void setLdapConfiguration(LdapConfiguration ldapConfiguration) {
    this.ldapConfiguration = ldapConfiguration;
  }


}
