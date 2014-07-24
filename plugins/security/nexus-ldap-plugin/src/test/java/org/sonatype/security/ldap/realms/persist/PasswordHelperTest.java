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

package org.sonatype.security.ldap.realms.persist;

import org.sonatype.sisu.goodies.crypto.internal.CryptoHelperImpl;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;

public class PasswordHelperTest
    extends TestSupport
{

  public PasswordHelper getPasswordHelper()
      throws Exception
  {
    return new DefaultPasswordHelper(new CryptoHelperImpl());
  }

  @Test
  public void testValidPass()
      throws Exception
  {
    PasswordHelper ph = this.getPasswordHelper();

    String password = "PASSWORD";
    String encodedPass = ph.encrypt(password);
    assertEquals(password, ph.decrypt(encodedPass));
  }

  @Test
  public void testNullEncrypt()
      throws Exception
  {
    PasswordHelper ph = this.getPasswordHelper();
    assertNull(ph.encrypt(null));
  }

  @Test
  public void testNullDecrypt()
      throws Exception
  {
    PasswordHelper ph = this.getPasswordHelper();
    assertNull(ph.decrypt(null));
  }

  @Test
  public void testDecryptNonEncyprtedPassword()
      throws Exception
  {
    PasswordHelper ph = this.getPasswordHelper();

    try {
      ph.decrypt("clear-text-password");
      fail("Expected: IllegalArgumentException");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

  }

}
