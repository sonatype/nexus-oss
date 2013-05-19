/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.security.usermanagement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

/**
 * A util class to calculate various digests on Strings. Usaful for some simple password management.
 * 
 * @author cstamas
 */
public class StringDigester
{

    /**
     * Calculates a digest for a String user the requested algorithm.
     * 
     * @param alg
     * @param content
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String getDigest( String alg, String content )
        throws NoSuchAlgorithmException
    {
        String result = null;

        try
        {
            InputStream fis = new ByteArrayInputStream( content.getBytes( "UTF-8" ) );

            try
            {
                byte[] buffer = new byte[1024];

                MessageDigest md = MessageDigest.getInstance( alg );

                int numRead;

                do
                {
                    numRead = fis.read( buffer );
                    if ( numRead > 0 )
                    {
                        md.update( buffer, 0, numRead );
                    }
                }
                while ( numRead != -1 );

                result = new String( Hex.encodeHex( md.digest() ) );
            }
            finally
            {
                fis.close();
            }
        }
        catch ( IOException e )
        {
            // hrm
            result = null;
        }

        return result;
    }

    /**
     * Calculates a SHA1 digest for a string.
     * 
     * @param content
     * @return
     */
    public static String getSha1Digest( String content )
    {
        try
        {
            return getDigest( "SHA1", content );
        }
        catch ( NoSuchAlgorithmException e )
        {
            // will not happen
            return null;
        }
    }

    /**
     * Calculates MD5 digest for a string.
     * 
     * @param content
     * @return
     */
    public static String getMd5Digest( String content )
    {
        try
        {
            return getDigest( "MD5", content );
        }
        catch ( NoSuchAlgorithmException e )
        {
            // will not happen
            return null;
        }
    }

}
