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
