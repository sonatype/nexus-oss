/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao.password;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.ldap.dao.password.hash.MD5Crypt;


/**
 * @author cstamas
 */
@Component(role=PasswordEncoder.class, hint="crypt")
public class MD5CryptPasswordEncoder
    implements PasswordEncoder
{

    public String getMethod()
    {
        return "CRYPT";
    }

    public String encodePassword( String password, Object salt )
    {
        return "{CRYPT}" + MD5Crypt.unixMD5( password );
    }

    public boolean isPasswordValid( String encPassword, String inputPassword, Object salt )
    {
        String encryptedPassword = encPassword;
        if ( encryptedPassword.startsWith( "{crypt}" ) || encryptedPassword.startsWith( "{CRYPT}" ) )
        {
            encryptedPassword = encryptedPassword.substring( "{crypt}".length() );
        }

        int lastDollar = encryptedPassword.lastIndexOf( '$' );
        String realSalt = encryptedPassword.substring( "$1$".length(), lastDollar );

        String check = MD5Crypt.unixMD5( inputPassword, realSalt );

        return check.equals( encryptedPassword );
    }

}
