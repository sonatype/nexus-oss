/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.index;

import java.io.IOException;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;

public interface IndexerManager
{
    // ----------------------------------------------------------------------------
    // Context management et al
    // ----------------------------------------------------------------------------

    void shutdown( boolean deleteFiles )
        throws IOException;

    void addRepositoryIndexContext( String repositoryId )
        throws IOException,
            NoSuchRepositoryException;

    void removeRepositoryIndexContext( String repositoryId, boolean deleteFiles )
        throws IOException,
            NoSuchRepositoryException;

    void updateRepositoryIndexContext( String repositoryId )
        throws IOException,
            NoSuchRepositoryException;

    void resetConfiguration();

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

    void addRepositoryGroupIndexContext( String repositoryGroupId )
        throws IOException,
            NoSuchRepositoryGroupException;

    void removeRepositoryGroupIndexContext( String repositoryGroupId, boolean deleteFiles )
        throws IOException,
            NoSuchRepositoryGroupException;

    IndexingContext getRepositoryGroupContext( String repositoryGroupId )
        throws NoSuchRepositoryGroupException;

    void setRepositoryIndexContextSearchable( String repositoryId, boolean searchable )
        throws IOException,
            NoSuchRepositoryException;

    // ----------------------------------------------------------------------------
    // Publish the used NexusIndexer
    // ----------------------------------------------------------------------------

    NexusIndexer getNexusIndexer();

    // ----------------------------------------------------------------------------
    // Publishing index
    // ----------------------------------------------------------------------------

    void publishAllIndex()
        throws IOException;

    void publishRepositoryIndex( String repositoryId )
        throws IOException,
            NoSuchRepositoryException;

    void publishRepositoryGroupIndex( String repositoryGroupId )
        throws IOException,
            NoSuchRepositoryGroupException;

    // ----------------------------------------------------------------------------
    // Reindexing related
    // ----------------------------------------------------------------------------

    void reindexAllRepositories( String path )
        throws IOException;

    void reindexRepository( String path, String repositoryId )
        throws NoSuchRepositoryException,
            IOException;

    void reindexRepositoryGroup( String path, String repositoryGroupId )
        throws NoSuchRepositoryGroupException,
            IOException;

    // ----------------------------------------------------------------------------
    // Identify
    // ----------------------------------------------------------------------------

    ArtifactInfo identifyArtifact( String type, String checksum )
        throws IOException,
            IndexContextInInconsistentStateException;

    // ----------------------------------------------------------------------------
    // Combined searching
    // ----------------------------------------------------------------------------

    FlatSearchResponse searchArtifactFlat( String term, String repositoryId, String groupId, Integer from, Integer count );

    FlatSearchResponse searchArtifactClassFlat( String term, String repositoryId, String groupId, Integer from,
        Integer count );

    FlatSearchResponse searchArtifactFlat( String gTerm, String aTerm, String vTerm, String pTerm, String cTerm,
        String repositoryId, String groupId, Integer from, Integer count );

    // ----------------------------------------------------------------------------
    // Query construction
    // ----------------------------------------------------------------------------

    Query constructQuery( String field, String query );

}
