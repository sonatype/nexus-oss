/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.index;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Query;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.Field;
import org.apache.maven.index.FlatSearchResponse;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.SearchType;
import org.apache.maven.index.expr.SearchExpression;
import org.apache.maven.index.treeview.TreeNode;
import org.apache.maven.index.treeview.TreeNodeFactory;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Nexus facade for NexusIndexer operations.
 * 
 * @author cstamas
 */
public interface IndexerManager
{
    // ----------------------------------------------------------------------------
    // Config management et al
    // ----------------------------------------------------------------------------

    /**
     * Shutdown of Indexer, with cleanup. It remove index files if needed.
     * 
     * @param deleteFile set to true if you want to completely remove index files.
     */
    void shutdown( boolean deleteFiles )
        throws IOException;

    /**
     * Resets the configuration of Indexer, forcing it to renew it's values.
     */
    void resetConfiguration();

    // ----------------------------------------------------------------------------
    // Context management et al
    // ----------------------------------------------------------------------------

    /**
     * Adds a new IndexContext for repository.
     * 
     * @param repositoryId
     * @throws IOException
     * @throws NoSuchRepositoryException
     */
    void addRepositoryIndexContext( String repositoryId )
        throws IOException, NoSuchRepositoryException;

    /**
     * Removes an IndexContext for repository.
     * 
     * @param repositoryId
     * @param deleteFiles
     * @throws IOException
     * @throws NoSuchRepositoryException
     */
    void removeRepositoryIndexContext( String repositoryId, boolean deleteFiles )
        throws IOException, NoSuchRepositoryException;

    /**
     * Updates an IndexContext for repository.
     * 
     * @param repositoryId
     * @throws IOException
     * @throws NoSuchRepositoryException
     */
    void updateRepositoryIndexContext( String repositoryId )
        throws IOException, NoSuchRepositoryException;

    /**
     * Flags an indexing context should be searched in global searches or not.
     * 
     * @param repositoryId
     * @param searchable
     * @throws IOException
     * @throws NoSuchRepositoryException
     */
    void setRepositoryIndexContextSearchable( String repositoryId, boolean searchable )
        throws IOException, NoSuchRepositoryException;

    // ----------------------------------------------------------------------------
    // adding/removing on the fly
    // ----------------------------------------------------------------------------

    /**
     * Adds an item to index.
     * 
     * @param repository
     * @param item
     * @throws IOException
     */
    void addItemToIndex( Repository repository, StorageItem item )
        throws IOException;

    /**
     * Removes single item from index.
     * 
     * @param repository
     * @param item
     * @throws IOException
     */
    void removeItemFromIndex( Repository repository, StorageItem item )
        throws IOException;

    // ----------------------------------------------------------------------------
    // Reindexing related (will do local-scan, remote-download, merge, publish)
    // ----------------------------------------------------------------------------

    void reindexAllRepositories( String path, boolean fullReindex )
        throws IOException;

    void reindexRepository( String path, String repositoryId, boolean fullReindex )
        throws NoSuchRepositoryException, IOException;

    // ----------------------------------------------------------------------------
    // Downloading remote indexes (will do remote-download, merge only)
    // ----------------------------------------------------------------------------

    void downloadAllIndex()
        throws IOException;

    void downloadRepositoryIndex( String repositoryId )
        throws IOException, NoSuchRepositoryException;

    // ----------------------------------------------------------------------------
    // Publishing index (will do publish only)
    // ----------------------------------------------------------------------------

    void publishAllIndex()
        throws IOException;

    void publishRepositoryIndex( String repositoryId )
        throws IOException, NoSuchRepositoryException;

    // ----------------------------------------------------------------------------
    // Optimizing index
    // ----------------------------------------------------------------------------

    void optimizeAllRepositoriesIndex()
        throws IOException;

    void optimizeRepositoryIndex( String repositoryId )
        throws NoSuchRepositoryException, IOException;

    // ----------------------------------------------------------------------------
    // Identify
    // ----------------------------------------------------------------------------

    Collection<ArtifactInfo> identifyArtifact( Field field, String data )
        throws IOException;

    // ----------------------------------------------------------------------------
    // Combined searching
    // ----------------------------------------------------------------------------

    @Deprecated
    FlatSearchResponse searchArtifactFlat( String term, String repositoryId, Integer from, Integer count,
                                           Integer hitLimit )
        throws NoSuchRepositoryException;

    @Deprecated
    FlatSearchResponse searchArtifactClassFlat( String term, String repositoryId, Integer from, Integer count,
                                                Integer hitLimit )
        throws NoSuchRepositoryException;

    @Deprecated
    FlatSearchResponse searchArtifactFlat( String gTerm, String aTerm, String vTerm, String pTerm, String cTerm,
                                           String repositoryId, Integer from, Integer count, Integer hitLimit )
        throws NoSuchRepositoryException;

    IteratorSearchResponse searchQueryIterator( Query query, String repositoryId, Integer from, Integer count,
                                                Integer hitLimit, boolean uniqueRGA, List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException;

    IteratorSearchResponse searchArtifactIterator( String term, String repositoryId, Integer from, Integer count,
                                                   Integer hitLimit, boolean uniqueRGA, SearchType searchType,
                                                   List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException;

    IteratorSearchResponse searchArtifactClassIterator( String term, String repositoryId, Integer from, Integer count,
                                                        Integer hitLimit, SearchType searchType,
                                                        List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException;

    IteratorSearchResponse searchArtifactIterator( String gTerm, String aTerm, String vTerm, String pTerm,
                                                   String cTerm, String repositoryId, Integer from, Integer count,
                                                   Integer hitLimit, boolean uniqueRGA, SearchType searchType,
                                                   List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException;

    IteratorSearchResponse searchArtifactSha1ChecksumIterator( String sha1Checksum, String repositoryId, Integer from,
                                                               Integer count, Integer hitLimit,
                                                               List<ArtifactInfoFilter> filters )
        throws NoSuchRepositoryException;

    // ----------------------------------------------------------------------------
    // Query construction
    // ----------------------------------------------------------------------------

    @Deprecated
    Query constructQuery( Field field, String query, SearchType type )
        throws IllegalArgumentException;

    Query constructQuery( Field field, SearchExpression expression )
        throws IllegalArgumentException;

    // ----------------------------------------------------------------------------
    // Tree nodes
    // ----------------------------------------------------------------------------

    TreeNode listNodes( final TreeNodeFactory factory, final String path, final String repositoryId )
        throws NoSuchRepositoryException, IOException;

    TreeNode listNodes( final TreeNodeFactory factory, final String path, final Map<Field, String> hints,
                        final ArtifactInfoFilter artifactInfoFilter, final String repositoryId )
        throws NoSuchRepositoryException, IOException;
}
