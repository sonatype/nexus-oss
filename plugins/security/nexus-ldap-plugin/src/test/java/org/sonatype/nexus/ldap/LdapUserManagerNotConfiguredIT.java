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
package org.sonatype.nexus.ldap;

import java.util.Collection;
import java.util.Collections;

import org.sonatype.nexus.ldap.internal.LdapITSupport;
import org.sonatype.nexus.security.user.UserManager;
import org.sonatype.nexus.security.user.UserNotFoundException;

import org.junit.Test;

import static org.junit.Assert.fail;

public class LdapUserManagerNotConfiguredIT
    extends LdapITSupport
{
  @Override
  protected Collection<String> getLdapServerNames() {
    return Collections.emptyList();
  }

  @Test
  public void testNotConfigured()
      throws Exception
  {
    final UserManager userManager = lookup(UserManager.class, "LDAP");
    try {
      userManager.getUser("cstamas");
      fail("Expected UserNotFoundException");
    }
    catch (UserNotFoundException e) {
      // OSS LDAP did throw transient when not configured, but Pro does not, it thrown only on error
      // catch (UserNotFoundTransientException e) {
      // expect transient error due to misconfiguration
    }
  }
}
