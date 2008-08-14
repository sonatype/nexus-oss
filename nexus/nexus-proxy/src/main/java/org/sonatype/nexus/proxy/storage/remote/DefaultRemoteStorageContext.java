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

import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;

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

    private CRemoteConnectionSettings remoteConnectionSettings = new CRemoteConnectionSettings();

    private CRemoteHttpProxySettings remoteHttpProxySettings = null;

    private CRemoteAuthentication remoteAuthenticationSettings = null;

    private RemoteStorageContext defaults;

    public DefaultRemoteStorageContext( RemoteStorageContext defaults )
    {
        super();

        this.defaults = defaults;
    }

    public long getLastChanged()
    {
        return lastChanged;
    }

    public void setLastChanged( long ts )
    {
        lastChanged = ts;
    }

    public CRemoteConnectionSettings getRemoteConnectionSettings()
    {
        if ( remoteConnectionSettings == null && defaults != null )
        {
            return defaults.getRemoteConnectionSettings();
        }
        else
        {
            return remoteConnectionSettings;
        }
    }

    public void setRemoteConnectionSettings( CRemoteConnectionSettings remoteConnectionSettings )
    {
        this.remoteConnectionSettings = remoteConnectionSettings;

        lastChanged = System.currentTimeMillis();
    }

    public CRemoteHttpProxySettings getRemoteHttpProxySettings()
    {
        if ( remoteHttpProxySettings == null && defaults != null )
        {
            return defaults.getRemoteHttpProxySettings();
        }
        else
        {
            return remoteHttpProxySettings;
        }
    }

    public void setRemoteHttpProxySettings( CRemoteHttpProxySettings remoteHttpProxySettings )
    {
        this.remoteHttpProxySettings = remoteHttpProxySettings;

        lastChanged = System.currentTimeMillis();
    }

    public CRemoteAuthentication getRemoteAuthenticationSettings()
    {
        return remoteAuthenticationSettings;
    }

    public void setRemoteAuthenticationSettings( CRemoteAuthentication remoteAuthenticationSettings )
    {
        this.remoteAuthenticationSettings = remoteAuthenticationSettings;

        lastChanged = System.currentTimeMillis();
    }

    public Map<String, Object> getRemoteConnectionContext()
    {
        return context;
    }

}
