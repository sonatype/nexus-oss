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
package org.sonatype.nexus.configuration;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.sonatype.nexus.configuration.model.CGroupsSetting;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRepositoryGrouping;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CSecurity;
import org.sonatype.nexus.configuration.model.Configuration;

public class SimpleApplicationConfiguration
    implements ApplicationConfiguration
{
    private Configuration configuration;

    private Vector<ConfigurationChangeListener> listeners = new Vector<ConfigurationChangeListener>();

    public SimpleApplicationConfiguration()
    {
        super();

        this.configuration = new Configuration();

        configuration.setWorkingDirectory( "target/plexus-home/" );

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
        return new File( configuration.getWorkingDirectory() );
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

    public void addConfigurationChangeListener( ConfigurationChangeListener listener )
    {
        listeners.add( listener );
    }

    public void removeConfigurationChangeListener( ConfigurationChangeListener listener )
    {
        listeners.remove( listener );
    }

    public void notifyConfigurationChangeListeners()
    {
        notifyConfigurationChangeListeners( new ConfigurationChangeEvent( this ) );
    }

    public void notifyConfigurationChangeListeners( ConfigurationChangeEvent evt )
    {
        for ( ConfigurationChangeListener l : listeners )
        {
            l.onConfigurationChange( evt );
        }
    }

    public File getConfigurationDirectory()
    {
        File result = new File( configuration.getWorkingDirectory(), "conf" );
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

}
