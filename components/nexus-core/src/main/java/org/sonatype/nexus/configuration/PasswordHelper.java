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

package org.sonatype.nexus.configuration;

import org.sonatype.plexus.components.cipher.PlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component(role = PasswordHelper.class)
public class PasswordHelper
{

  private static final String ENC = "CMMDwoV";

  @Requirement
  private PlexusCipher plexusCipher;

  public String encrypt(String password)
      throws PlexusCipherException
  {
    return encrypt(password, ENC);
  }

  public String encrypt(String password, String encoding)
      throws PlexusCipherException
  {
    // check if the password is encrypted
    if (plexusCipher.isEncryptedString(password)) {
      return password;
    }

    if (password != null) {
      return plexusCipher.encryptAndDecorate(password, encoding);
    }

    return null;
  }

  public String decrypt(String encodedPassword)
      throws PlexusCipherException
  {
    return decrypt(encodedPassword, ENC);
  }

  public String decrypt(String encodedPassword, String encoding)
      throws PlexusCipherException
  {
    // check if the password is encrypted
    if (!plexusCipher.isEncryptedString(encodedPassword)) {
      return encodedPassword;
    }

    if (encodedPassword != null) {
      return plexusCipher.decryptDecorated(encodedPassword, encoding);
    }
    return null;
  }
}
