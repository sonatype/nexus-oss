/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.configuration.application;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.sonatype.nexus.configuration.model.CGroupsSetting;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CSecurity;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;

public class SimpleApplicationConfiguration
    implements ApplicationConfiguration
{
    private Configuration configuration;

    private Vector<EventListener> listeners = new Vector<EventListener>();

    public SimpleApplicationConfiguration()
    {
        super();

        this.configuration = new Configuration();

        configuration.setSecurity( new CSecurity() );
        configuration.setGlobalConnectionSettings( new CRemoteConnectionSettings() );
        // configuration.setGlobalHttpProxySettings( new CRemoteHttpProxySettings() );
        configuration.setRouting( new CRouting() );
        configuration.getRouting().setGroups( new CGroupsSetting() );
        configuration.setRepositoryGrouping( new CRepositoryGrouping() );
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public File getWorkingDirectory()
    {
        return new File( "target/plexus-home/" );
    }

    public File getWorkingDirectory( String key )
    {
        return new File( getWorkingDirectory(), key );
    }

    public File getTemporaryDirectory()
    {
        File result = new File( "target/tmp" );

        result.mkdirs();

        return result;
    }

    public File getWastebasketDirectory()
    {
        return getWorkingDirectory( "trash" );
    }

    public File getConfigurationDirectory()
    {
        File result = new File( getWorkingDirectory(), "conf" );
        if ( !result.exists() )
        {
            result.mkdirs();
        }
        return result;
    }

    public void saveConfiguration()
        throws IOException
    {
        // DO NOTHING, this is test
    }

    public boolean isSecurityEnabled()
    {
        return false;
    }

    public void addProximityEventListener( EventListener listener )
    {
        listeners.add( listener );
    }

    public void removeProximityEventListener( EventListener listener )
    {
        listeners.remove( listener );
    }

    public void notifyProximityEventListeners( AbstractEvent evt )
    {
        for ( EventListener l : listeners )
        {
            l.onProximityEvent( evt );
        }
    }

}
