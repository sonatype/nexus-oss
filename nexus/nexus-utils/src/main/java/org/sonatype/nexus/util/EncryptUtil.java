package org.sonatype.nexus.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.codehaus.plexus.util.IOUtil;

import cryptix.util.mime.Base64InputStream;
import cryptix.util.mime.Base64OutputStream;

public class EncryptUtil
{

    public static void generateKeys( OutputStream publicKeyOut, OutputStream privateKeyOut )
        throws GeneralSecurityException, IOException
    {
        try
        {
            OutputStream publicOut = new Base64OutputStream( publicKeyOut );
            OutputStream privateOut = new Base64OutputStream( privateKeyOut );

            KeyPairGenerator generator = KeyPairGenerator.getInstance( "RSA" );

            SecureRandom random = SecureRandom.getInstance( "SHA1PRNG" );
            generator.initialize( 1024, random );

            KeyPair keyPair = generator.generateKeyPair();

            PrivateKey privateKey = keyPair.getPrivate();
            privateOut.write( privateKey.getEncoded() );
            IOUtil.close( privateOut );

            PublicKey publicKey = keyPair.getPublic();
            publicOut.write( publicKey.getEncoded() );
            IOUtil.close( publicOut );
        }
        finally
        {
            IOUtil.close( publicKeyOut );
            IOUtil.close( privateKeyOut );
        }
    }

    public static PublicKey readPublicKey( InputStream keyInput )
        throws IOException, GeneralSecurityException
    {

        try
        {
            InputStream input = new Base64InputStream( keyInput );
            byte[] encKey = IOUtil.toByteArray( input );
            IOUtil.close( input );

            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec( encKey );
            KeyFactory keyFactory = KeyFactory.getInstance( "RSA" );
            PublicKey pubKey = keyFactory.generatePublic( pubKeySpec );

            return pubKey;
        }
        finally
        {
            IOUtil.close( keyInput );
        }
    }

    public static PrivateKey readPrivateKey( InputStream keyInput )
        throws IOException, GeneralSecurityException
    {
        try
        {
            InputStream input = new Base64InputStream( keyInput );
            byte[] encKey = IOUtil.toByteArray( input );
            IOUtil.close( input );

            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec( encKey );
            KeyFactory keyFactory = KeyFactory.getInstance( "RSA" );
            PrivateKey privateKey = keyFactory.generatePrivate( privateKeySpec );

            return privateKey;
        }
        finally
        {
            IOUtil.close( keyInput );
        }
    }

    public static void encrypt( InputStream plainInput, OutputStream encryptedOutput, InputStream publickKey )
        throws IOException, GeneralSecurityException
    {
        PublicKey key = readPublicKey( publickKey );
        encrypt( plainInput, encryptedOutput, key );
    }

    public static void encrypt( InputStream plainInput, OutputStream encryptedOutput, PublicKey key )
        throws IOException, GeneralSecurityException
    {

        try
        {
            byte[] data = IOUtil.toByteArray( plainInput );
            byte[] encrypted = getCipher( key, javax.crypto.Cipher.ENCRYPT_MODE ).doFinal( data );

            Base64OutputStream output = new Base64OutputStream( encryptedOutput );
            IOUtil.copy( encrypted, output );
            IOUtil.close( output );
        }
        finally
        {
            IOUtil.close( plainInput );
            IOUtil.close( encryptedOutput );
        }
    }

    public static void decrypt( InputStream encryptedInput, OutputStream plainOutput, InputStream secretKey )
        throws IOException, GeneralSecurityException
    {
        PrivateKey key = readPrivateKey( secretKey );
        decrypt( encryptedInput, plainOutput, key );
    }

    public static void decrypt( InputStream encryptedInput, OutputStream plainOutput, PrivateKey key )
        throws IOException, GeneralSecurityException
    {
        try
        {
            Base64InputStream input = new Base64InputStream( encryptedInput );
            byte[] encrypted = IOUtil.toByteArray( input );
            IOUtil.close( input );

            byte[] data = getCipher( key, javax.crypto.Cipher.DECRYPT_MODE ).doFinal( encrypted );

            IOUtil.copy( data, plainOutput );
        }
        finally
        {
            IOUtil.close( encryptedInput );
            IOUtil.close( plainOutput );
        }
    }

    private static Cipher getCipher( Key key, int cipherMode )
        throws GeneralSecurityException
    {
        Cipher cipher = Cipher.getInstance( "RSA/ECB/PKCS1Padding" );
        cipher.init( cipherMode, key );

        return cipher;
    }
}
