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
            encoder.encodePassword( "password", ":нсп" );
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
