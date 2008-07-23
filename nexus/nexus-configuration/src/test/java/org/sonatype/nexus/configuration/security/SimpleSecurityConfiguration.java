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
package org.sonatype.nexus.configuration.security;

import java.io.IOException;
import java.util.Vector;

import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;
import org.sonatype.nexus.configuration.security.model.Configuration;

public class SimpleSecurityConfiguration
    implements SecurityConfiguration
{
    private Configuration configuration;

    private Vector<ConfigurationChangeListener> listeners = new Vector<ConfigurationChangeListener>();

    public SimpleSecurityConfiguration()
    {
        super();

        this.configuration = new Configuration();
    }

    public Configuration getConfiguration()
    {
        return configuration;
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

    public void saveConfiguration()
        throws IOException
    {
        // DO NOTHING, this is test
    }

}
