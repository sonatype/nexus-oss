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
package org.sonatype.nexus;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.feeds.AuthcAuthzEvent;
import org.sonatype.nexus.feeds.ErrorWarningEvent;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.feeds.SystemEvent;
import org.sonatype.nexus.feeds.SystemProcess;
import org.sonatype.nexus.log.LogConfig;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalRequest;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalResult;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.templates.NoSuchTemplateIdException;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;

/**
 * The main Nexus application interface.
 * 
 * @author Jason van Zyl
 * @author cstamas
 */
public interface Nexus
    extends ApplicationStatusSource
{
    // ------------------------------------------------------------------
    // Configuration

    NexusConfiguration getNexusConfiguration();

    // ----------------------------------------------------------------------------
    // Reposes
    // ----------------------------------------------------------------------------

    StorageItem dereferenceLinkItem( StorageLinkItem item )
        throws NoSuchResourceStoreException, ItemNotFoundException, AccessDeniedException, IllegalOperationException,
        StorageException;

    RepositoryRouter getRootRouter();

    // ----------------------------------------------------------------------------
    // Repo maintenance
    // ----------------------------------------------------------------------------

    /**
     * Delete a user managed repository
     * 
     * @param id
     * @throws NoSuchRepositoryException
     * @throws IOException
     * @throws ConfigurationException
     * @throws AccessDeniedException
     * @see #deleteRepository(String, boolean)
     */
    public void deleteRepository( String id )
        throws NoSuchRepositoryException, IOException, ConfigurationException, AccessDeniedException;

    /**
     * Delete a repository, can only delete user managed repository unless force == true
     * 
     * @param id
     * @param force
     * @throws NoSuchRepositoryException
     * @throws IOException
     * @throws ConfigurationException
     * @throws AccessDeniedException when try to delete a non-user-managed repository and without force enabled
     */
    public void deleteRepository( String id, boolean force )
        throws NoSuchRepositoryException, IOException, ConfigurationException, AccessDeniedException;

    // ----------------------------------------------------------------------------
    // Maintenance
    // ----------------------------------------------------------------------------

    NexusStreamResponse getConfigurationAsStream()
        throws IOException;

    Collection<NexusStreamResponse> getApplicationLogFiles()
        throws IOException;

    NexusStreamResponse getApplicationLogAsStream( String logFile, long fromByte, long bytesCount )
        throws IOException;

    void expireAllCaches( ResourceStoreRequest request );

    @Deprecated
    void reindexAllRepositories( String path, boolean fullReindex )
        throws IOException;

    void rebuildAttributesAllRepositories( ResourceStoreRequest request )
        throws IOException;

    void rebuildMavenMetadataAllRepositories( ResourceStoreRequest request )
        throws IOException;

    Collection<String> evictAllUnusedProxiedItems( ResourceStoreRequest request, long timestamp )
        throws IOException;

    SnapshotRemovalResult removeSnapshots( SnapshotRemovalRequest request )
        throws NoSuchRepositoryException, IllegalArgumentException;

    /**
     * Delete the repository folders ( storage/, proxy/attributes/, indexer/ )
     * 
     * @param repository
     * @param deleteForever move storge/ into trash if it's false, otherwise 'rm -fr' it
     */
    void deleteRepositoryFolders( Repository repository, boolean deleteForever );

    /**
     * List the names of files in nexus-work/conf
     */
    Map<String, String> getConfigurationFiles();

    /**
     * Get the content of configuration file based on the key
     * 
     * @param key index in configuration file name list
     * @return
     * @throws IOException
     */
    NexusStreamResponse getConfigurationAsStreamByKey( String key )
        throws IOException;

    /**
     * Gets log config.
     * 
     * @return
     * @throws IOException
     * @deprecated Use org.sonatype.nexus.log.LogManager component to manage logs.
     */
    LogConfig getLogConfig()
        throws IOException;

    /**
     * Sets log config.
     * 
     * @param config
     * @throws IOException
     * @deprecated Use org.sonatype.nexus.log.LogManager component to manage logs.
     */
    void setLogConfig( LogConfig config )
        throws IOException;

    // ----------------------------------------------------------------------------
    // Feeds
    // ----------------------------------------------------------------------------

    // creating

    void addNexusArtifactEvent( NexusArtifactEvent nae );

    void addSystemEvent( String action, String message );

    void addAuthcAuthzEvent( AuthcAuthzEvent evt );

    SystemProcess systemProcessStarted( String action, String message );

    void systemProcessFinished( SystemProcess prc, String finishMessage );

    void systemProcessBroken( SystemProcess prc, Throwable e );

    // reading

    List<NexusArtifactEvent> getRecentlyStorageChanges( Integer from, Integer count, Set<String> repositoryIds );

    List<NexusArtifactEvent> getRecentlyDeployedOrCachedArtifacts( Integer from, Integer count,
                                                                   Set<String> repositoryIds );

    List<NexusArtifactEvent> getRecentlyCachedArtifacts( Integer from, Integer count, Set<String> repositoryIds );

    List<NexusArtifactEvent> getRecentlyDeployedArtifacts( Integer from, Integer count, Set<String> repositoryIds );

    List<NexusArtifactEvent> getBrokenArtifacts( Integer from, Integer count, Set<String> repositoryIds );

    List<SystemEvent> getRepositoryStatusChanges( Integer from, Integer count );

    List<SystemEvent> getSystemEvents( Integer from, Integer count );

    List<AuthcAuthzEvent> getAuthcAuthzEvents( Integer from, Integer count );

    List<ErrorWarningEvent> getErrorWarningEvents( Integer from, Integer count );

    // ----------------------------------------------------------------------------
    // Repo templates
    // ----------------------------------------------------------------------------

    TemplateSet getRepositoryTemplates();

    RepositoryTemplate getRepositoryTemplateById( String id )
        throws NoSuchTemplateIdException;;
}
