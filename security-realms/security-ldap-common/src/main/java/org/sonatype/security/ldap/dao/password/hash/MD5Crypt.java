/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao.password.hash;

import java.util.Random;

/**
 * Unix MD5 encryption, used for /etc/passwd style passwords.
 * 
 * @author Kenney Westerhof
 */
public class MD5Crypt
{
    private static final String SALTCHARS = "abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    private static final String ITOA64 = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz";

    private MD5Crypt()
    {
        // prevent instantiation
    }

    private static int bytes2u( byte in )
    {
        return (int) in & 0xff;
    }

    private static String to64( long v, int size )
    {
        String result = "";
        while ( --size >= 0 )
        {
            result += ITOA64.charAt( (int) ( v & 0x3f ) );
            v >>>= 6;
        }
        return result;
    }

    public static String unixMD5( final String s )
    {
        Random r = new Random();

        String salt = "";

        while ( salt.length() < 8 )
        {
            salt += SALTCHARS.charAt( (int) ( r.nextFloat() * SALTCHARS.length() ) );
        }

        return unixMD5( s, salt );
    }

    public static String unixMD5( String s, String salt )
    {
        return unixMD5( s, salt, "$1$" );
    }

    public static String unixMD5( String s, String salt, String magic )
    {
        if ( salt.startsWith( magic ) )
            salt = salt.substring( magic.length() );
        if ( salt.indexOf( "$" ) >= 0 )
            salt = salt.substring( 0, salt.indexOf( "$" ) );
        if ( salt.length() > 8 )
            salt = salt.substring( 0, 8 );

        MD5 md5 = new MD5();
        md5.update( s );
        md5.update( "$1$" );
        md5.update( salt );

        MD5 md52 = new MD5();
        md52.update( s );
        md52.update( salt );
        md52.update( s );
        byte[] fin = md52.digest();

        for ( int p = s.length(); p > 0; p -= 16 )
        {
            md5.update( fin, 0, p > 16 ? 16 : p );
        }

        for ( int i = 0; i < fin.length; i++ )
            fin[i] = 0;

        for ( int i = s.length(); i != 0; i >>>= 1 )
        {
            if ( ( i & 1 ) != 0 )
                md5.update( fin, 0, 1 );
            else
                md5.update( s.getBytes(), 0, 1 );
        }

        fin = md5.digest();

        for ( int i = 0; i < 1000; i++ )
        {
            md5 = new MD5();

            if ( ( i & 1 ) != 0 )
                md5.update( s );
            else
                md5.update( fin, 0, 16 );

            if ( ( i % 3 ) != 0 )
                md5.update( salt );

            if ( ( i % 7 ) != 0 )
                md5.update( s );

            if ( ( i & 1 ) != 0 )
                md5.update( fin, 0, 16 );
            else
                md5.update( s );

            fin = md5.digest();
        }

        // make output string
        String result = "$1$" + salt + "$";

        result += to64( ( bytes2u( fin[0] ) << 16 ) | ( bytes2u( fin[6] ) << 8 ) | bytes2u( fin[12] ), 4 );

        result += to64( ( bytes2u( fin[1] ) << 16 ) | ( bytes2u( fin[7] ) << 8 ) | bytes2u( fin[13] ), 4 );

        result += to64( ( bytes2u( fin[2] ) << 16 ) | ( bytes2u( fin[8] ) << 8 ) | bytes2u( fin[14] ), 4 );

        result += to64( ( bytes2u( fin[3] ) << 16 ) | ( bytes2u( fin[9] ) << 8 ) | bytes2u( fin[15] ), 4 );

        result += to64( ( bytes2u( fin[4] ) << 16 ) | ( bytes2u( fin[10] ) << 8 ) | bytes2u( fin[5] ), 4 );

        result += to64( bytes2u( fin[11] ), 2 );

        for ( int i = 0; i < fin.length; i++ )
            fin[i] = 0;

        return result;
    }
}
