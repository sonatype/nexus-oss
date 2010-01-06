/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao.password;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.security.ldap.dao.password.PasswordEncoder;
import org.sonatype.security.ldap.dao.password.hash.MD5Crypt;


public class MD5CryptPasswordEncoderTest
    extends PlexusTestCase
{

    public void testEncryptAndVerify()
        throws Exception
    {
        PasswordEncoder encoder = lookup( PasswordEncoder.class, "crypt" );

        String crypted = encoder.encodePassword( "test", null );

        // System.out.println( "Crypted password: \'" + crypted + "\'" );

        int lastIdx = crypted.lastIndexOf( '$' );
        int firstIdx = crypted.indexOf( '$' );

        String salt = crypted.substring( firstIdx + "$1$".length(), lastIdx );

        String check = "{CRYPT}" + MD5Crypt.unixMD5( "test", salt );

        // System.out.println( "Check value: \'" + check + "\'" );

        assertEquals( check, crypted );

        Assert.assertTrue( encoder.isPasswordValid( crypted, "test", null ) );
    }

}
