/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.security.settings;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.sisu.goodies.crypto.CryptoHelper;
import org.sonatype.sisu.goodies.crypto.maven.MavenCipher;
import org.sonatype.sisu.goodies.crypto.maven.PasswordCipherMavenImpl;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * FIXME This needs to be abstracted, as this is just a copy of the class in nexus. The problem is if we move this to
 * base-configuration (or something) it becomes less secure, as we are using the same key for everything)
 */
@Singleton
@Named("default")
public class PasswordHelper
{
  private static final String ENC = "CMMDwoV";

  private final MavenCipher mavenCipher;

  @Inject
  public PasswordHelper(final CryptoHelper cryptoHelper) {
    checkNotNull(cryptoHelper);
    this.mavenCipher = new MavenCipher(new PasswordCipherMavenImpl(cryptoHelper));
  }

  public String encrypt(final String password)
  {
    return encrypt(password, ENC);
  }

  public String encrypt(final String password,final  String encoding) {
    if (password != null) {
      return mavenCipher.encrypt(password, encoding);
    }

    return null;
  }

  public String decrypt(final String encodedPassword)
  {
    return decrypt(encodedPassword, ENC);
  }

  public String decrypt(final String encodedPassword, final String encoding) {
    // check if the password is encrypted
    if (!mavenCipher.isPasswordCipher(encodedPassword)) {
      return encodedPassword;
    }

    if (encodedPassword != null) {
      return mavenCipher.decrypt(encodedPassword, encoding);
    }
    return null;
  }
}
