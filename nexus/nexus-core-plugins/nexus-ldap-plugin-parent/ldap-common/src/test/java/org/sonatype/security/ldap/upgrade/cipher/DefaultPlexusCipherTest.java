/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.security.ldap.upgrade.cipher;

import org.codehaus.plexus.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.test.PlexusTestCaseSupport;

/**
 * Test the Plexus Cipher container
 *
 * @author Oleg Gusakov
 * @version $Id$
 */
public class DefaultPlexusCipherTest
    extends PlexusTestCaseSupport
{
    private String passPhrase = "foofoo";

    String str = "my testing phrase";

    String encStr = "CFUju8n8eKQHj8u0HI9uQMRmKQALtoXH7lY=";

    TestPlexusCipher pc;

    // -------------------------------------------------------------
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        pc = (TestPlexusCipher) lookup( PlexusCipher.class, "test" );
    }

    // -------------------------------------------------------------
    @Test
    public void testDefaultAlgorithmExists()
        throws Exception
    {
        if ( StringUtils.isEmpty( pc.algorithm ) )
            throw new Exception( "No default algoritm found in DefaultPlexusCipher" );

        String[] res = CryptoUtils.getCryptoImpls( "Cipher" );
        Assert.assertNotNull( "No Cipher providers found in the current environment", res );

        for ( String provider : res )
            if ( pc.algorithm.equalsIgnoreCase( provider ) )
                return;

        throw new Exception( "Cannot find default algorithm " + pc.algorithm + " in the current environment." );
    }

    // intentionally not a test?
    public void stestFindDefaultAlgorithm()
        throws Exception
    {
        String[] res = CryptoUtils.getServiceTypes();
        Assert.assertNotNull( "No Cipher providers found in the current environment", res );

        for ( String provider : CryptoUtils.getCryptoImpls( "Cipher" ) )
            try
            {
                System.out.print( provider );
                pc.algorithm = provider;
                pc.encrypt( str, passPhrase );
                System.out.println( "------------------> Success !!!!!!" );
            }
            catch ( Exception e )
            {
                System.out.println( e.getMessage() );
            }
    }

    @Test
    public void testDecrypt()
        throws Exception
    {
        String res = pc.decrypt( encStr, passPhrase );
        Assert.assertEquals( "Decryption did not produce desired result", str, res );
    }

    @Test
    public void testEncrypt()
        throws Exception
    {
        String xRes = pc.encrypt( str, passPhrase );
        String res = pc.decrypt( xRes, passPhrase );
        Assert.assertEquals( "Encryption/Decryption did not produce desired result", str, res );
    }

    @Test
    public void testDecorate()
        throws Exception
    {
        String res = pc.decorate( "aaa" );
        Assert.assertEquals( "Decoration failed", PlexusCipher.ENCRYPTED_STRING_DECORATION_START + "aaa"
            + PlexusCipher.ENCRYPTED_STRING_DECORATION_STOP, res );
    }

    @Test
    public void testUnDecorate()
        throws Exception
    {
        String res =
            pc.unDecorate( PlexusCipher.ENCRYPTED_STRING_DECORATION_START + "aaa"
                + PlexusCipher.ENCRYPTED_STRING_DECORATION_STOP );
        Assert.assertEquals( "Decoration failed", "aaa", res );
    }
}
