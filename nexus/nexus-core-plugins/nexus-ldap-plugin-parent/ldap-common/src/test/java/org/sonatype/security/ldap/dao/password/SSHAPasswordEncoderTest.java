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
package org.sonatype.security.ldap.dao.password;

import java.io.UnsupportedEncodingException;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;

public class SSHAPasswordEncoderTest
    extends PlexusTestCase
{
    public void testVerify()
        throws Exception
    {
        String encPassword = "{SSHA}FBProvj7X/SW+7nYtd83uX/noSQ6reGv";

        SSHAPasswordEncoder encoder = new SSHAPasswordEncoder();

        Assert.assertTrue( encoder.isPasswordValid( encPassword, "password", null ) );
        Assert.assertTrue( encoder.isPasswordValid( "{ssha}FBProvj7X/SW+7nYtd83uX/noSQ6reGv", "password", null ) );
        Assert.assertFalse( encoder.isPasswordValid(
            "{ssha}FBProvj7X/SW+7nYtd83uX/noSQ6reGv",
            "FBProvj7X/SW+7nYtd83uX/noSQ6reGv",
            null ) );
        Assert.assertFalse( encoder.isPasswordValid( encPassword, "Password", null ) );
        Assert.assertFalse( encoder.isPasswordValid( encPassword, "junk", null ) );
        Assert.assertFalse( encoder.isPasswordValid( encPassword, "", null ) );
        Assert.assertFalse( encoder.isPasswordValid( encPassword, null, null ) );

        Assert.assertTrue( encoder.isPasswordValid( "FBProvj7X/SW+7nYtd83uX/noSQ6reGv", "password", null ) );
        Assert.assertFalse( encoder.isPasswordValid( "notValid", "password", null ) );
    }

    public void testEncode()
        throws Exception
    {
        SSHAPasswordEncoder encoder = new SSHAPasswordEncoder();
        byte[] salt = new byte[] { 58, -83, -31, -81 };

        Assert.assertEquals( "{SSHA}FBProvj7X/SW+7nYtd83uX/noSQ6reGv", encoder.encodePassword( "password", salt ) );

        try
        {
            // salt must be byte[], this salt below is string and should throw IAE
            encoder.encodePassword( "password", ":abc" );
            Assert.fail( "Expected IllegalArgumentException" );
        }
        catch ( IllegalArgumentException e )
        {
            // expected
        }

        String clearPass = "foobar";
        Assert.assertTrue( encoder.isPasswordValid( encoder.encodePassword( clearPass, null ), clearPass, null ) );
        Assert.assertTrue( encoder.isPasswordValid(
            encoder.encodePassword( clearPass, "byte[]".getBytes() ),
            clearPass,
            null ) );

        try
        {
            encoder.encodePassword( clearPass, new Object() );
            Assert.fail( "expected: IllegalArgumentException" );
        }
        catch ( IllegalArgumentException e )
        {
            // expected
        }
    }
}
