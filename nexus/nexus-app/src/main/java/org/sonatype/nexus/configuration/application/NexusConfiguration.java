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
package org.sonatype.nexus.configuration.application;

import java.io.IOException;
import java.util.Map;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.NexusStreamResponse;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.source.ApplicationConfigurationSource;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * A component responsible for configuration management.
 * 
 * @author cstamas
 */
public interface NexusConfiguration
    extends ApplicationConfiguration, MutableConfiguration
{
    /**
     * Explicit loading of configuration. Does not force reload.
     * 
     * @throws ConfigurationException
     * @throws IOException
     */
    void loadConfiguration()
        throws ConfigurationException, IOException;

    /**
     * Explicit loading of configuration. Enables to force reloading of config.
     * 
     * @throws ConfigurationException
     * @throws IOException
     */
    void loadConfiguration( boolean forceReload )
        throws ConfigurationException, IOException;

    ApplicationConfigurationSource getConfigurationSource();

    boolean isInstanceUpgraded();

    boolean isConfigurationUpgraded();

    boolean isConfigurationDefaulted();

    Repository createRepositoryFromModel( CRepository repository )
        throws ConfigurationException;

    // ------------------------------------------------------------------
    // Booting

    /**
     * Creates internals like reposes configured in nexus.xml. Called on startup.
     */
    void createInternals()
        throws ConfigurationException;

    /**
     * Cleanups the internals, like on shutdown.
     */
    void dropInternals();

    /**
     * List the names of files under Configuration Directory
     * 
     * @return A map with the value be file name
     */
    Map<String, String> getConfigurationFiles();

    /**
     * Loads the config file.
     * 
     * @param key
     * @return
     * @throws IOException
     */
    NexusStreamResponse getConfigurationAsStreamByKey( String key )
        throws IOException;
}
