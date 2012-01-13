/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.security.ldap.dao.password;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.security.ldap.dao.password.hash.MD5Crypt;

import java.security.NoSuchAlgorithmException;


/**
 * @author cstamas
 */
@Component(role=PasswordEncoder.class, hint="crypt")
public class MD5CryptPasswordEncoder
    implements PasswordEncoder
{
    final private MD5Crypt md5Crypt = new MD5Crypt();

    public String getMethod()
    {
        return "CRYPT";
    }

    public String encodePassword( String password, Object salt )
    {
        try
        {
            return "{CRYPT}" + md5Crypt.crypt( password );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( "No MD5 Algorithm", e );
        }
    }

    public boolean isPasswordValid( String encPassword, String inputPassword, Object salt )
    {
        try
        {
            String encryptedPassword = encPassword;
            if ( encryptedPassword.startsWith( "{crypt}" ) || encryptedPassword.startsWith( "{CRYPT}" ) )
            {
                encryptedPassword = encryptedPassword.substring( "{crypt}".length() );
            }

            int lastDollar = encryptedPassword.lastIndexOf( '$' );
            String realSalt = encryptedPassword.substring( "$1$".length(), lastDollar );

            String check = md5Crypt.crypt( inputPassword, realSalt );

            return check.equals( encryptedPassword );
        }
        catch ( NoSuchAlgorithmException e )
        {
            throw new RuntimeException( "No MD5 Algorithm", e );
        }
    }

}
