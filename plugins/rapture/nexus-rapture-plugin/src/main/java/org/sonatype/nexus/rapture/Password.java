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
package org.sonatype.nexus.rapture;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;

/**
 * Helper for password fields.
 *
 * @since 3.0
 */
public class Password
{
  /**
   * Token used for passwords that are defined, but which are not transmitted.
   */
  @VisibleForTesting
  static final String PASSWORD_PLACEHOLDER = "#~NEXUS~PLACEHOLDER~PASSWORD~#";

  /**
   * Returns password placeholder.
   */
  public static String placeholder() {
    return PASSWORD_PLACEHOLDER;
  }

  /**
   * Returns fake password placeholder unless value is {@code null}.
   */
  @Nullable
  public static String placeholder(@Nullable final String value) {
    if (value != null) {
      return PASSWORD_PLACEHOLDER;
    }
    return null;
  }

  /**
   * Determine if given value is a password placeholder.
   */
  public static boolean isPlaceholder(@Nullable final String value) {
    return value != null && PASSWORD_PLACEHOLDER.equals(value);
  }
}
