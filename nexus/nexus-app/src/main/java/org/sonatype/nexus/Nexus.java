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
package org.sonatype.nexus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.sonatype.nexus.configuration.application.MutableConfiguration;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.feeds.NexusArtifactEvent;
import org.sonatype.nexus.feeds.SystemEvent;
import org.sonatype.nexus.feeds.SystemProcess;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalRequest;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalResult;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;
import org.sonatype.scheduling.schedules.Schedule;

/**
 * The main Nexus application interface.
 * 
 * @author Jason van Zyl
 * @author cstamas
 */
public interface Nexus
    extends MutableConfiguration
{
    String ROLE = Nexus.class.getName();

    // ------------------------------------------------------------------
    // Status

    SystemStatus getSystemStatus();

    boolean setState( SystemState state );

    // ------------------------------------------------------------------
    // Configuration

    NexusConfiguration getNexusConfiguration();

    // ----------------------------------------------------------------------------
    // Reposes
    // ----------------------------------------------------------------------------

    Repository getRepository( String repoId )
        throws NoSuchRepositoryException;

    List<Repository> getRepositoryGroup( String repoGroupId )
        throws NoSuchRepositoryGroupException;

    String getRepositoryGroupType( String repoGroupId )
        throws NoSuchRepositoryGroupException;

    Collection<Repository> getRepositories();

    StorageItem dereferenceLinkItem( StorageItem item )
        throws NoSuchRepositoryException,
            ItemNotFoundException,
            AccessDeniedException,
            RepositoryNotAvailableException,
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

    InputStream getConfigurationAsStream()
        throws IOException;

    Collection<String> getApplicationLogFiles()
        throws IOException;

    InputStream getApplicationLogAsStream( String logFile )
        throws IOException;

    void clearAllCaches( String path );

    void clearRepositoryCaches( String path, String repositoryId )
        throws NoSuchRepositoryException;

    void clearRepositoryGroupCaches( String path, String repositoryGroupId )
        throws NoSuchRepositoryGroupException;

    void reindexAllRepositories( String path )
        throws IOException;

    void reindexRepository( String path, String repositoryId )
        throws NoSuchRepositoryException,
            IOException;

    void reindexRepositoryGroup( String path, String repositoryGroupId )
        throws NoSuchRepositoryGroupException,
            IOException;

    void publishAllIndex()
        throws IOException;

    void publishRepositoryIndex( String repositoryId )
        throws IOException,
            NoSuchRepositoryException;

    void publishRepositoryGroupIndex( String repositoryGroupId )
        throws IOException,
            NoSuchRepositoryGroupException;

    void rebuildAttributesAllRepositories( String path )
        throws IOException;

    void rebuildAttributesRepository( String path, String repositoryId )
        throws NoSuchRepositoryException,
            IOException;

    void rebuildAttributesRepositoryGroup( String path, String repositoryGroupId )
        throws NoSuchRepositoryGroupException,
            IOException;

    Collection<String> evictAllUnusedProxiedItems( long timestamp )
        throws IOException;

    Collection<String> evictRepositoryUnusedProxiedItems( long timestamp, String repositoryId )
        throws NoSuchRepositoryException,
            IOException;

    Collection<String> evictRepositoryGroupUnusedProxiedItems( long timestamp, String repositoryGroupId )
        throws NoSuchRepositoryGroupException,
            IOException;

    SnapshotRemovalResult removeSnapshots( SnapshotRemovalRequest request )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            IllegalArgumentException;

    // ----------------------------------------------------------------------------
    // Feeds
    // ----------------------------------------------------------------------------

    // creating

    void addNexusArtifactEvent( NexusArtifactEvent nae );

    void addSystemEvent( String action, String message );

    SystemProcess systemProcessStarted( String action, String message );

    void systemProcessFinished( SystemProcess prc );

    void systemProcessBroken( SystemProcess prc, Throwable e );

    // reading

    List<NexusArtifactEvent> getRecentlyStorageChanges();

    List<NexusArtifactEvent> getRecentlyDeployedOrCachedArtifacts();

    List<NexusArtifactEvent> getRecentlyCachedArtifacts();

    List<NexusArtifactEvent> getRecentlyDeployedArtifacts();

    List<NexusArtifactEvent> getBrokenArtifacts();

    List<SystemEvent> getRepositoryStatusChanges();

    List<SystemEvent> getSystemEvents();

    // ----------------------------------------------------------------------------
    // Scheduler
    // ----------------------------------------------------------------------------

    <T> void submit( String name, SchedulerTask<T> task )
        throws RejectedExecutionException,
            NullPointerException;

    <T> ScheduledTask<T> schedule( String name, SchedulerTask<T> nexusTask, Schedule schedule )
        throws RejectedExecutionException,
            NullPointerException;

    <T> ScheduledTask<T> updateSchedule( ScheduledTask<T> task )
        throws RejectedExecutionException,
            NullPointerException;

    Map<Class<?>, List<ScheduledTask<?>>> getAllTasks();

    Map<Class<?>, List<ScheduledTask<?>>> getActiveTasks();

    ScheduledTask<?> getTaskById( String id )
        throws NoSuchTaskException;

    SchedulerTask<?> createTaskInstance( String taskType )
        throws IllegalArgumentException;

    SchedulerTask<?> createTaskInstance( Class<?> taskType )
        throws IllegalArgumentException;

    // ----------------------------------------------------------------------------
    // Default Configuration
    // ----------------------------------------------------------------------------

    boolean isDefaultSecurityEnabled();

    boolean isDefaultAnonymousAccessEnabled();

    String getDefaultAnonymousUsername();

    String getDefaultAnonymousPassword();

    List<String> getDefaultRealms();

    InputStream getDefaultConfigurationAsStream()
        throws IOException;

    String readDefaultWorkingDirectory();

    String readDefaultApplicationLogDirectory();

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

    ArtifactInfo identifyArtifact( String type, String checksum )
        throws IOException;

    FlatSearchResponse searchArtifactFlat( String term, String repositoryId, String groupId, Integer from, Integer count );

    FlatSearchResponse searchArtifactClassFlat( String term, String repositoryId, String groupId, Integer from, Integer count );

    FlatSearchResponse searchArtifactFlat( String gTerm, String aTerm, String vTerm, String cTerm, String repositoryId,
        String groupId, Integer from, Integer count );
}
