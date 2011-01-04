/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
