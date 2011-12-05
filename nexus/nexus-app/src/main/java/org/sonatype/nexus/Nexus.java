/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
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

    @Deprecated
    void expireAllCaches( ResourceStoreRequest request );

    @Deprecated
    void reindexAllRepositories( String path, boolean fullReindex )
        throws IOException;

    @Deprecated
    void rebuildAttributesAllRepositories( ResourceStoreRequest request )
        throws IOException;

    @Deprecated
    void rebuildMavenMetadataAllRepositories( ResourceStoreRequest request )
        throws IOException;

    @Deprecated
    Collection<String> evictAllUnusedProxiedItems( ResourceStoreRequest request, long timestamp )
        throws IOException;

    @Deprecated
    SnapshotRemovalResult removeSnapshots( SnapshotRemovalRequest request )
        throws NoSuchRepositoryException, IllegalArgumentException;

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

    // ----------------------------------------------------------------------------
    // Feeds
    // ----------------------------------------------------------------------------

    // creating
/*
    @Deprecated
    void addNexusArtifactEvent( NexusArtifactEvent nae );

    @Deprecated
    void addSystemEvent( String action, String message );

    @Deprecated
    void addAuthcAuthzEvent( AuthcAuthzEvent evt );

    @Deprecated
    SystemProcess systemProcessStarted( String action, String message );

    @Deprecated
    void systemProcessFinished( SystemProcess prc, String finishMessage );

    @Deprecated
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
*/
    // ----------------------------------------------------------------------------
    // Repo templates
    // ----------------------------------------------------------------------------

    TemplateSet getRepositoryTemplates();

    RepositoryTemplate getRepositoryTemplateById( String id )
        throws NoSuchTemplateIdException;;
}
