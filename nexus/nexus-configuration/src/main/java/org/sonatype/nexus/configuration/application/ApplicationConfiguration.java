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
package org.sonatype.nexus.configuration.application;

import java.io.File;

import org.sonatype.nexus.configuration.NotifiableConfiguration;
import org.sonatype.nexus.configuration.model.Configuration;

/**
 * ApplicationConfiguration is the main component to have and maintain configuration.
 */
public interface ApplicationConfiguration
    extends NotifiableConfiguration
{
    String ROLE = ApplicationConfiguration.class.getName();

    /**
     * Gets the working directory as file. The directory is created if needed and is guaranteed to exists.
     * 
     * @return
     */
    File getWorkingDirectory();

    /**
     * Gets the working directory with some subpath. The directory is created and is guaranteed to exists.
     * 
     * @param key
     * @return
     */
    File getWorkingDirectory( String key );

    /**
     * Returns the configuration directory. It defaults to $NEXUS_WORK/conf.
     * 
     * @return
     */
    File getConfigurationDirectory();

    /**
     * Returns the temporary directory.
     * 
     * @return
     */
    File getTemporaryDirectory();

    /**
     * Returns the wastebasket directory.
     * 
     * @return
     */
    File getWastebasketDirectory();

    /**
     * Is security enabled?
     * 
     * @return
     */
    boolean isSecurityEnabled();

    /**
     * Gets the Configuration object.
     * 
     * @return
     */
    Configuration getConfiguration();
}
