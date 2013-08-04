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

package org.sonatype.nexus.security.filter.authc;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.shiro.authc.AuthenticationToken;

public class NexusSecureHttpAuthenticationFilter
    extends NexusHttpAuthenticationFilter
{
  @Inject
  @Nullable
  private PasswordDecryptor passwordDecryptor;

  protected PasswordDecryptor getPasswordDecryptor() {
    return passwordDecryptor;
  }

  protected String decryptPasswordIfNeeded(String password) {
    PasswordDecryptor pd = getPasswordDecryptor();

    if (pd != null && pd.isEncryptedPassword(password)) {
      return pd.getDecryptedPassword(password);
    }
    else {
      return password;
    }
  }

  @Override
  protected AuthenticationToken createToken(String username, String password, boolean rememberMe, String address) {
    return super.createToken(username, decryptPasswordIfNeeded(password), rememberMe, address);
  }
}
