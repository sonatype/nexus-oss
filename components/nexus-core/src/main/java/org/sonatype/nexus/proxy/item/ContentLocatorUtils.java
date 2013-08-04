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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.util.IOUtils;
import org.sonatype.nexus.util.SystemPropertiesHelper;

import org.codehaus.plexus.util.IOUtil;

public class ContentLocatorUtils
{
  private static final boolean USE_MMAP = SystemPropertiesHelper.getBoolean(
      "org.sonatype.nexus.proxy.item.ContentLocatorUtils.useMmap", false);

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
    if (locator != null) {
      InputStream fis = null;

      try {
        fis = locator.getContent();

        if (USE_MMAP && fis instanceof FileInputStream) {
          return IOUtils.getBytesNioMmap(count, (FileInputStream) fis);
        }
        else {
          return IOUtils.getBytesClassic(count, fis);
        }
      }
      finally {
        IOUtil.close(fis);
      }
    }

    return null;
  }
}
