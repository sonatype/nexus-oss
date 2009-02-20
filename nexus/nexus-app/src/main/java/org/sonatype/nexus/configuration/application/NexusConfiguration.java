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
import java.io.InputStream;
import java.util.Map;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.source.ApplicationConfigurationSource;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryGroup;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.CRepositoryWebSite;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.repository.WebSiteRepository;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

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

    void applyConfiguration( Object... changeds )
        throws IOException;

    void saveConfiguration()
        throws IOException;

    ApplicationConfigurationSource getConfigurationSource();

    InputStream getConfigurationAsStream()
        throws IOException;

    boolean isInstanceUpgraded();

    boolean isConfigurationUpgraded();

    boolean isConfigurationDefaulted();

    RemoteStorageContext getRemoteStorageContext();

    Repository createRepositoryFromModel( Configuration configuration, CRepository repository )
        throws InvalidConfigurationException;

    ShadowRepository createRepositoryFromModel( Configuration configuration, CRepositoryShadow repositoryShadow )
        throws InvalidConfigurationException;

    GroupRepository createRepositoryFromModel( Configuration configuration, CRepositoryGroup repositoryGroup )
        throws InvalidConfigurationException;

    WebSiteRepository createRepositoryFromModel( Configuration configuration, CRepositoryWebSite repositorySite )
        throws InvalidConfigurationException;

    // ------------------------------------------------------------------
    // Booting

    void createInternals()
        throws ConfigurationException;

    void dropInternals();

    /**
     * List the names of files under Configuration Directory
     * 
     * @return A map with the value be file name
     */
    Map<String, String> getConfigurationFiles();

    InputStream getConfigurationAsStreamByKey( String key )
        throws IOException;
}
