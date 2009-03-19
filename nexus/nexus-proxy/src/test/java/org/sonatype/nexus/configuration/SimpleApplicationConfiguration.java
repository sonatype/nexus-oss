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
package org.sonatype.nexus.configuration;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CSecurity;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.proxy.AbstractNexusTestCase;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public class SimpleApplicationConfiguration
    implements ApplicationConfiguration
{
    private Configuration configuration;

    private Vector<EventListener> listeners = new Vector<EventListener>();

    private RemoteStorageContext remoteStorageContext = new DefaultRemoteStorageContext( null );

    public SimpleApplicationConfiguration()
    {
        super();

        this.configuration = new Configuration();

        configuration.setSecurity( new CSecurity() );
        configuration.setGlobalConnectionSettings( new CRemoteConnectionSettings() );
        // configuration.setGlobalHttpProxySettings( new CRemoteHttpProxySettings() );
        configuration.setRouting( new CRouting() );
        configuration.setRepositoryGrouping( new CRepositoryGrouping() );
    }

    public RemoteStorageContext getGlobalRemoteStorageContext()
    {
        return remoteStorageContext;
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public File getWorkingDirectory()
    {
        return AbstractNexusTestCase.WORK_HOME;
    }

    public File getConfigurationDirectory()
    {
        return AbstractNexusTestCase.CONF_HOME;
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
        return new File( getWorkingDirectory(), "trash" );
    }

    public File getSecurityConfigurationFile()
    {
        return new File( getConfigurationDirectory(), "security.xml" );
    }

    public void saveConfiguration()
        throws IOException
    {
        // NOTHING TO DO HERE
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
