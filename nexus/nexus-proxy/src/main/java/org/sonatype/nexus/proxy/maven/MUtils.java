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
package org.sonatype.nexus.proxy.maven;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.item.StorageFileItem;

/**
 * Maven specific "static" utilities.
 * 
 * @author cstamas
 */
public class MUtils
{
    /**
     * This method will read up a stream usually created from .sha1/.md5. The method CLOSES the passed in stream!
     * 
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String readDigestFromStream( final InputStream inputStream )
        throws IOException
    {
        try
        {
            String raw = StringUtils.chomp( IOUtil.toString( inputStream, "UTF-8" ) ).trim();

            String digest;
            // digest string at end with separator, e.g.:
            // MD5 (pom.xml) = 68da13206e9dcce2db9ec45a9f7acd52
            // ant-1.5.jar: DCAB 88FC 2A04 3C24 79A6 DE67 6A2F 8179 E9EA 2167
            if ( raw.contains( "=" ) || raw.contains( ":" ) )
            {
                digest = raw.split( "[=:]", 2 )[1].trim();
            }
            else
            {
                // digest string at start, e.g. '68da13206e9dcce2db9ec45a9f7acd52 pom.xml'
                digest = raw.split( " ", 2 )[0];
            }

            if ( !isDigest( digest ) )
            {
                // maybe it's "uncompressed", e.g. 'DCAB 88FC 2A04 3C24 79A6 DE67 6A2F 8179 E9EA 2167'
                digest = compress( digest );
            }

            if ( !isDigest( digest ) )
            {
                // check if the raw string is an uncompressed checksum, e.g.
                // 'DCAB 88FC 2A04 3C24 79A6 DE67 6A2F 8179 E9EA 2167'
                digest = compress( raw );
            }

            if ( !isDigest( digest ) )
            {
                // check if the raw string is an uncompressed checksum with file name suffix, e.g.
                // 'DCAB 88FC 2A04 3C24 79A6 DE67 6A2F 8179 E9EA 2167 pom.xml'
                digest = compress( raw.substring( 0, raw.lastIndexOf( " " ) ).trim() );
            }

            if ( !isDigest( digest ) )
            {
                // we have to return some string even if it's not a valid digest, because 'null' is treated as
                // "checksum does not exist" elsewhere (AbstractChecksumContentValidator)
                // -> fallback to original behavior
                digest = raw.split( " ", 2 )[0];
            }

            return digest;
        }
        finally
        {
            IOUtil.close( inputStream );
        }
    }

    private static String compress( String digest )
    {
        digest = digest.replaceAll( " ", "" ).toLowerCase( Locale.US );
        return digest;
    }

    private static boolean isDigest( String digest )
    {
        return digest.length() >= 32 && digest.matches( "^[a-z0-9]+$" );
    }

    /**
     * Reads up a hash as string from StorageFileItem pointing to .sha1/.md5 files.
     * 
     * @param inputFileItem
     * @return
     * @throws IOException
     */
    public static String readDigestFromFileItem( final StorageFileItem inputFileItem )
        throws IOException
    {
        return readDigestFromStream( inputFileItem.getInputStream() );
    }
}
