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

package org.sonatype.security.ldap.dao.password;

import org.codehaus.plexus.component.annotations.Component;

/**
 * @author tstevens
 */
@Component(role = PasswordEncoder.class, hint = "clear")
public class ClearTextPasswordEncoder
    implements PasswordEncoder
{

  public String getMethod() {
    return "CLEAR";
  }

  public String encodePassword(String password, Object salt) {
    return password;
  }

  public boolean isPasswordValid(String encPassword, String inputPassword, Object salt) {
    String encryptedPassword = encPassword;
    if (encryptedPassword.startsWith("{CLEAR}") || encryptedPassword.startsWith("{clear}")) {
      encryptedPassword = encryptedPassword.substring("{clear}".length());
    }

    String check = encodePassword(inputPassword, salt);

    return check.equals(encryptedPassword);
  }
}
