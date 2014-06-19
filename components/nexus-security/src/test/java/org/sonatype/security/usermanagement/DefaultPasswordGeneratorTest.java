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

package org.sonatype.security.usermanagement;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link DefaultPasswordGenerator}.
 */
public class DefaultPasswordGeneratorTest
    extends TestSupport
{
  protected DefaultPasswordGenerator underTest;

  @Before
  public void setUp() throws Exception {
    this.underTest = new DefaultPasswordGenerator();
  }

  @Test
  public void testGeneratePassword() throws Exception {
    String pw = underTest.generatePassword(10, 10);

    assertTrue(pw != null);
    assertTrue(pw.length() == 10);

    String encrypted = underTest.hashPassword(pw);
    String encrypted2 = underTest.hashPassword(pw);

    assertTrue(encrypted != null);
    assertTrue(encrypted2 != null);
    assertFalse(pw.equals(encrypted));
    assertFalse(pw.equals(encrypted2));
    assertTrue(encrypted.equals(encrypted2));

    String newPw = underTest.generatePassword(10, 10);

    assertTrue(newPw != null);
    assertTrue(newPw.length() == 10);
    assertFalse(pw.equals(newPw));

    String newEncrypted = underTest.hashPassword(newPw);
    String newEncrypted2 = underTest.hashPassword(newPw);

    assertTrue(newEncrypted != null);
    assertTrue(newEncrypted2 != null);
    assertFalse(newPw.equals(newEncrypted));
    assertFalse(newPw.equals(newEncrypted2));
    assertTrue(newEncrypted.equals(newEncrypted2));
    assertFalse(encrypted.equals(newEncrypted));
  }
}
