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
package org.sonatype.nexus.index;

import java.io.IOException;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;

public interface IndexerManager
{
    // ----------------------------------------------------------------------------
    // Context management et al
    // ----------------------------------------------------------------------------

    void shutdown( boolean deleteFiles )
        throws IOException;

    void resetConfiguration();

    void addRepositoryIndexContext( String repositoryId )
        throws IOException,
            NoSuchRepositoryException;

    void removeRepositoryIndexContext( String repositoryId, boolean deleteFiles )
        throws IOException,
            NoSuchRepositoryException;

    void updateRepositoryIndexContext( String repositoryId )
        throws IOException,
            NoSuchRepositoryException;

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

    /**
     * Flags an indexing context should be searched in global searches or not.
     * 
     * @param repositoryId
     * @param searchable
     * @throws IOException
     * @throws NoSuchRepositoryException
     */
    void setRepositoryIndexContextSearchable( String repositoryId, boolean searchable )
        throws IOException,
            NoSuchRepositoryException;

    // ----------------------------------------------------------------------------
    // Publish the used NexusIndexer
    // ----------------------------------------------------------------------------

    NexusIndexer getNexusIndexer();

    // ----------------------------------------------------------------------------
    // Reindexing related (will do local-scan, remote-download, merge, publish)
    // ----------------------------------------------------------------------------

    void reindexAllRepositories( String path )
        throws IOException;

    void reindexRepository( String path, String repositoryId )
        throws NoSuchRepositoryException,
            IOException;

    void reindexRepositoryGroup( String path, String repositoryGroupId )
        throws NoSuchRepositoryException,
            IOException;

    // ----------------------------------------------------------------------------
    // Downloading remote indexes (will do remote-download, merge only)
    // ----------------------------------------------------------------------------

    void downloadAllIndex()
        throws IOException;

    void downloadRepositoryIndex( String repositoryId )
        throws IOException,
            NoSuchRepositoryException;

    void downloadRepositoryGroupIndex( String repositoryGroupId )
        throws IOException,
            NoSuchRepositoryException;

    // ----------------------------------------------------------------------------
    // Publishing index (will do publish only)
    // ----------------------------------------------------------------------------

    void publishAllIndex()
        throws IOException;

    void publishRepositoryIndex( String repositoryId )
        throws IOException,
            NoSuchRepositoryException;

    void publishRepositoryGroupIndex( String repositoryGroupId )
        throws IOException,
            NoSuchRepositoryException;

    // ----------------------------------------------------------------------------
    // Identify
    // ----------------------------------------------------------------------------

    ArtifactInfo identifyArtifact( String type, String checksum )
        throws IOException;

    // ----------------------------------------------------------------------------
    // Combined searching
    // ----------------------------------------------------------------------------

    FlatSearchResponse searchArtifactFlat( String term, String repositoryId, Integer from, Integer count )
        throws NoSuchRepositoryException;

    FlatSearchResponse searchArtifactClassFlat( String term, String repositoryId, Integer from, Integer count )
        throws NoSuchRepositoryException;

    FlatSearchResponse searchArtifactFlat( String gTerm, String aTerm, String vTerm, String pTerm, String cTerm,
        String repositoryId, Integer from, Integer count )
        throws NoSuchRepositoryException;

    // ----------------------------------------------------------------------------
    // Query construction
    // ----------------------------------------------------------------------------

    Query constructQuery( String field, String query );

}
