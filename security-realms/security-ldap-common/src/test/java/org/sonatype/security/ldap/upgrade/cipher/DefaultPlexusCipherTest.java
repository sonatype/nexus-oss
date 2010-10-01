package org.sonatype.security.ldap.upgrade.cipher;

/*
 * Copyright (C) 2008 Sonatype Inc.
 * Sonatype Inc, licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.security.ldap.upgrade.cipher.CryptoUtils;
import org.sonatype.security.ldap.upgrade.cipher.PlexusCipher;
import org.sonatype.security.ldap.upgrade.cipher.TestPlexusCipher;

/**
 * Test the Plexus Cipher container
 *
 * @author Oleg Gusakov
 * @version $Id$
 */
public class DefaultPlexusCipherTest
    extends PlexusTestCase
{
    private String passPhrase = "foofoo";

    String str = "my testing phrase";

    String encStr = "CFUju8n8eKQHj8u0HI9uQMRmKQALtoXH7lY=";

    TestPlexusCipher pc;

    // -------------------------------------------------------------
    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();

        pc = (TestPlexusCipher) lookup( PlexusCipher.class, "test" );
    }

    // -------------------------------------------------------------
    public void testDefaultAlgorithmExists()
        throws Exception
    {
        if ( StringUtils.isEmpty( pc.algorithm ) )
            throw new Exception( "No default algoritm found in DefaultPlexusCipher" );

        String[] res = CryptoUtils.getCryptoImpls( "Cipher" );
        assertNotNull( "No Cipher providers found in the current environment", res );

        for ( String provider : res )
            if ( pc.algorithm.equalsIgnoreCase( provider ) )
                return;

        throw new Exception( "Cannot find default algorithm " + pc.algorithm + " in the current environment." );
    }

    // -------------------------------------------------------------
    public void stestFindDefaultAlgorithm()
        throws Exception
    {
        String[] res = CryptoUtils.getServiceTypes();
        assertNotNull( "No Cipher providers found in the current environment", res );

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

    // -------------------------------------------------------------
    public void testDecrypt()
        throws Exception
    {
        String res = pc.decrypt( encStr, passPhrase );
        assertEquals( "Decryption did not produce desired result", str, res );
    }

    // -------------------------------------------------------------
    public void testEncrypt()
        throws Exception
    {
        String xRes = pc.encrypt( str, passPhrase );
        String res = pc.decrypt( xRes, passPhrase );
        assertEquals( "Encryption/Decryption did not produce desired result", str, res );
    }

    // -------------------------------------------------------------
    public void testDecorate()
        throws Exception
    {
        String res = pc.decorate( "aaa" );
        assertEquals( "Decoration failed", PlexusCipher.ENCRYPTED_STRING_DECORATION_START + "aaa"
            + PlexusCipher.ENCRYPTED_STRING_DECORATION_STOP, res );
    }

    // -------------------------------------------------------------
    public void testUnDecorate()
        throws Exception
    {
        String res =
            pc.unDecorate( PlexusCipher.ENCRYPTED_STRING_DECORATION_START + "aaa"
                + PlexusCipher.ENCRYPTED_STRING_DECORATION_STOP );
        assertEquals( "Decoration failed", "aaa", res );
    }
    // -------------------------------------------------------------
    // -------------------------------------------------------------
}
