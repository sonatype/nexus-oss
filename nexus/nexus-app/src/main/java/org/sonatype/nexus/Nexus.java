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
import java.util.concurrent.RejectedExecutionException;

import org.sonatype.nexus.configuration.application.MutableConfiguration;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.feeds.AuthcAuthzEvent;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.feeds.SystemEvent;
import org.sonatype.nexus.feeds.SystemProcess;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.log.SimpleLog4jConfig;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalRequest;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalResult;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.schedules.Schedule;

/**
 * The main Nexus application interface.
 * 
 * @author Jason van Zyl
 * @author cstamas
 */
public interface Nexus
    extends ApplicationStatusSource, MutableConfiguration, NexusService
{
    // ------------------------------------------------------------------
    // Configuration

    NexusConfiguration getNexusConfiguration();

    // ----------------------------------------------------------------------------
    // Reposes
    // ----------------------------------------------------------------------------

    Repository getRepository( String repoId )
        throws NoSuchRepositoryException;

    <T> T getRepositoryWithFacet( String repoId, Class<T> f )
        throws NoSuchRepositoryException;

    Collection<Repository> getRepositories();

    <T> Collection<T> getRepositoriesWithFacet( Class<T> f );

    StorageItem dereferenceLinkItem( StorageLinkItem item )
        throws NoSuchResourceStoreException,
            ItemNotFoundException,
            AccessDeniedException,
            IllegalOperationException,
            StorageException;

    RepositoryRouter getRootRouter();

    // ----------------------------------------------------------------------------
    // Wastebasket
    // ----------------------------------------------------------------------------

    long getWastebasketItemCount()
        throws IOException;

    long getWastebasketSize()
        throws IOException;

    void wastebasketPurge()
        throws IOException;

    // ----------------------------------------------------------------------------
    // Maintenance
    // ----------------------------------------------------------------------------

    NexusStreamResponse getConfigurationAsStream()
        throws IOException;

    Collection<NexusStreamResponse> getApplicationLogFiles()
        throws IOException;

    NexusStreamResponse getApplicationLogAsStream( String logFile, long fromByte, long bytesCount )
        throws IOException;

    void clearAllCaches( String path );

    void clearRepositoryCaches( String path, String repositoryId )
        throws NoSuchRepositoryException;

    void clearRepositoryGroupCaches( String path, String repositoryGroupId )
        throws NoSuchRepositoryException;

    void reindexAllRepositories( String path )
        throws IOException;

    void reindexRepository( String path, String repositoryId )
        throws NoSuchRepositoryException,
            IOException;

    void reindexRepositoryGroup( String path, String repositoryGroupId )
        throws NoSuchRepositoryException,
            IOException;

    void publishAllIndex()
        throws IOException;

    void publishRepositoryIndex( String repositoryId )
        throws IOException,
            NoSuchRepositoryException;

    void publishRepositoryGroupIndex( String repositoryGroupId )
        throws IOException,
            NoSuchRepositoryException;

    void rebuildAttributesAllRepositories( String path )
        throws IOException;

    void rebuildAttributesRepository( String path, String repositoryId )
        throws NoSuchRepositoryException,
            IOException;

    void rebuildAttributesRepositoryGroup( String path, String repositoryGroupId )
        throws NoSuchRepositoryException,
            IOException;

    void rebuildMavenMetadataAllRepositories( String path )
        throws IOException;

    void rebuildMavenMetadataRepository( String path, String repositoryId )
        throws NoSuchRepositoryException,
            IOException;

    void rebuildMavenMetadataRepositoryGroup( String path, String repositoryGroupId )
        throws NoSuchRepositoryException,
            IOException;

    Collection<String> evictAllUnusedProxiedItems( long timestamp )
        throws IOException;

    Collection<String> evictRepositoryUnusedProxiedItems( long timestamp, String repositoryId )
        throws NoSuchRepositoryException,
            IOException;

    Collection<String> evictRepositoryGroupUnusedProxiedItems( long timestamp, String repositoryGroupId )
        throws NoSuchRepositoryException,
            IOException;

    SnapshotRemovalResult removeSnapshots( SnapshotRemovalRequest request )
        throws NoSuchRepositoryException,
            IllegalArgumentException;

    void synchronizeShadow( String shadowRepositoryId )
        throws NoSuchRepositoryException;

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

    // ----------------------------------------------------------------------------
    // Scheduler
    // ----------------------------------------------------------------------------

    <T> ScheduledTask<T> submit( String name, NexusTask<T> task )
        throws RejectedExecutionException,
            NullPointerException;

    <T> ScheduledTask<T> schedule( String name, NexusTask<T> nexusTask, Schedule schedule )
        throws RejectedExecutionException,
            NullPointerException;

    <T> ScheduledTask<T> updateSchedule( ScheduledTask<T> task )
        throws RejectedExecutionException,
            NullPointerException;

    Map<String, List<ScheduledTask<?>>> getAllTasks();

    Map<String, List<ScheduledTask<?>>> getActiveTasks();

    ScheduledTask<?> getTaskById( String id )
        throws NoSuchTaskException;

    /**
     * A factory for tasks.
     * 
     * @param taskType
     * @return
     * @throws IllegalArgumentException
     * @deprecated prefer the createTaskInstance(Class<T> type) method instead.
     */
    NexusTask<?> createTaskInstance( String taskType )
        throws IllegalArgumentException;

    /**
     * A factory for tasks.
     * 
     * @param taskType
     * @return
     * @throws IllegalArgumentException
     */
    <T> T createTaskInstance( Class<T> taskType )
        throws IllegalArgumentException;

    // ----------------------------------------------------------------------------
    // Default Configuration
    // ----------------------------------------------------------------------------

    boolean isDefaultSecurityEnabled();

    boolean isDefaultAnonymousAccessEnabled();

    String getDefaultAnonymousUsername();

    String getDefaultAnonymousPassword();

    List<String> getDefaultRealms();

    NexusStreamResponse getDefaultConfigurationAsStream()
        throws IOException;

    CRemoteConnectionSettings readDefaultGlobalRemoteConnectionSettings();

    CRemoteHttpProxySettings readDefaultGlobalRemoteHttpProxySettings();

    CSmtpConfiguration readDefaultSmtpConfiguration();

    CRouting readDefaultRouting();

    // ----------------------------------------------------------------------------
    // Repo templates, CRUD
    // ----------------------------------------------------------------------------

    Collection<CRepository> listRepositoryTemplates()
        throws IOException;

    void createRepositoryTemplate( CRepository settings )
        throws IOException;

    CRepository readRepositoryTemplate( String id )
        throws IOException;

    void updateRepositoryTemplate( CRepository settings )
        throws IOException;

    void deleteRepositoryTemplate( String id )
        throws IOException;

    Collection<CRepositoryShadow> listRepositoryShadowTemplates()
        throws IOException;

    void createRepositoryShadowTemplate( CRepositoryShadow settings )
        throws IOException;

    CRepositoryShadow readRepositoryShadowTemplate( String id )
        throws IOException;

    void updateRepositoryShadowTemplate( CRepositoryShadow settings )
        throws IOException;

    void deleteRepositoryShadowTemplate( String id )
        throws IOException;

    // ----------------------------------------------------------------------------
    // Search/identify
    // ----------------------------------------------------------------------------

    /**
     * Returns the local index (the true index for hosted ones, and the true cacheds index for proxy reposes). Every
     * repo has local index.
     * 
     * @param repositoryId
     * @return
     * @throws NoSuchRepositoryException
     */
    IndexingContext getRepositoryLocalIndexContext( String repositoryId )
        throws NoSuchRepositoryException;

    /**
     * Returns the remote index. Only proxy repositories have remote index, otherwise null is returnded.
     * 
     * @param repositoryId
     * @return
     * @throws NoSuchRepositoryException
     */
    IndexingContext getRepositoryRemoteIndexContext( String repositoryId )
        throws NoSuchRepositoryException;

    /**
     * Returns the "best" indexing context. If it has remoteIndex, and it is bigger then local, remote is considered
     * "best", otherwise local.
     * 
     * @param repositoryId
     * @return
     * @throws NoSuchRepositoryException
     */
    IndexingContext getRepositoryBestIndexContext( String repositoryId )
        throws NoSuchRepositoryException;

    ArtifactInfo identifyArtifact( String type, String checksum )
        throws IOException;

    FlatSearchResponse searchArtifactFlat( String term, String repositoryId, Integer from, Integer count )
        throws NoSuchRepositoryException;

    FlatSearchResponse searchArtifactClassFlat( String term, String repositoryId, Integer from, Integer count )
        throws NoSuchRepositoryException;

    FlatSearchResponse searchArtifactFlat( String gTerm, String aTerm, String vTerm, String pTerm, String cTerm,
        String repositoryId, Integer from, Integer count )
        throws NoSuchRepositoryException;

    /**
     * Remove the repository's storage folder
     */
    void removeRepositoryFolder( Repository repository );

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

    SimpleLog4jConfig getLogConfig()
        throws IOException;

    void setLogConfig( SimpleLog4jConfig config )
        throws IOException;

}
