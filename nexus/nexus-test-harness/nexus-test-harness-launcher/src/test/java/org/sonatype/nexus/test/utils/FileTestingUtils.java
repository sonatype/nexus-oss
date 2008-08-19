package org.sonatype.nexus.test.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.codehaus.plexus.util.InterpolationFilterReader;

/**
 * Simple File testing utilities.
 */
public class FileTestingUtils
{

    private static final int BUFFER_SIZE = 0x1000;

    private static final String SHA1 = "SHA1";

    /**
     * Creates a SHA1 hash from a file.
     * 
     * @param file The file to be digested.
     * @return An SHA1 hash based on the contents of the file.
     * @throws IOException
     */
    public static String createSHA1FromFile( File file )
        throws IOException
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( file );
            return createSHA1FromStream( fis );
        }
        finally
        {
            if ( fis != null )
                fis.close();
        }
    }

    /**
     * Creates a SHA1 hash from a url.
     * 
     * @param url The URL to opened and digested.
     * @return An SHA1 hash based on the contents of the URL.
     * @throws IOException
     */
    public static String createSHA1FromURL( URL url )
        throws IOException
    {
        InputStream is = url.openStream();
        try
        {
            return createSHA1FromStream( is );
        }
        finally
        {
            if ( is != null )
            {
                is.close();
            }
        }
    }

    /**
     * Creates a SHA1 hash from the contents of a String.
     * 
     * @param data the String to be digested.
     * @return An SHA1 hash based on the contents of the String.
     * @throws IOException
     */
    public static String createSHA1FromString( String data )
        throws IOException
    {

        ByteArrayInputStream bais = new ByteArrayInputStream( data.getBytes() );
        return createSHA1FromStream( bais );

    }

    /**
     * Creates a SHA1 hash from an InputStream.
     * 
     * @param in Inputstream to be digested.
     * @returnn SHA1 hash based on the contents of the stream.
     * @throws IOException
     */
    public static String createSHA1FromStream( InputStream in )
        throws IOException
    {

        byte[] bytes = new byte[BUFFER_SIZE];

        try
        {
            MessageDigest digest = MessageDigest.getInstance( SHA1 );
            for ( int n; ( n = in.read( bytes ) ) >= 0; )
            {
                if ( n > 0 )
                {
                    digest.update( bytes, 0, n );
                }
            }

            bytes = digest.digest();
            StringBuffer sb = new StringBuffer( bytes.length * 2 );
            for ( int i = 0; i < bytes.length; i++ )
            {
                int n = bytes[i] & 0xFF;
                if ( n < 0x10 )
                {
                    sb.append( '0' );
                }
                sb.append( Integer.toHexString( n ) );
            }

            return sb.toString();
        }
        catch ( NoSuchAlgorithmException noSuchAlgorithmException )
        {
            throw new IllegalStateException( noSuchAlgorithmException.getMessage(), noSuchAlgorithmException );
        }
    }

    public static boolean compareFileSHA1s( File file1, File file2 )
        throws IOException
    {

        if ( file1 != null && file1.exists() && file2 != null && file2.exists() )
        {
            String file1SHA1 = createSHA1FromFile( file1 );
            String file2SHA1 = createSHA1FromFile( file2 );

            return file1SHA1.equals( file2SHA1 );
        }
        return false;
    }

    @SuppressWarnings( "unchecked" )
    public static File getTestFile( Class clazz, String filename )
    {
        String resource = clazz.getName().replace( '.', '/' ) + "Resources/" + filename;
        System.out.println( "Looking for resource: " + resource );
        URL classURL = Thread.currentThread().getContextClassLoader().getResource( resource );
        System.out.println( "found: " + classURL );
        return new File( classURL.getFile() );
    }

    public static void main( String[] args )
    {
        String usage = "Usage: java " + FileTestingUtils.class + " <url>";

        if ( args == null || args.length != 1 )
        {
            System.out.println( usage );
            return;
        }

        try
        {
            URL url = new URL( args[0] );
            System.out.println( createSHA1FromURL( url ) );
        }
        catch ( Exception e )
        {
            System.out.println( usage );
            e.printStackTrace( System.out );
        }

    }

    public static void interpolationFileCopy( File from, File dest, Map<String, String> variables )
        throws IOException
    {

        // we may also need to create any parent directories
        if ( dest.getParentFile() != null && !dest.getParentFile().exists() )
        {
            dest.getParentFile().mkdirs();
        }

        FileReader fileReader = new FileReader( from );
        InterpolationFilterReader filterReader = new InterpolationFilterReader( fileReader, variables );

        FileWriter fos = new FileWriter( dest );

        int readChar = -1;
        while ( ( readChar = (int) filterReader.read() ) != -1 )
        {
            fos.write( readChar );
        }

        // close everything
        fileReader.close();
        fos.close();
    }

}
