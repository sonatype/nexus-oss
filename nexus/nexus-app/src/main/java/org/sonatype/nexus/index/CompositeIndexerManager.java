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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.Query;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.treeview.TreeNode;
import org.sonatype.nexus.index.treeview.TreeNodeFactory;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * An {@link IndexerManager} that will multiplex calls to all available {@link IndexerManager}s available. <br>
 * For non void methods will apply a strategy of first manager returning non null wins In case of search methods it will
 * acummulate teh results. If a manager does not implement a method it can throw {@link UnsupportedOperationException},
 * case when the manager is ignored.
 *
 * @author Alin Dreghiciu
 */
@Component( role = IndexerManager.class, hint = "composite" )
public class CompositeIndexerManager
    extends AbstractLogEnabled
    implements IndexerManager
{

    @Requirement( role = ComposableIndexerManager.class )
    private Map<String, IndexerManager> m_managers;

    /**
     * Test overwrite helper method to inject mock instances without plexus.
     * Better would have been constructor injection.
     * @param managers
     */
    void setManagers( Map<String, IndexerManager> managers ) {
        m_managers = managers;
    }

    /**
     * {@inheritDoc}
     */
    public void addRepositoryIndexContext( final String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.addRepositoryIndexContext( repositoryId );
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Query constructQuery( final String field, final String query )
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                final Query result = manager.constructQuery( field, query );
                if ( result != null )
                {
                    return result;
                }
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void downloadAllIndex()
        throws IOException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.downloadAllIndex();
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void downloadRepositoryGroupIndex( final String repositoryGroupId )
        throws IOException, NoSuchRepositoryException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.downloadRepositoryGroupIndex( repositoryGroupId );
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void downloadRepositoryIndex( final String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.downloadRepositoryIndex( repositoryId );
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public NexusIndexer getNexusIndexer()
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                final NexusIndexer result = manager.getNexusIndexer();
                if ( result != null )
                {
                    return result;
                }
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IndexingContext getRepositoryBestIndexContext( final String repositoryId )
        throws NoSuchRepositoryException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                final IndexingContext result = manager.getRepositoryBestIndexContext( repositoryId );
                if ( result != null )
                {
                    return result;
                }
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IndexingContext getRepositoryLocalIndexContext( final String repositoryId )
        throws NoSuchRepositoryException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                final IndexingContext result = manager.getRepositoryLocalIndexContext( repositoryId );
                if ( result != null )
                {
                    return result;
                }
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IndexingContext getRepositoryRemoteIndexContext( final String repositoryId )
        throws NoSuchRepositoryException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                final IndexingContext result = manager.getRepositoryRemoteIndexContext( repositoryId );
                if ( result != null )
                {
                    return result;
                }
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ArtifactInfo identifyArtifact( final String type, final String checksum )
        throws IOException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                final ArtifactInfo result = manager.identifyArtifact( type, checksum );
                if ( result != null )
                {
                    return result;
                }
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void publishAllIndex()
        throws IOException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.publishAllIndex();
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void publishRepositoryGroupIndex( final String repositoryGroupId )
        throws IOException, NoSuchRepositoryException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.publishRepositoryGroupIndex( repositoryGroupId );
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void publishRepositoryIndex( final String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.publishRepositoryIndex( repositoryId );
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void reindexAllRepositories( final String path, final boolean fullReindex )
        throws IOException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.reindexAllRepositories( path, fullReindex );
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void reindexRepository( final String path, final String repositoryId, final boolean fullReindex )
        throws NoSuchRepositoryException, IOException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.reindexRepository( path, repositoryId, fullReindex );
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void reindexRepositoryGroup( final String path, final String repositoryGroupId, final boolean fullReindex )
        throws NoSuchRepositoryException, IOException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.reindexRepositoryGroup( path, repositoryGroupId, fullReindex );
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeRepositoryIndexContext( final String repositoryId, final boolean deleteFiles )
        throws IOException, NoSuchRepositoryException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.removeRepositoryIndexContext( repositoryId, deleteFiles );
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void resetConfiguration()
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.resetConfiguration();
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void resetGroupIndex( String groupId )
        throws NoSuchRepositoryException, IOException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.resetGroupIndex( groupId );
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public FlatSearchResponse searchArtifactClassFlat( final String term, final String repositoryId,
                                                       final Integer from, final Integer count, final Integer hitLimit )
        throws NoSuchRepositoryException
    {
        int hits = 0;
        Set<ArtifactInfo> results = new HashSet<ArtifactInfo>();
                           
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                final FlatSearchResponse result =
                    manager.searchArtifactClassFlat( term, repositoryId, from, count, hitLimit );
                if ( result != null )
                {
                    results.addAll( result.getResults() );
                    hits += result.getTotalHits();
                }
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
        if ( results != null )
        {
            return new FlatSearchResponse( null, hits, results );
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public FlatSearchResponse searchArtifactFlat( final String gTerm, final String aTerm, final String vTerm,
                                                  final String pTerm, final String cTerm, final String repositoryId,
                                                  final Integer from, final Integer count, final Integer hitLimit )
        throws NoSuchRepositoryException
    {
        int hits = 0;
        Set<ArtifactInfo> results = new HashSet<ArtifactInfo>();
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                final FlatSearchResponse result =
                    manager.searchArtifactFlat( gTerm, aTerm, vTerm, pTerm, cTerm, repositoryId, from, count, hitLimit );
                if ( result != null )
                {
                    results.addAll( result.getResults() );
                    hits += result.getTotalHits();
                }
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
        if ( results != null )
        {
            return new FlatSearchResponse( null, hits, results );
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public FlatSearchResponse searchArtifactFlat( final String term, final String repositoryId, final Integer from,
                                                  final Integer count, final Integer hitLimit )
        throws NoSuchRepositoryException
    {
        int hits = 0;
        Set<ArtifactInfo> results = new HashSet<ArtifactInfo>();
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                final FlatSearchResponse result =
                    manager.searchArtifactFlat( term, repositoryId, from, count, hitLimit );
                if ( result != null )
                {
                    results.addAll( result.getResults() );
                    hits += result.getTotalHits();
                }
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
        if ( results != null )
        {
            return new FlatSearchResponse( null, hits, results );
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setRepositoryIndexContextSearchable( final String repositoryId, final boolean searchable )
        throws IOException, NoSuchRepositoryException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.setRepositoryIndexContextSearchable( repositoryId, searchable );
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void shutdown( final boolean deleteFiles )
        throws IOException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.shutdown( deleteFiles );
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void updateRepositoryIndexContext( final String repositoryId )
        throws IOException, NoSuchRepositoryException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.updateRepositoryIndexContext( repositoryId );
            }
            catch ( UnsupportedOperationException ignore )
            {
                // ignore
            }
        }
    }

    public void addItemToIndex( Repository repository, StorageItem item )
        throws IOException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.addItemToIndex( repository, item );
            }
            catch ( IOException e )
            {
                // ignore?
            }
        }
    }

    public void removeItemFromIndex( Repository repository, StorageItem item )
        throws IOException
    {
        for ( IndexerManager manager : m_managers.values() )
        {
            try
            {
                manager.removeItemFromIndex( repository, item );
            }
            catch ( IOException e )
            {
                // ignore?
            }
        }
    }

    public TreeNode listNodes( TreeNodeFactory factory, Repository repository, String path )
    {
        // not quite sure how this would get implemented here...for now
        // will simply iterate through until 1 is not null
        for ( IndexerManager manager : m_managers.values() )
        {
            TreeNode node = manager.listNodes( factory, repository, path );
            
            if ( node != null )
            {
                return node;
}
        }
        
        return null;
    }
}
