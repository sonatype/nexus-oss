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
package org.sonatype.nexus.security.filter.authc;

import org.apache.shiro.authc.AuthenticationToken;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

public class NexusSecureHttpAuthenticationFilter
    extends NexusHttpAuthenticationFilter
{
    private PasswordDecryptor passwordDecryptor;

    protected PasswordDecryptor getPasswordDecryptor()
    {
        if ( passwordDecryptor == null && getPlexusContainer().hasComponent( PasswordDecryptor.class ) )
        {
            try
            {
                passwordDecryptor = getPlexusContainer().lookup( PasswordDecryptor.class );
            }
            catch ( ComponentLookupException e )
            {
                // ignore it? There is none set
            }
        }

        return passwordDecryptor;
    }

    protected String decryptPasswordIfNeeded( String password )
    {
        PasswordDecryptor pd = getPasswordDecryptor();

        if ( pd != null && pd.isEncryptedPassword( password ) )
        {
            return pd.getDecryptedPassword( password );
        }
        else
        {
            return password;
        }
    }

    @Override
    protected AuthenticationToken createToken( String username, String password, boolean rememberMe, String address )
    {
        return super.createToken( username, decryptPasswordIfNeeded( password ), rememberMe, address );
    }
}
