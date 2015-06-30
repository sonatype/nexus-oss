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
package org.sonatype.nexus.ldap.internal.connector.dao.password;

import org.sonatype.nexus.ldap.internal.connector.dao.password.hash.MD5Crypt;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class MD5CryptPasswordEncoderTest
    extends TestSupport
{
  @Test
  public void testEncryptAndVerify()
      throws Exception
  {
    final PasswordEncoder encoder = new MD5CryptPasswordEncoder();
    String crypted = encoder.encodePassword("test", null);
    int lastIdx = crypted.lastIndexOf('$');
    int firstIdx = crypted.indexOf('$');
    String salt = crypted.substring(firstIdx + "$1$".length(), lastIdx);
    String check = "{CRYPT}" + new MD5Crypt().crypt("test", salt);
    assertThat(check, equalTo(crypted));
    assertThat(encoder.isPasswordValid(crypted, "test", null), is(true));
  }
}
