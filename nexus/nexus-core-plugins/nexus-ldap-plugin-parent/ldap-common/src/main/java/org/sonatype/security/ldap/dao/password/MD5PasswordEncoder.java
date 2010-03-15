/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao.password;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.digest.Hex;

/**
 * @author cstamas
 */
@Component( role = PasswordEncoder.class, hint = "md5" )
public class MD5PasswordEncoder
    implements PasswordEncoder
{

    public String getMethod()
    {
        return "MD5";
    }

    public String encodePassword( String password, Object salt )
    {
        return "{MD5}" + encodeString( password );
    }

    public boolean isPasswordValid( String encPassword, String inputPassword, Object salt )
    {
        String encryptedPassword = this.stripHeader( encPassword );
        String check = this.stripHeader( encodePassword( inputPassword, salt ) );

        return check.equals( encryptedPassword );
    }
    
    protected String stripHeader( String encryptedPassword )
    {
        if ( encryptedPassword.startsWith( "{" + getMethod().toUpperCase() + "}" )
            || encryptedPassword.startsWith( "{" + getMethod().toLowerCase() + "}" ) )
        {
            encryptedPassword = encryptedPassword.substring( "{MD5}".length() );
        }
        return encryptedPassword;
    }


    protected String encodeString( String input )
    {
        InputStream is = new ByteArrayInputStream( input.getBytes() );
        String result = null;
        try
        {
            byte[] buffer = new byte[1024];
            MessageDigest md5 = MessageDigest.getInstance( "MD5" );
            int numRead;
            do
            {
                numRead = is.read( buffer );
                if ( numRead > 0 )
                {
                    md5.update( buffer, 0, numRead );
                }
            }
            while ( numRead != -1 );
            result = new String( Hex.encode( md5.digest() ) );
        }
        catch ( Exception e )
        {
        }
        return result;
    }

}
