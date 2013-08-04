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
 * Simple internal utility class to perform checks of arguments. Inspired by Google's {@code Preconditions}.
 *
 * @since 2.1
 */
public class Check
{

  public static boolean isBlank(final String t) {
    // check for null
    return t == null || t.trim().isEmpty();
  }

  public static String notBlank(final String t, final String name) {
    // check for blank
    argument(!isBlank(t), new Template("\"%s\" is blank!", name));
    return t;
  }

  public static <T> T notNull(final T t, final Class<?> clazz) {
    return notNull(t, new Template("%s is null!", clazz.getSimpleName()));
  }

  public static <T> T notNull(final T t, final Object message) {
    if (null == t) {
      throw new NullPointerException(String.valueOf(message));
    }

    return t;
  }

  public static void argument(boolean condition, final Object message) {
    argument(condition, null, message);
  }

  public static <T> T argument(boolean condition, final T t, final Object message) {
    if (!condition) {
      throw new IllegalArgumentException(String.valueOf(message));
    }

    return t;
  }
}
