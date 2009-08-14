package org.sonatype.nexus.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Iterator;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.codehaus.plexus.util.IOUtil;

public class EncryptUtil
{

    private static final String PROVIDER = "BC";

    static
    {
        Security.addProvider( new BouncyCastleProvider() );
    }

    @SuppressWarnings( "unchecked" )
    public static PGPPublicKey readPublicKey( InputStream keyInput )
        throws IOException, PGPException
    {
        keyInput = PGPUtil.getDecoderStream( keyInput );

        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection( keyInput );

        Iterator<PGPPublicKeyRing> rIt = pgpPub.getKeyRings();

        while ( rIt.hasNext() )
        {
            PGPPublicKeyRing kRing = rIt.next();
            Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();

            while ( kIt.hasNext() )
            {
                PGPPublicKey k = kIt.next();

                if ( k.isEncryptionKey() )
                {
                    return k;
                }
            }
        }

        throw new IllegalArgumentException( "Can't find encryption key in key ring." );
    }

    public static PGPPrivateKey readPrivateKey( InputStream keyInput, long id, char[] passphrase )
        throws IOException, PGPException
    {
        keyInput = PGPUtil.getDecoderStream( keyInput );

        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection( keyInput );

        PGPSecretKey key = pgpSec.getSecretKey( id );

        if ( key == null )
        {
            throw new IllegalArgumentException( "Can't find encryption key in key ring." );
        }

        PGPPrivateKey privateKey;
        try
        {
            privateKey = key.extractPrivateKey( passphrase, PROVIDER );
        }
        catch ( NoSuchProviderException e )
        {
            // should never happen
            throw new RuntimeException( e.getMessage(), e );
        }
        return privateKey;
    }

    public static void encrypt( InputStream plainInput, OutputStream encryptedOutput, InputStream publickKey )
        throws IOException, PGPException
    {
        PGPPublicKey key = readPublicKey( publickKey );
        encrypt( plainInput, encryptedOutput, key );
    }

    public static void encrypt( InputStream plainInput, OutputStream encryptedOutput, PGPPublicKey key )
        throws IOException, PGPException
    {
        try
        {
            encryptedOutput = new ArmoredOutputStream( encryptedOutput );

            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator( CompressionAlgorithmTags.ZIP );
            IOUtil.copy( plainInput, comData.open( bOut ) );
            comData.close();

            PGPEncryptedDataGenerator cPk =
                new PGPEncryptedDataGenerator( SymmetricKeyAlgorithmTags.CAST5, true, new SecureRandom(), PROVIDER );

            try
            {
                cPk.addMethod( key );
            }
            catch ( NoSuchProviderException e )
            {
                // should never happen
                throw new RuntimeException( e.getMessage(), e );
            }

            byte[] bytes = bOut.toByteArray();
            OutputStream cOut = cPk.open( encryptedOutput, bytes.length );
            cOut.write( bytes );
            cOut.close();
        }
        finally
        {
            IOUtil.close( plainInput );
            IOUtil.close( encryptedOutput );
        }
    }

    @SuppressWarnings( "unchecked" )
    public static void decrypt( InputStream encryptedInput, OutputStream plainOutput, InputStream secretKey,
                                char[] passPhrase )
        throws IOException, PGPException
    {
        try
        {
            encryptedInput = PGPUtil.getDecoderStream( encryptedInput );

            PGPObjectFactory pgpF = new PGPObjectFactory( encryptedInput );
            PGPEncryptedDataList enc;

            Object o = pgpF.nextObject();
            //
            // the first object might be a PGP marker packet.
            //
            if ( o instanceof PGPEncryptedDataList )
            {
                enc = (PGPEncryptedDataList) o;
            }
            else
            {
                enc = (PGPEncryptedDataList) pgpF.nextObject();
            }

            PGPPublicKeyEncryptedData pbe = null;
            PGPPrivateKey key = null;

            Iterator<PGPPublicKeyEncryptedData> it = enc.getEncryptedDataObjects();

            while ( key == null && it.hasNext() )
            {
                pbe = it.next();
                key = readPrivateKey( secretKey, pbe.getKeyID(), passPhrase );
            }

            if ( key == null )
            {
                throw new IllegalArgumentException( "secret key for message not found." );
            }

            InputStream clear;
            try
            {
                clear = pbe.getDataStream( key, PROVIDER );
            }
            catch ( NoSuchProviderException e )
            {
                // should never happen
                throw new RuntimeException( e.getMessage(), e );
            }

            PGPObjectFactory pgpFact = new PGPObjectFactory( clear );
            Object message = pgpFact.nextObject();

            if ( message instanceof PGPCompressedData )
            {
                PGPCompressedData cData = (PGPCompressedData) message;
                pgpFact = new PGPObjectFactory( cData.getDataStream() );

                message = pgpFact.nextObject();
            }

            if ( message instanceof PGPLiteralData )
            {
                PGPLiteralData ld = (PGPLiteralData) message;

                InputStream unc = ld.getInputStream();
                IOUtil.copy( unc, plainOutput );
            }
            else if ( message instanceof PGPOnePassSignatureList )
            {
                throw new PGPException( "encrypted message contains a signed message - not literal data." );
            }
            else
            {
                throw new PGPException( "message is not a simple encrypted file - type unknown." );
            }

            if ( pbe.isIntegrityProtected() )
            {
                if ( !pbe.verify() )
                {
                    throw new PGPException( "message failed integrity check" );
                }
            }
        }
        finally
        {
            IOUtil.close( encryptedInput );
            IOUtil.close( plainOutput );
        }
    }

}
