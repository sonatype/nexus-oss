/**
 * Sonatype Nexus (TM) Professional Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions/.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.security.ldap.dao.password;

import org.codehaus.plexus.component.annotations.Component;

/**
 * @author tstevens
 */
@Component( role=PasswordEncoder.class, hint="clear" )
public class ClearTextPasswordEncoder
    implements PasswordEncoder
{

    public String getMethod()
    {
        return "CLEAR";
    }

    public String encodePassword( String password, Object salt )
    {
        return password;
    }

    public boolean isPasswordValid( String encPassword, String inputPassword, Object salt )
    {
        String encryptedPassword = encPassword;
        if ( encryptedPassword.startsWith( "{CLEAR}" ) || encryptedPassword.startsWith( "{clear}" ) )
        {
            encryptedPassword = encryptedPassword.substring( "{clear}".length() );
        }

        String check = encodePassword( inputPassword, salt );

        return check.equals( encryptedPassword );
    }
}
