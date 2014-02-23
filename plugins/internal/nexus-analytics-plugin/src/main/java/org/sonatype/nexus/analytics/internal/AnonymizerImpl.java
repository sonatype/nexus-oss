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
package org.sonatype.nexus.analytics.internal;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.analytics.Anonymizer;
import org.sonatype.nexus.util.Tokens;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.crypto.CryptoHelper;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Default {@link Anonymizer} implementation.
 *
 * @since 2.8
 */
@Named
@Singleton
public class AnonymizerImpl
  extends ComponentSupport
  implements Anonymizer
{
  private final CryptoHelper crypto;

  private byte[] salt;

  @Inject
  public AnonymizerImpl(final CryptoHelper crypto) {
    this.crypto = checkNotNull(crypto);
  }

  public void setSalt(final byte[] salt) {
    checkNotNull(salt);
    checkArgument(salt.length != 0);
    this.salt = salt;
  }

  @Override
  public byte[] anonymize(final byte[] data) {
    checkNotNull(data);
    checkArgument(data.length != 0);
    checkState(salt != null, "Missing salt");
    MessageDigest digest = createDigest();
    digest.update(salt);
    digest.update(data);
    return digest.digest();
  }

  @Override
  public String anonymize(final String data) {
    checkNotNull(data);
    byte[] bytes = anonymize(data.getBytes(Charsets.UTF_8));
    return Tokens.encodeHexString(bytes);
  }

  private MessageDigest createDigest() {
    try {
      return crypto.createDigest("sha1");
    }
    catch (NoSuchAlgorithmException e) {
      throw Throwables.propagate(e);
    }
  }
}
