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

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test the Plexus Cipher container
 *
 * @author Oleg Gusakov
 * @version $Id$
 */
public class DefaultPlexusCipherTest
    extends TestSupport
{
  private String passPhrase = "foofoo";

  String str = "my testing phrase";

  String encStr = "CFUju8n8eKQHj8u0HI9uQMRmKQALtoXH7lY=";

  DefaultPlexusCipher pc;

  @Before
  public void before()
      throws Exception
  {
    pc = new DefaultPlexusCipher();
  }

  // -------------------------------------------------------------

  @Test
  @Ignore("This is not a test, it dumps Ciphers only")
  public void testFindDefaultAlgorithm()
      throws Exception
  {
    String[] res = CryptoUtils.getServiceTypes();
    assertNotNull("No Cipher providers found in the current environment", res);

    for (String provider : CryptoUtils.getCryptoImpls("Cipher")) {
      try {
        System.out.print(provider);
        pc = new DefaultPlexusCipher(new BouncyCastleProvider(), provider, 23);
        pc.encrypt(str, passPhrase);
        System.out.println("------------------> Success !!!!!!");
      }
      catch (Exception e) {
        System.out.println(e.getMessage());
      }
    }
  }

  @Test
  public void testDecrypt()
      throws Exception
  {
    String res = pc.decrypt(encStr, passPhrase);
    assertEquals("Decryption did not produce desired result", str, res);
  }

  @Test
  public void testEncrypt()
      throws Exception
  {
    String xRes = pc.encrypt(str, passPhrase);
    String res = pc.decrypt(xRes, passPhrase);
    assertEquals("Encryption/Decryption did not produce desired result", str, res);
  }

  @Test
  public void testDecorate()
      throws Exception
  {
    String res = pc.decorate("aaa");
    assertEquals("Decoration failed", PlexusCipher.ENCRYPTED_STRING_DECORATION_START + "aaa"
        + PlexusCipher.ENCRYPTED_STRING_DECORATION_STOP, res);
  }

  @Test
  public void testUnDecorate()
      throws Exception
  {
    String res =
        pc.unDecorate(PlexusCipher.ENCRYPTED_STRING_DECORATION_START + "aaa"
            + PlexusCipher.ENCRYPTED_STRING_DECORATION_STOP);
    assertEquals("Decoration failed", "aaa", res);
  }
}
