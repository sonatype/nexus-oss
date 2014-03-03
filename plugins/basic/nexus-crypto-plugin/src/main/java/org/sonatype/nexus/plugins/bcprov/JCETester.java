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

package org.sonatype.nexus.plugins.bcprov;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

/**
 * JCE Tester class.
 *
 * @since 2.8
 */
public class JCETester
{
  private static final String ALGORITHM = "AES";

  // no instances
  private JCETester() {}

  /**
   * Returns {@code true} if JCE Unlimited Strength Jurisdiction Policy files are installed, and unlimited
   * cryptographic cipher keys are allowed. Otherwise {@code false} is returned. Note: the test is performed by
   * asking for {@link Cipher#getMaxAllowedKeyLength(String)} of {@code AES} algorithm. In some cases, the
   * {@link NoSuchAlgorithmException} might be thrown, and this exception will be converted into a {@link
   * RuntimeException} as JVM not even having {@code AES} algorithm is not capable of running Nexus.
   *
   * @throws RuntimeException if the tested (and required) algorithm is not even present in JVM, not even minimal
   *                          requirements are met.
   * @see <a href="http://docs.oracle.com/javase/7/docs/api/javax/crypto/Cipher.html#getMaxAllowedKeyLength%28java.lang.String%29">Cipher#getMaxAllowedKeyLength(String)
   * Javadoc</a>
   * @see <a href="http://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/CryptoSpec.html#ExemptApps">Java7
   * JCA Reference Guide</a>
   */
  public static boolean isUnlimitedStrengthPolicyInstalled() throws RuntimeException {
    try {
      // Javadoc: If JCE unlimited strength jurisdiction policy files are installed, Integer.MAX_VALUE will be returned.
      return Cipher.getMaxAllowedKeyLength(ALGORITHM) == Integer.MAX_VALUE;
    }
    catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(ALGORITHM + " cryptographic algorithm not present in JVM, but is required.", e);
    }
  }
}