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
package org.sonatype.nexus.proxy.storage.remote;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.storage.AbstractStorageContext;
import org.sonatype.nexus.proxy.storage.StorageContext;

/**
 * The default remote storage context.
 * 
 * @author cstamas
 */
public class DefaultRemoteStorageContext
    extends AbstractStorageContext
    implements RemoteStorageContext
{
    public DefaultRemoteStorageContext( StorageContext parent )
    {
        super( parent );
    }

    public boolean hasRemoteAuthenticationSettings()
    {
        return hasContextObject( RemoteAuthenticationSettings.class.getName() );
    }

    public RemoteAuthenticationSettings getRemoteAuthenticationSettings()
    {
        return (RemoteAuthenticationSettings) getContextObject( RemoteAuthenticationSettings.class.getName() );
    }

    public void setRemoteAuthenticationSettings( RemoteAuthenticationSettings settings )
    {
        putContextObject( RemoteAuthenticationSettings.class.getName(), settings );
    }

    public void removeRemoteAuthenticationSettings()
    {
        removeContextObject( RemoteAuthenticationSettings.class.getName() );
    }

    public boolean hasRemoteConnectionSettings()
    {
        return hasContextObject( RemoteConnectionSettings.class.getName() );
    }

    public RemoteConnectionSettings getRemoteConnectionSettings()
    {
        return (RemoteConnectionSettings) getContextObject( RemoteConnectionSettings.class.getName() );
    }

    public void setRemoteConnectionSettings( RemoteConnectionSettings settings )
    {
        putContextObject( RemoteConnectionSettings.class.getName(), settings );
    }

    public void removeRemoteConnectionSettings()
    {
        removeContextObject( RemoteConnectionSettings.class.getName() );
    }

    public boolean hasRemoteProxySettings()
    {
        return hasContextObject( RemoteProxySettings.class.getName() );
    }

    public RemoteProxySettings getRemoteProxySettings()
    {
        // we have a special case here, need to track blockInheritance flag
        // so, a little code duplication happens
        // three cases:
        // 1. we have _no_ proxy settings in this context, fallback to original code
        // 2. we have proxy settings with no proxyHost set, then obey the blockInheritance
        // 3. we have proxy settings with set proxyHost, then return it

        final String key = RemoteProxySettings.class.getName();

        if ( !hasContextObject( key ) )
        {
            // case 1
            return (RemoteProxySettings) getContextObject( key );
        }
        else
        {
            RemoteProxySettings remoteProxySettings = (RemoteProxySettings) getContextObject( key, false );

            if ( StringUtils.isBlank( remoteProxySettings.getHostname() ) )
            {
                // case 2
                if ( !remoteProxySettings.isBlockInheritance() )
                {
                    return (RemoteProxySettings) getContextObject( key );
                }
                else
                {
                    // no proxy on this level, and do _not_ inherit
                    return null;
                }
            }
            else
            {
                // case 3
                return remoteProxySettings;
            }
        }
    }

    public void setRemoteProxySettings( RemoteProxySettings settings )
    {
        putContextObject( RemoteProxySettings.class.getName(), settings );
    }

    public void removeRemoteProxySettings()
    {
        removeContextObject( RemoteProxySettings.class.getName() );
    }
}
