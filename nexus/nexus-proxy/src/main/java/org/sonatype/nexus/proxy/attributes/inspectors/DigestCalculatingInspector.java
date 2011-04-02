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
package org.sonatype.nexus.proxy.attributes.inspectors;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.attributes.AbstractStorageFileItemInspector;
import org.sonatype.nexus.proxy.attributes.StorageFileItemInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * The Class DigestCalculatingInspector calculates MD5 and SHA1 digests of a file and stores them into extended
 * attributes.
 * 
 * @author cstamas
 */
@Component( role = StorageFileItemInspector.class, hint = "digest" )
public class DigestCalculatingInspector
    extends AbstractStorageFileItemInspector
{

    /** The digest md5 key. */
    @Deprecated
    public static String DIGEST_MD5_KEY = RequestContext.CTX_DIGEST_MD5_KEY;

    /** The digest sha1 key. */
    public static String DIGEST_SHA1_KEY = RequestContext.CTX_DIGEST_SHA1_KEY;

    public Set<String> getIndexableKeywords()
    {
        Set<String> result = new HashSet<String>( 2 );
        result.add( DIGEST_MD5_KEY );
        result.add( DIGEST_SHA1_KEY );
        return result;
    }

    public boolean isHandled( StorageItem item )
    {
        if ( item instanceof StorageFileItem )
        {
            if ( item.getItemContext().containsKey( RequestContext.CTX_DIGEST_SHA1_KEY ) )
            {
                item.getAttributes().put( DIGEST_SHA1_KEY,
                    String.valueOf( item.getItemContext().get( RequestContext.CTX_DIGEST_SHA1_KEY ) ) );

                // do this one "blindly"
                item.getAttributes().put( DIGEST_MD5_KEY,
                    String.valueOf( item.getItemContext().get( RequestContext.CTX_DIGEST_MD5_KEY ) ) );

                // we did our job, we "lifted" the digest from context
                return false;
            }

        }

        // handling all files otherwise
        return true;
    }

    public void processStorageFileItem( StorageFileItem item, File file )
        throws Exception
    {
        InputStream fis = new FileInputStream( file );
        try
        {
            byte[] buffer = new byte[1024];
            MessageDigest md5 = MessageDigest.getInstance( "MD5" );
            MessageDigest sha1 = MessageDigest.getInstance( "SHA1" );
            int numRead;
            do
            {
                numRead = fis.read( buffer );
                if ( numRead > 0 )
                {
                    md5.update( buffer, 0, numRead );
                    sha1.update( buffer, 0, numRead );
                }
            }
            while ( numRead != -1 );
            String md5digestStr = new String( Hex.encodeHex( md5.digest() ) );
            String sha1DigestStr = new String( Hex.encodeHex( sha1.digest() ) );
            item.getAttributes().put( DIGEST_MD5_KEY, md5digestStr );
            item.getAttributes().put( DIGEST_SHA1_KEY, sha1DigestStr );
        }
        finally
        {
            fis.close();
        }
    }

}
