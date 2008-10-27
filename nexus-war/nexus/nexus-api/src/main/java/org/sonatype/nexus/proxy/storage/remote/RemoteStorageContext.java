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

import java.util.Map;

/**
 * The remote storage settings and context.
 * 
 * @author cstamas
 */
public interface RemoteStorageContext
{
    public static final String REMOTE_CONNECTIONS_SETTINGS = "remoteConnectionSettings";

    public static final String REMOTE_HTTP_PROXY_SETTINGS = "remoteHttpProxySettings";

    public static final String REMOTE_AUTHENTICATION_SETTINGS = "remoteAuthenticationSettings";

    long getLastChanged();

    void setLastChanged( long ts );

    Object getRemoteConnectionContextObject( String key );

    void putRemoteConnectionContextObject( String key, Object value );

    void removeRemoteConnectionContextObject( String key );

    Map<String, Object> getRemoteConnectionContext();
}
