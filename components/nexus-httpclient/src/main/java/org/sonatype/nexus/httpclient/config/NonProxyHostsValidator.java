/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.httpclient.config;

import javax.validation.ConstraintValidatorContext;

import org.sonatype.nexus.validation.ConstraintValidatorSupport;

import com.google.common.base.Strings;

/**
 * {@link NonProxyHosts} validator.
 *
 * @since 3.0
 */
public class NonProxyHostsValidator
    extends ConstraintValidatorSupport<NonProxyHosts, String[]>
{
  @Override
  public boolean isValid(final String[] values, final ConstraintValidatorContext context) {
    for (String value : values) {
      if (!isValid(value)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns {@code true} if value is considered as valid nonProxyHosts expression. This is NOT validating the
   * single-string used to set system property (where expressions are delimited with "|")!
   */
  private boolean isValid(final String value) {
    // A value should be a non-empty string optionally prefixed or suffixed with an asterisk
    if (Strings.isNullOrEmpty(value)) {
      // must be non-empty
      return false;
    }
    if (value.contains("|")) {
      // must not contain | separator (used to separate multiple values in system properties)
      return false;
    }
    if (value.contains("*") && !(value.startsWith("*") || value.endsWith("*"))) {
      // if contains asterisk, it must be at beginning or end only
      return false;
    }
    return true;
  }
}
