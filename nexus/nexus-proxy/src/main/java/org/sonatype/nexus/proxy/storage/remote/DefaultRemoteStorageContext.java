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

import java.util.HashMap;

import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;

/**
 * The default remote storage context.
 * 
 * @author cstamas
 */
public class DefaultRemoteStorageContext
    implements RemoteStorageContext
{
    private final HashMap<String, Object> context = new HashMap<String, Object>();

    private long lastChanged = System.currentTimeMillis();

    private RemoteStorageContext parent;

    public DefaultRemoteStorageContext( RemoteStorageContext parent )
    {
        this.parent = parent;

        if ( parent == null )
        {
            // Note: this is needed since RemoteConnectionsSettings, in contrary to the other two (HttpProxy and Auth)
            // is _mandatory_ and is always used, while the other two is used if present, otherwise not (like
            // HttpProxy).
            // Also, when we have no "parent" (defaults, to delegate lookup to), that means that we have to create a
            // default one.
            RemoteConnectionSettings defaultRemoteConnectionSettings = new RemoteConnectionSettings();

            setRemoteConnectionSettings( defaultRemoteConnectionSettings );
        }
    }

    public long getLastChanged()
    {
        if ( parent != null )
        {
            return parent.getLastChanged() > lastChanged ? parent.getLastChanged() : lastChanged;
        }
        else
        {
            return lastChanged;
        }
    }

    protected void setLastChanged( long ts )
    {
        lastChanged = ts;
    }

    public RemoteStorageContext getParentRemoteStorageContext()
    {
        return parent;
    }

    public Object getRemoteConnectionContextObject( String key )
    {
        if ( context.containsKey( key ) )
        {
            return context.get( key );
        }
        else if ( parent != null )
        {
            return parent.getRemoteConnectionContextObject( key );
        }
        else
        {
            return null;
        }
    }

    public void putRemoteConnectionContextObject( String key, Object value )
    {
        context.put( key, value );

        setLastChanged( System.currentTimeMillis() );
    }

    public void removeRemoteConnectionContextObject( String key )
    {
        context.remove( key );

        setLastChanged( System.currentTimeMillis() );
    }

    public boolean hasRemoteConnectionContextObject( String key )
    {
        return context.containsKey( key );
    }

    public boolean hasRemoteAuthenticationSettings()
    {
        return hasRemoteConnectionContextObject( RemoteAuthenticationSettings.class.getName() );
    }

    public RemoteAuthenticationSettings getRemoteAuthenticationSettings()
    {
        return (RemoteAuthenticationSettings) getRemoteConnectionContextObject( RemoteAuthenticationSettings.class
            .getName() );
    }

    public void setRemoteAuthenticationSettings( RemoteAuthenticationSettings settings )
    {
        putRemoteConnectionContextObject( RemoteAuthenticationSettings.class.getName(), settings );
    }

    public void removeRemoteAuthenticationSettings()
    {
        removeRemoteConnectionContextObject( RemoteAuthenticationSettings.class.getName() );
    }

    public boolean hasRemoteConnectionSettings()
    {
        return hasRemoteConnectionContextObject( RemoteConnectionSettings.class.getName() );
    }

    public RemoteConnectionSettings getRemoteConnectionSettings()
    {
        return (RemoteConnectionSettings) getRemoteConnectionContextObject( RemoteConnectionSettings.class.getName() );
    }

    public void setRemoteConnectionSettings( RemoteConnectionSettings settings )
    {
        putRemoteConnectionContextObject( RemoteConnectionSettings.class.getName(), settings );
    }

    public void removeRemoteConnectionSettings()
    {
        removeRemoteConnectionContextObject( RemoteConnectionSettings.class.getName() );
    }

    public boolean hasRemoteProxySettings()
    {
        return hasRemoteConnectionContextObject( RemoteProxySettings.class.getName() );
    }

    public RemoteProxySettings getRemoteProxySettings()
    {
        return (RemoteProxySettings) getRemoteConnectionContextObject( RemoteProxySettings.class.getName() );
    }

    public void setRemoteProxySettings( RemoteProxySettings settings )
    {
        putRemoteConnectionContextObject( RemoteProxySettings.class.getName(), settings );
    }

    public void removeRemoteProxySettings()
    {
        removeRemoteConnectionContextObject( RemoteProxySettings.class.getName() );
    }
}
