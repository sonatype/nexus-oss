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
import java.io.InputStream;

import org.sonatype.nexus.NexusService;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.security.source.SecurityConfigurationSource;

/**
 * A component responsible for configuration management.
 * 
 * @author cstamas
 */
public interface NexusSecurityConfiguration
    extends SecurityConfiguration, MutableNexusSecurityConfiguration, NexusService
{
    String ROLE = NexusSecurityConfiguration.class.getName();

    /**
     * Explicit loading of configuration. Does not force reload.
     * 
     * @throws ConfigurationException
     * @throws IOException
     */
    void loadConfiguration()
        throws ConfigurationException,
            IOException;

    /**
     * Explicit loading of configuration. Enables to force reloading of config.
     * 
     * @throws ConfigurationException
     * @throws IOException
     */
    void loadConfiguration( boolean forceReload )
        throws ConfigurationException,
            IOException;

    /**
     * Applies the config by creating needed objects/reposes/etc.
     * 
     * @throws ConfigurationException
     * @throws IOException
     */
    void applyConfiguration()
        throws ConfigurationException,
            IOException;

    SecurityConfigurationSource getConfigurationSource();

    InputStream getConfigurationAsStream()
        throws IOException;

    boolean isInstanceUpgraded();

    boolean isConfigurationUpgraded();

    boolean isConfigurationDefaulted();
}
