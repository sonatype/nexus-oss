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

import java.io.File;
import java.io.IOException;

import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.ConfigurationChangeListener;
import org.sonatype.nexus.configuration.security.model.Configuration;

/**
 * Adapter for NexusConfiguration.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.nexus.configuration.security.SecurityConfiguration"
 */
public class NexusSecurityConfigurationAdapter
    implements SecurityConfiguration
{
    
    /**
     * @plexus.requirement
     */
    private NexusSecurityConfiguration nexusConfiguration;

    public Configuration getConfiguration()
    {
        return nexusConfiguration.getConfiguration();
    }
    
    public File getConfigurationFile()
    {
        return nexusConfiguration.getConfigurationFile();
    }
    
    public void saveConfiguration()
        throws IOException
    {
        nexusConfiguration.saveConfiguration();
    }

    public void addConfigurationChangeListener( ConfigurationChangeListener listener )
    {
        nexusConfiguration.addConfigurationChangeListener( listener );
    }

    public void removeConfigurationChangeListener( ConfigurationChangeListener listener )
    {
        nexusConfiguration.removeConfigurationChangeListener( listener );
    }

    public void notifyConfigurationChangeListeners()
    {
        nexusConfiguration.notifyConfigurationChangeListeners();
    }

    public void notifyConfigurationChangeListeners( ConfigurationChangeEvent evt )
    {
        nexusConfiguration.notifyConfigurationChangeListeners( evt );
    }

}
