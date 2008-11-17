/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.storage.remote;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;

/**
 * The default remote storage context.
 * 
 * @author cstamas
 */
public class DefaultRemoteStorageContext
    implements RemoteStorageContext
{
    private long lastChanged = System.currentTimeMillis();

    private HashMap<String, Object> context = new HashMap<String, Object>();

    private RemoteStorageContext defaults;

    public DefaultRemoteStorageContext( RemoteStorageContext defaults )
    {
        super();

        this.defaults = defaults;

        if ( defaults == null )
        {
            // Note: this is needed since RemoteConnectionsSettings, in contrary to the other two (HttpProxy and Auth)
            // is _mandatory_ and is always used, while the other two is used if present, otherwise not (like
            // HttpProxy).
            // Also, when we have no "parent" (defaults, to delegate lookup to), that means that we have to create a
            // default one.
            this.putRemoteConnectionContextObject( REMOTE_CONNECTIONS_SETTINGS, new CRemoteConnectionSettings() );
        }
    }

    public long getLastChanged()
    {
        return lastChanged;
    }

    public void setLastChanged( long ts )
    {
        lastChanged = ts;
    }

    public Map<String, Object> getRemoteConnectionContext()
    {
        return context;
    }

    public Object getRemoteConnectionContextObject( String key )
    {
        if ( context.containsKey( key ) )
        {
            return context.get( key );
        }
        else if ( defaults != null )
        {
            return defaults.getRemoteConnectionContextObject( key );
        }
        else
        {
            return null;
        }
    }

    public void removeRemoteConnectionContextObject( String key )
    {
        context.remove( key );
    }

    public void putRemoteConnectionContextObject( String key, Object value )
    {
        context.put( key, value );

        lastChanged = System.currentTimeMillis();
    }

}
