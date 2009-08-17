package org.sonatype.nexus.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

import junit.framework.TestCase;

import org.codehaus.plexus.util.IOUtil;

public class EncryptUtilTest
    extends TestCase
{

    private static final String ENCRYPTED_TEXT;

    private static final String PUBLIC_KEY;

    private static final String PRIVATE_KEY;

    static
    {
        try
        {
            ENCRYPTED_TEXT = IOUtil.toString( EncryptUtilTest.class.getResourceAsStream( "/text.enc" ) );
            PUBLIC_KEY = IOUtil.toString( EncryptUtilTest.class.getResourceAsStream( "/UT-public-key.txt" ) );
            PRIVATE_KEY = IOUtil.toString( EncryptUtilTest.class.getResourceAsStream( "/UT-secret-key.txt" ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public void testGenerateKeys()
        throws GeneralSecurityException, IOException
    {
        ByteArrayOutputStream publicKeyOut = new ByteArrayOutputStream();
        ByteArrayOutputStream privateKeyOut = new ByteArrayOutputStream();

        EncryptUtil.generateKeys( publicKeyOut, privateKeyOut );

        byte[] publicBytes = publicKeyOut.toByteArray();
        byte[] privateBytes = privateKeyOut.toByteArray();
        assertFalse( publicBytes.length == 0 );
        assertFalse( privateBytes.length == 0 );
        assertTrue( privateBytes.length > publicBytes.length );

        assertFalse( new String( publicBytes ).equals( PUBLIC_KEY ) );
        assertFalse( new String( privateBytes ).equals( PRIVATE_KEY ) );

        // System.out.println( "Public key:\n" + new String( publicBytes ) );
        // System.out.println( "Private key:\n" + new String( privateBytes ) );
        //
        // final ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        // EncryptUtil.encrypt( new ByteArrayInputStream( "Simple decryption test!".getBytes() ), encryptedOut,
        // new ByteArrayInputStream( publicBytes ) );
        //
        // String encryptedText = new String( encryptedOut.toByteArray() );
        // System.out.println( "Encrypted text:\n" + encryptedText );
    }

    public void testKeyRead()
        throws Exception
    {
        PublicKey publicKey =
            EncryptUtil.readPublicKey( new ByteArrayInputStream( EncryptUtilTest.PUBLIC_KEY.getBytes() ) );
        assertNotNull( publicKey );

        PrivateKey privateKey =
            EncryptUtil.readPrivateKey( new ByteArrayInputStream( EncryptUtilTest.PRIVATE_KEY.getBytes() ) );
        assertNotNull( privateKey );
    }

    public void testDecryptTest()
        throws Exception
    {
        final ByteArrayOutputStream plainOutput = new ByteArrayOutputStream();
        EncryptUtil.decrypt( new ByteArrayInputStream( ENCRYPTED_TEXT.getBytes() ), plainOutput,
                             new ByteArrayInputStream( PRIVATE_KEY.getBytes() ) );

        String decryptedText = new String( plainOutput.toByteArray() );
        assertEquals( "Simple decryption test!", decryptedText );
    }

    public void testEncryptTest()
        throws Exception
    {
        String textToEncrypt = "This is a simple text to be encrypted!!!";

        final ByteArrayOutputStream encryptedOut = new ByteArrayOutputStream();
        EncryptUtil.encrypt( new ByteArrayInputStream( textToEncrypt.getBytes() ), encryptedOut,
                             new ByteArrayInputStream( PUBLIC_KEY.getBytes() ) );

        String encryptedText = new String( encryptedOut.toByteArray() );
        assertFalse( textToEncrypt.equals( encryptedText ) );

        final ByteArrayOutputStream plainOutput = new ByteArrayOutputStream();
        EncryptUtil.decrypt( new ByteArrayInputStream( encryptedOut.toByteArray() ), plainOutput,
                             new ByteArrayInputStream( PRIVATE_KEY.getBytes() ) );

        String decryptedText = new String( plainOutput.toByteArray() );
        assertEquals( textToEncrypt, decryptedText );
    }

}
