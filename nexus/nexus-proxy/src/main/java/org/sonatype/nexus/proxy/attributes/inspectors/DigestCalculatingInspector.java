/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
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
    public static String DIGEST_MD5_KEY = "digest.md5";

    /** The digest sha1 key. */
    public static String DIGEST_SHA1_KEY = "digest.sha1";

    public Set<String> getIndexableKeywords()
    {
        Set<String> result = new HashSet<String>( 2 );
        result.add( DIGEST_MD5_KEY );
        result.add( DIGEST_SHA1_KEY );
        return result;
    }

    public boolean isHandled( StorageItem item )
    {
        // handling all files
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
