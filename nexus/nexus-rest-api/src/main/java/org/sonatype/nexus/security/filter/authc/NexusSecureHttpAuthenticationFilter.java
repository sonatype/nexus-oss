/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.security.filter.authc;

import java.net.InetAddress;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.jsecurity.authc.AuthenticationToken;

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
    protected AuthenticationToken createToken( String username, String password, boolean rememberMe, InetAddress inet )
    {
        return super.createToken( username, decryptPasswordIfNeeded( password ), rememberMe, inet );
    }
}
