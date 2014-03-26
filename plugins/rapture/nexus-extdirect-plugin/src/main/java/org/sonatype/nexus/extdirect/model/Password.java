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
package org.sonatype.nexus.extdirect.model;

/**
 * A password field.
 *
 * @since 3.0
 */
public class Password
    extends Base64String
{

  public static final String FAKE_PASSWORD = "|$|N|E|X|U|S|$|";

  public Password() {
  }

  public Password(final String value) {
    super(value);
  }

  public boolean isValid() {
    return !FAKE_PASSWORD.equals(getValue());
  }

  public String getValueIfValid() {
    return isValid() ? getValue() : null;
  }

  public static Password password(final String value) {
    return new Password(value);
  }

  public static Password fakePassword() {
    return password(FAKE_PASSWORD);
  }

}
