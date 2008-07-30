/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.configuration.security;

import org.sonatype.nexus.AbstractNexusTestCase;

public class DefaultPasswordGeneratorTest
    extends AbstractNexusTestCase
{
    protected DefaultPasswordGenerator pwGenerator;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        pwGenerator = (DefaultPasswordGenerator) this.lookup( PasswordGenerator.ROLE );
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
