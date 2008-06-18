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
package org.sonatype.nexus.index;

import java.io.IOException;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.index.context.IndexContextInInconsistentStateException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.events.EventListener;

public interface IndexerManager
    extends EventListener
{
    String ROLE = IndexerManager.class.getName();

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

    void addRepositoryGroupIndexContext( String repositoryGroupId )
        throws IOException,
            NoSuchRepositoryGroupException;

    void removeRepositoryGroupIndexContext( String repositoryGroupId, boolean deleteFiles )
        throws IOException,
            NoSuchRepositoryGroupException;

    void setRepositoryIndexContextSearchable( String repositoryId, boolean searchable )
        throws IOException,
            NoSuchRepositoryException;

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

    void reindexAllRepositories()
        throws IOException;

    void reindexRepository( String repositoryId )
        throws NoSuchRepositoryException,
            IOException;

    void reindexRepositoryGroup( String repositoryGroupId )
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

    FlatSearchResponse searchArtifactFlat( String gTerm, String aTerm, String vTerm, String cTerm, String repositoryId,
        String groupId, Integer from, Integer count );

    // ----------------------------------------------------------------------------
    // Query construction
    // ----------------------------------------------------------------------------

    Query constructQuery( String field, String query );

}
