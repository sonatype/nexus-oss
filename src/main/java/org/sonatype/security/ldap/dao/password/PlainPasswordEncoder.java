/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao.password;

import org.codehaus.plexus.component.annotations.Component;

/**
 * @author cstamas
 */
@Component( role=PasswordEncoder.class, hint="plain" )
public class PlainPasswordEncoder
    implements PasswordEncoder
{

    public String getMethod()
    {
        return "PLAIN";
    }

    public String encodePassword( String password, Object salt )
    {
        return "{PLAIN}" + password;
    }

    public boolean isPasswordValid( String encPassword, String inputPassword, Object salt )
    {
        String encryptedPassword = encPassword;
        if ( encryptedPassword.startsWith( "{PLAIN}" ) || encryptedPassword.startsWith( "{plain}" ) )
        {
            encryptedPassword = encryptedPassword.substring( "{plain}".length() );
        }

        return inputPassword.equals( encryptedPassword );
    }
}
