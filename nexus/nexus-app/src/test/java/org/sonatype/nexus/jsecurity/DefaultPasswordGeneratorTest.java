/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.jsecurity;

import org.codehaus.plexus.PlexusTestCase;

public class DefaultPasswordGeneratorTest
    extends PlexusTestCase
{
    protected DefaultPasswordGenerator pwGenerator;

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
