/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.realms.persist;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.security.ldap.realms.persist.PasswordHelper;
import org.sonatype.security.ldap.upgrade.cipher.PlexusCipherException;


public class PasswordHelperTest
    extends PlexusTestCase
{

    public PasswordHelper getPasswordHelper()
        throws Exception
    {
        return (PasswordHelper) this.lookup( PasswordHelper.class );
    }

    public void testValidPass()
        throws Exception
    {
        PasswordHelper ph = this.getPasswordHelper();

        String password = "PASSWORD";
        String encodedPass = ph.encrypt( password );
        Assert.assertEquals( password, ph.decrypt( encodedPass ) );
    }

    public void testNullEncrypt()
        throws Exception
    {
        PasswordHelper ph = this.getPasswordHelper();
        Assert.assertNull( ph.encrypt( null ) );
    }

    public void testNullDecrypt()
        throws Exception
    {
        PasswordHelper ph = this.getPasswordHelper();
        Assert.assertNull( ph.decrypt( null ) );
    }

    public void testDecryptNonEncyprtedPassword()
        throws Exception
    {
        PasswordHelper ph = this.getPasswordHelper();

        try
        {
            ph.decrypt( "clear-text-password" );
            Assert.fail( "Expected: PlexusCipherException" );
        }
        catch ( PlexusCipherException e )
        {
            // expected
        }

    }

}
