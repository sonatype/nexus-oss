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
@Component(role=PasswordEncoder.class, hint="sha")
public class SHA1PasswordEncoder
    implements PasswordEncoder
{

    public String getMethod()
    {
        return "SHA";
    }

    public String encodePassword( String password, Object salt )
    {
        return "{SHA}" + encodeString( password );
    }

    public boolean isPasswordValid( String encPassword, String inputPassword, Object salt )
    {
        String encryptedPassword = encPassword;
        if ( encryptedPassword.startsWith( "{SHA}" ) || encryptedPassword.startsWith( "{sha}" ) )
        {
            encryptedPassword = encryptedPassword.substring( "{sha}".length() );
        }

        String check = encodePassword( inputPassword, salt );

        return check.equals( encryptedPassword );
    }

    protected String encodeString( String input )
    {
        InputStream is = new ByteArrayInputStream( input.getBytes() );
        String result = null;
        try
        {
            byte[] buffer = new byte[1024];
            MessageDigest md = MessageDigest.getInstance( "SHA1" );
            int numRead;
            do
            {
                numRead = is.read( buffer );
                if ( numRead > 0 )
                {
                    md.update( buffer, 0, numRead );
                }
            }
            while ( numRead != -1 );
            result = new String( Hex.encode( md.digest() ) );
        }
        catch ( Exception e )
        {
        }
        return result;
    }

}
