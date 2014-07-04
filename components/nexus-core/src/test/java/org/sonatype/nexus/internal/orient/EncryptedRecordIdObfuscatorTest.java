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
package org.sonatype.nexus.internal.orient;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for {@link EncryptedRecordIdObfuscator}.
 */
public class EncryptedRecordIdObfuscatorTest
  extends TestSupport
{
  private EncryptedRecordIdObfuscator underTest;

  @Before
  public void setUp() throws Exception {
    this.underTest = new EncryptedRecordIdObfuscator("password", "salt", "0123456789ABCDEF");
  }

  @Test
  public void encodeAndDecode() {
    ORID rid = new ORecordId("#9:1");
    log("RID: {}", rid);
    String encoded = underTest.encode(rid);
    log("Encoded: {}", encoded);
    ORID decoded = underTest.decode(encoded);
    log("Decoded: {}", decoded);
    assertThat(decoded.toString(), is("#9:1"));
  }
}
