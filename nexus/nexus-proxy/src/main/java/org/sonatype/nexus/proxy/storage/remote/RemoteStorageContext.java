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

import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;

/**
 * The remote storage settings and context.
 * 
 * @author cstamas
 */
public interface RemoteStorageContext
{
    String ROLE = RemoteStorageContext.class.getName();

    long getLastChanged();

    void setLastChanged( long ts );

    CRemoteConnectionSettings getRemoteConnectionSettings();

    void setRemoteConnectionSettings( CRemoteConnectionSettings settings );

    CRemoteHttpProxySettings getRemoteHttpProxySettings();

    void setRemoteHttpProxySettings( CRemoteHttpProxySettings settings );

    CRemoteAuthentication getRemoteAuthenticationSettings();

    void setRemoteAuthenticationSettings( CRemoteAuthentication settings );

    Map<String, Object> getRemoteConnectionContext();
}
