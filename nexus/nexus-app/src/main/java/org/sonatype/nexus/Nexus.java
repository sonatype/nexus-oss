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

import org.sonatype.nexus.configuration.MutableConfiguration;
import org.sonatype.nexus.configuration.NexusConfiguration;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.CRouting;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SubmittedTask;
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

    SystemStatus getSystemState();

    // ------------------------------------------------------------------
    // Configuration

    NexusConfiguration getNexusConfiguration();

    // ------------------------------------------------------------------
    // Reposes

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

    // ------------------------------------------------------------------
    // Maintenance

    InputStream getConfigurationAsStream()
        throws IOException;

    Collection<String> getApplicationLogFiles()
        throws IOException;

    InputStream getApplicationLogAsStream( String logFile )
        throws IOException;

    void clearCaches( String path, String repositoryId, String repositoryGroupId )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException;

    // ------------------------------------------------------------------
    // Feeds

    FeedRecorder getFeedRecorder();

    // ------------------------------------------------------------------
    // Scheduler

    <T> void submit( NexusTask<T> task )
        throws RejectedExecutionException,
            NullPointerException;

    <T> ScheduledTask<T> schedule( NexusTask<T> nexusTask, Schedule schedule )
        throws RejectedExecutionException,
            NullPointerException;

    Map<String, List<SubmittedTask<?>>> getActiveTasks();

    SubmittedTask<?> getTaskById( String id )
        throws NoSuchTaskException;

    // ------------------------------------------------------------------
    // Configuration defaults

    boolean isDefaultSecurityEnabled();

    boolean isDefaultAnonymousAccessEnabled();

    String getDefaultAuthenticationSourceType();

    InputStream getDefaultConfigurationAsStream()
        throws IOException;

    String readDefaultWorkingDirectory();

    String readDefaultApplicationLogDirectory();

    CRemoteConnectionSettings readDefaultGlobalRemoteConnectionSettings();

    CRemoteHttpProxySettings readDefaultGlobalRemoteHttpProxySettings();

    CRouting readDefaultRouting();

    // ------------------------------------------------------------------
    // Repo templates, CRUD

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

    // ------------------------------------------------------------------
    // Search and indexing related

    void reindexAllRepositories()
        throws IOException;

    void reindexRepository( String repositoryId )
        throws NoSuchRepositoryException,
            IOException;

    void reindexRepositoryGroup( String repositoryGroupId )
        throws NoSuchRepositoryGroupException,
            IOException;

    void rebuildAttributesAllRepositories()
        throws IOException;

    void rebuildAttributesRepository( String repositoryId )
        throws NoSuchRepositoryException,
            IOException;

    void rebuildAttributesRepositoryGroup( String repositoryGroupId )
        throws NoSuchRepositoryGroupException,
            IOException;

    ArtifactInfo identifyArtifact( String type, String checksum )
        throws IOException;

    // ----------------------------------------------------------------------------
    // Combined searching
    // ----------------------------------------------------------------------------

    Collection<ArtifactInfo> searchArtifactFlat( String term, String repositoryId, String groupId );

    Collection<ArtifactInfo> searchArtifactFlat( String gTerm, String aTerm, String vTerm, String cTerm,
        String repositoryId, String groupId );

}
