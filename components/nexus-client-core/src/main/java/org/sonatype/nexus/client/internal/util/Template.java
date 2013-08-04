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

package org.sonatype.nexus.client.internal.util;

/**
 * A simple "template" just to defer the evaluation until it is actually needed. See {@link Check} for it's use, but is
 * not limited to it.
 *
 * @since 2.1
 */
public class Template
{

  private final String format;

  private final Object[] args;

  public Template(final String format, final Object... args) {
    if (format == null || format.trim().isEmpty()) {
      throw new IllegalArgumentException("Template's format cannot be blank!");
    }
    this.format = format;
    this.args = args;
  }

  public String evaluate() {
    return String.format(format, args);
  }

  public String getFormat() {
    return format;
  }

  public Object[] getArgs() {
    return args;
  }

  @Override
  public String toString() {
    return evaluate();
  }

  // ==

  public static Template of(final String format, final Object... args) {
    return new Template(format, args);
  }
}
