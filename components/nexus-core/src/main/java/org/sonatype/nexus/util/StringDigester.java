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

package org.sonatype.nexus.util;

/**
 * A util class to calculate various digests on Strings. Usaful for some simple password management.
 *
 * @author cstamas
 * @deprecated Use DigesterUtils instead!
 */
public class StringDigester
{
  @Deprecated
  public static String LINE_SEPERATOR = System.getProperty("line.separator");

  /**
   * Calculates a SHA1 digest for a string.
   */
  @Deprecated
  public static String getSha1Digest(String content) {
    return DigesterUtils.getSha1Digest(content);
  }

  /**
   * Calculates MD5 digest for a string.
   */
  @Deprecated
  public static String getMd5Digest(String content) {
    return DigesterUtils.getMd5Digest(content);
  }
}
