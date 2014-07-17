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

package org.sonatype.security.ldap.upgrade.cipher;

import org.sonatype.security.ldap.realms.persist.DefaultPasswordHelper;
import org.sonatype.security.ldap.realms.persist.PasswordHelper;
import org.sonatype.sisu.goodies.crypto.internal.CryptoHelperImpl;
import org.sonatype.sisu.goodies.crypto.internal.DefaultPasswordCipher;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;

public class LegacyPasswordEncryptionTest
    extends TestSupport
{
  @Test
  public void testLegacyPassword()
      throws Exception
  {
    final String legacyEncryptedPassword = "CP2WQrKyuB/fphz8c1eg5zaG";
    final String legacyClearPassword = "S0natyp31";

    PasswordHelper passHelper = new DefaultPasswordHelper(new DefaultPasswordCipher(new CryptoHelperImpl()));

    assertEquals(passHelper.decrypt(legacyEncryptedPassword), legacyClearPassword);
  }

}
