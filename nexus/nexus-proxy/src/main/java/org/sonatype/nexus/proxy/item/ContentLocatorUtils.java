/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.item;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.util.IOUtils;
import org.sonatype.nexus.util.SystemPropertiesHelper;

public class ContentLocatorUtils
{
    private static final boolean USE_MMAP = SystemPropertiesHelper.getBoolean(
        "org.sonatype.nexus.proxy.item.ContentLocatorUtils.useMmap", false );

    /**
     * Reads up first bytes (exactly {@code count} of them) from ContentLocator's content. It returns byte array of
     * exact size of count, or null (ie. if file is smaller).
     * 
     * @param count the count of bytes to read up (and hence, the size of byte array to be returned).
     * @param locator the ContentLocator to read from.
     * @return returns byte array of size count or null.
     * @throws IOException
     */
    public static byte[] getFirstBytes( final int count, final ContentLocator locator )
        throws IOException
    {
        if ( locator != null )
        {
            InputStream fis = null;

            try
            {
                fis = locator.getContent();

                if ( USE_MMAP && fis instanceof FileInputStream )
                {
                    return IOUtils.getBytesNioMmap( count, (FileInputStream) fis );
                }
                else
                {
                    return IOUtils.getBytesClassic( count, fis );
                }
            }
            finally
            {
                IOUtil.close( fis );
            }
        }

        return null;
    }
}
