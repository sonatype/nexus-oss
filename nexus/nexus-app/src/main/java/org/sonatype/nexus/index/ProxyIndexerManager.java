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
import java.util.Map;

import org.apache.lucene.search.Query;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.treeview.TreeNode;
import org.sonatype.nexus.index.treeview.TreeNodeFactory;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * An {@link IndexerManager} that will delegate to the legacy Lucene Indexer ({@link DefaultIndexerManager} in case that
 * there are no other indexer managers or to a composite indexer manager {@link CompositeIndexerManager} in case that
 * more indexer managers are available. This indirection is for performnace reasons as in case that there are no more
 * indexer managers then lucene just a simple delegate call is performed.
 * 
 * @author Alin Dreghiciu
 */
@Component( role = IndexerManager.class )
public class ProxyIndexerManager
    implements IndexerManager
{

    @Requirement( role = ComposableIndexerManager.class )
    private Map<String, IndexerManager> m_indexers;

    /**
     * {@link IndexerManager} delegate. Either Lucene (default) or a composite.
     */
    @Requirement( role = ComposableIndexerManager.class, hint = "lucene" )
    private IndexerManager m_lucene;

    @Requirement( hint = "composite" )
    private IndexerManager m_composite;

    /**
     * On intitalization decide if it should proxy directly to lucene (aalready teh value of delegate) or use the
     * composite as delegate. {@inheritDoc}
     */
    private IndexerManager indexer()
    {
        if ( m_indexers.size() > 1 )
        {
            return m_composite;
        }
        return m_lucene;
    }

    /**
     * {@inheritDoc}
     */
    public void addRepositoryIndexContext( String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        indexer().addRepositoryIndexContext( repositoryId );
    }

    /**
     * {@inheritDoc}
     */
    public Query constructQuery( String field, String query )
    {
        // Query is only supported by Lucene Indexer
        return m_lucene.constructQuery( field, query );
    }

    /**
     * {@inheritDoc}
     */
    public void downloadAllIndex()
        throws IOException
    {
        indexer().downloadAllIndex();
    }

    /**
     * {@inheritDoc}
     */
    public void downloadRepositoryGroupIndex( String repositoryGroupId )
        throws IOException, NoSuchRepositoryException
    {
        indexer().downloadRepositoryGroupIndex( repositoryGroupId );
    }

    /**
     * {@inheritDoc}
     */
    public void downloadRepositoryIndex( String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        indexer().downloadRepositoryIndex( repositoryId );
    }

    /**
     * {@inheritDoc}
     */
    public NexusIndexer getNexusIndexer()
    {
        // Nexus Indexer is Lucene Indexer
        return m_lucene.getNexusIndexer();
    }

    /**
     * {@inheritDoc}
     */
    public IndexingContext getRepositoryBestIndexContext( String repositoryId )
        throws NoSuchRepositoryException
    {
        // getting first served does not work so force Lucene
        return m_lucene.getRepositoryBestIndexContext( repositoryId );
    }

    /**
     * {@inheritDoc}
     */
    public IndexingContext getRepositoryLocalIndexContext( String repositoryId )
        throws NoSuchRepositoryException
    {
        // getting first served does not work so force Lucene
        return m_lucene.getRepositoryLocalIndexContext( repositoryId );
    }

    /**
     * {@inheritDoc}
     */
    public IndexingContext getRepositoryRemoteIndexContext( String repositoryId )
        throws NoSuchRepositoryException
    {
        // getting first served does not work so force Lucene
        return m_lucene.getRepositoryRemoteIndexContext( repositoryId );
    }

    /**
     * {@inheritDoc}
     */
    public ArtifactInfo identifyArtifact( String type, String checksum )
        throws IOException
    {
        // getting first served does not work so force Lucene
        return m_lucene.identifyArtifact( type, checksum );
    }

    /**
     * {@inheritDoc}
     */
    public void publishAllIndex()
        throws IOException
    {
        indexer().publishAllIndex();
    }

    /**
     * {@inheritDoc}
     */
    public void publishRepositoryGroupIndex( String repositoryGroupId )
        throws IOException, NoSuchRepositoryException
    {
        indexer().publishRepositoryGroupIndex( repositoryGroupId );
    }

    /**
     * {@inheritDoc}
     */
    public void publishRepositoryIndex( String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        indexer().publishRepositoryIndex( repositoryId );
    }

    /**
     * {@inheritDoc}
     */
    public void reindexAllRepositories( String path, boolean fullReindex )
        throws IOException
    {
        indexer().reindexAllRepositories( path, fullReindex );
    }

    /**
     * {@inheritDoc}
     */
    public void reindexRepository( String path, String repositoryId, boolean fullReindex )
        throws NoSuchRepositoryException, IOException
    {
        indexer().reindexRepository( path, repositoryId, fullReindex );
    }

    /**
     * {@inheritDoc}
     */
    public void reindexRepositoryGroup( String path, String repositoryGroupId, boolean fullReindex )
        throws NoSuchRepositoryException, IOException
    {
        indexer().reindexRepositoryGroup( path, repositoryGroupId, fullReindex );
    }

    /**
     * {@inheritDoc}
     */
    public void removeRepositoryIndexContext( String repositoryId, boolean deleteFiles )
        throws IOException, NoSuchRepositoryException
    {
        indexer().removeRepositoryIndexContext( repositoryId, deleteFiles );
    }

    /**
     * {@inheritDoc}
     */
    public void resetConfiguration()
    {
        indexer().resetConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    public void resetGroupIndex( String groupId )
        throws NoSuchRepositoryException, IOException
    {
        indexer().resetGroupIndex( groupId );
    }

    /**
     * {@inheritDoc}
     */
    public FlatSearchResponse searchArtifactClassFlat( String term, String repositoryId, Integer from, Integer count,
                                                       Integer hitLimit )
        throws NoSuchRepositoryException
    {
        return indexer().searchArtifactClassFlat( term, repositoryId, from, count, hitLimit );
    }

    /**
     * {@inheritDoc}
     */
    public FlatSearchResponse searchArtifactFlat( String gTerm, String aTerm, String vTerm, String pTerm, String cTerm,
                                                  String repositoryId, Integer from, Integer count, Integer hitLimit )
        throws NoSuchRepositoryException
    {
        return indexer().searchArtifactFlat( gTerm, aTerm, vTerm, pTerm, cTerm, repositoryId, from, count, hitLimit );
    }

    /**
     * {@inheritDoc}
     */
    public FlatSearchResponse searchArtifactFlat( String term, String repositoryId, Integer from, Integer count,
                                                  Integer hitLimit )
        throws NoSuchRepositoryException
    {
        return indexer().searchArtifactFlat( term, repositoryId, from, count, hitLimit );
    }

    /**
     * {@inheritDoc}
     */
    public void setRepositoryIndexContextSearchable( String repositoryId, boolean searchable )
        throws IOException, NoSuchRepositoryException
    {
        indexer().setRepositoryIndexContextSearchable( repositoryId, searchable );
    }

    /**
     * {@inheritDoc}
     */
    public void shutdown( boolean deleteFiles )
        throws IOException
    {
        indexer().shutdown( deleteFiles );
    }

    /**
     * {@inheritDoc}
     */
    public void updateRepositoryIndexContext( String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        indexer().updateRepositoryIndexContext( repositoryId );
    }

    public void addItemToIndex( Repository repository, StorageItem item )
        throws IOException
    {
        indexer().addItemToIndex( repository, item );
    }

    public void removeItemFromIndex( Repository repository, StorageItem item )
        throws IOException
    {
        indexer().removeItemFromIndex( repository, item );
    }

    public TreeNode listNodes( TreeNodeFactory factory, Repository repository, String path )
    {
        return indexer().listNodes( factory, repository, path );
    }

}