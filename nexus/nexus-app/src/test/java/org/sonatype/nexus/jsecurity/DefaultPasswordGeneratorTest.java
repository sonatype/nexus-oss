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
package org.sonatype.nexus.jsecurity;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.security.usermanagement.DefaultPasswordGenerator;
import org.sonatype.security.usermanagement.PasswordGenerator;

public class DefaultPasswordGeneratorTest
    extends PlexusTestCase
{

    @Override
    protected void customizeContainerConfiguration( ContainerConfiguration configuration )
    {
        configuration.setAutoWiring( true );
        configuration.setClassPathScanning( PlexusConstants.SCANNING_ON );
    }

    protected DefaultPasswordGenerator pwGenerator;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        pwGenerator = (DefaultPasswordGenerator) this.lookup( PasswordGenerator.class );
    }
    
    public void testGeneratePassword()
        throws Exception
    {
        String pw = pwGenerator.generatePassword( 10, 10 );
        
        assertTrue( pw != null );
        assertTrue( pw.length() == 10 );
        
        String encrypted = pwGenerator.hashPassword( pw );
        String encrypted2 = pwGenerator.hashPassword( pw );
        
        assertTrue( encrypted != null );
        assertTrue( encrypted2 != null );
        assertFalse( pw.equals( encrypted ) );
        assertFalse( pw.equals( encrypted2 ) );
        assertTrue( encrypted.equals( encrypted2 ) );
        
        String newPw = pwGenerator.generatePassword( 10, 10 );
        
        assertTrue( newPw != null );
        assertTrue( newPw.length() == 10 );
        assertFalse( pw.equals( newPw ) );
        
        String newEncrypted = pwGenerator.hashPassword( newPw );
        String newEncrypted2 = pwGenerator.hashPassword( newPw );
        
        assertTrue( newEncrypted != null );
        assertTrue( newEncrypted2 != null );
        assertFalse( newPw.equals( newEncrypted ) );
        assertFalse( newPw.equals( newEncrypted2 ) );
        assertTrue( newEncrypted.equals( newEncrypted2 ) );
        assertFalse( encrypted.equals( newEncrypted ) );
    }
}
