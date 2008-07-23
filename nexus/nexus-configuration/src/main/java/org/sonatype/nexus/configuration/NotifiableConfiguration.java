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

import java.io.IOException;

public interface NotifiableConfiguration
{
    String ROLE = NotifiableConfiguration.class.getName();
    
    /**
     * Saves the configuration.
     * 
     * @throws IOException
     */
    void saveConfiguration()
        throws IOException;

    /**
     * Registers a configuration change listener.
     * 
     * @param listener
     */
    void addConfigurationChangeListener( ConfigurationChangeListener listener );

    /**
     * Deregisters a configuration change listener.
     * 
     * @param listener
     */
    void removeConfigurationChangeListener( ConfigurationChangeListener listener );

    /**
     * Notifies the listeners about configuration change.
     */
    void notifyConfigurationChangeListeners();

    /**
     * Notifies the listeners about configuration change.
     */
    void notifyConfigurationChangeListeners( ConfigurationChangeEvent evt );
}
