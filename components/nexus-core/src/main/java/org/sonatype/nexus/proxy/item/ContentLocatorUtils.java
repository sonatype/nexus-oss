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

package org.sonatype.nexus.proxy.item;

import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkArgument;

public class ContentLocatorUtils
{
  /**
   * Reads up first bytes (exactly {@code count} of them) from ContentLocator's content. It returns byte array of
   * exact size of count, or null (ie. if file is smaller).
   *
   * @param count   the count of bytes to read up (and hence, the size of byte array to be returned).
   * @param locator the ContentLocator to read from.
   * @return returns byte array of size count or null.
   */
  public static byte[] getFirstBytes(final int count, final ContentLocator locator)
      throws IOException
  {
    checkArgument(count > 0);
    if (locator != null) {
      try (final InputStream fis = locator.getContent()) {
        final byte[] buf = new byte[count];
        int ar = fis.read(buf);
        if (ar == count) {
          return buf;
        }
        else {
          // content is unreadable or even less than we want to read
          return null;
        }
      }
    }

    return null;
  }
}
