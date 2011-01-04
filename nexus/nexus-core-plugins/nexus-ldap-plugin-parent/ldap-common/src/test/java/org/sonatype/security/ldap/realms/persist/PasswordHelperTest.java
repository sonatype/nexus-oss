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
