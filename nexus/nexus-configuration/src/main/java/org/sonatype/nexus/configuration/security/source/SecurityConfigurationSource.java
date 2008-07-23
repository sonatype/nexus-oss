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
package org.sonatype.nexus.configuration.security.source;

import java.io.IOException;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.security.model.Configuration;
import org.sonatype.nexus.configuration.source.ConfigurationSource;

/**
 * The Interface ApplicationConfigurationSource, responsible to fetch Nexus security configuration by some means. It also stores one
 * instance of Configuration object maintained thru life of Nexus. This component is also able to persist security config.
 * 
 * @author cstamas
 */
public interface SecurityConfigurationSource extends ConfigurationSource
{
    String ROLE = SecurityConfigurationSource.class.getName();
    
    /**
     * Gets the current configuration.
     * 
     * @return the configuration, null if not loaded
     * @throws ConfigurationException
     * @throws IOException
     */
    Configuration getConfiguration();
    

    /**
     * Forces reloading the user configuration.
     * 
     * @return the configuration
     * @throws ConfigurationException
     * @throws IOException
     */
    Configuration loadConfiguration()
        throws ConfigurationException,
            IOException;
    

    /**
     * Returns the configuration that this configuration uses for defaulting.
     * 
     * @return a config source that is default source for this config or null
     */
    SecurityConfigurationSource getDefaultsSource();
}
