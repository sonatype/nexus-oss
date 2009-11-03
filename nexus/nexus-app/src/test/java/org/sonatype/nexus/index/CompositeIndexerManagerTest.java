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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import org.apache.lucene.search.Query;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

import static org.easymock.EasyMock.*;

/**
 * This test tests that the composite tests for each public method:
 * 1. composited elements are called.
 * 2. composition is done as expected.
 *
 * @author Toni Menzel
 */
public class CompositeIndexerManagerTest extends TestCase
{

    public void testAddRepositoryIndexContext()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );

        man1.addRepositoryIndexContext( "foo" );
        man2.addRepositoryIndexContext( "foo" );

        replay( man1, man2 );
        composite.addRepositoryIndexContext( "foo" );
        verify( man1, man2 );
    }

    public void testConstructQuery()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );

        expect( man1.constructQuery( "foo", "bar" ) ).andReturn( getQueryDummy() );

        replay( man1, man2 );
        composite.constructQuery( "foo", "bar" );
        verify( man1, man2 );
    }

    public void testDownloadAllIndex()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );

        man1.downloadAllIndex();
        man2.downloadAllIndex();

        replay( man1, man2 );
        composite.downloadAllIndex();
        verify( man1, man2 );
    }

    public void testDownloadRepositoryGroupIndex()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        man1.downloadRepositoryGroupIndex( "foo" );
        man2.downloadRepositoryGroupIndex( "foo" );

        replay( man1, man2 );
        composite.downloadRepositoryGroupIndex( "foo" );
        verify( man1, man2 );
    }

    public void testDownloadRepositoryIndex()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        man1.downloadRepositoryIndex( "foo" );
        man2.downloadRepositoryIndex( "foo" );

        replay( man1, man2 );
        composite.downloadRepositoryIndex( "foo" );
        verify( man1, man2 );
    }

    public void testGetNexusIndexer()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        expect( man1.getNexusIndexer() ).andReturn( createMock( NexusIndexer.class ) );

        replay( man1, man2 );
        composite.getNexusIndexer();
        verify( man1, man2 );
    }

    public void testGetRepositoryBestIndexContext()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        expect( man1.getRepositoryBestIndexContext( "foo" ) ).andReturn( createMock( IndexingContext.class ) );
        replay( man1, man2 );
        composite.getRepositoryBestIndexContext( "foo" );
        verify( man1, man2 );
    }

    public void testGetRepositoryLocalIndexContext()
        throws NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        expect( man1.getRepositoryLocalIndexContext( "foo" ) ).andReturn( createMock( IndexingContext.class ) );
        replay( man1, man2 );
        composite.getRepositoryLocalIndexContext( "foo" );
        verify( man1, man2 );
    }

    public void testGetRepositoryRemoteIndexContext()
        throws NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        expect( man1.getRepositoryRemoteIndexContext( "foo" ) ).andReturn( createMock( IndexingContext.class ) );
        replay( man1, man2 );
        composite.getRepositoryRemoteIndexContext( "foo" );
        verify( man1, man2 );
    }

    public void testIdentifyArtifact()
        throws IOException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        expect( man1.identifyArtifact( "foo", "bar" ) ).andReturn( getDummyArtifactInfo() );
        replay( man1, man2 );
        composite.identifyArtifact( "foo", "bar" );
        verify( man1, man2 );
    }

    public void testPublishAllIndex()
        throws IOException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        man1.publishAllIndex();
        man2.publishAllIndex();

        replay( man1, man2 );
        composite.publishAllIndex();
        verify( man1, man2 );
    }

    public void testPublishRepositoryGroupIndex()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        man1.publishRepositoryGroupIndex( "foo" );
        man2.publishRepositoryGroupIndex( "foo" );

        replay( man1, man2 );
        composite.publishRepositoryGroupIndex( "foo" );
        verify( man1, man2 );
    }

    public void testPublishRepositoryIndex()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        man1.publishRepositoryIndex( "foo" );
        man2.publishRepositoryIndex( "foo" );

        replay( man1, man2 );
        composite.publishRepositoryIndex( "foo" );
        verify( man1, man2 );
    }

    public void testReindexAllRepositories()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        man1.reindexAllRepositories( "foo", true );
        man2.reindexAllRepositories( "foo", true );

        replay( man1, man2 );
        composite.reindexAllRepositories( "foo", true );
        verify( man1, man2 );
    }

    public void testReindexRepository()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        man1.reindexRepository( "foo", "bar", true );
        man2.reindexRepository( "foo", "bar", true );

        replay( man1, man2 );
        composite.reindexRepository( "foo", "bar", true );
        verify( man1, man2 );
    }

    public void testReindexRepositoryGroup()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        man1.reindexRepositoryGroup( "foo", "bar", true );
        man2.reindexRepositoryGroup( "foo", "bar", true );

        replay( man1, man2 );
        composite.reindexRepositoryGroup( "foo", "bar", true );
        verify( man1, man2 );
    }

    public void testRemoveItemFromIndex()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );

        Repository repos = createMock( Repository.class );
        StorageItem storageItem = createMock( StorageItem.class );

        man1.removeItemFromIndex( repos, storageItem );
        man2.removeItemFromIndex( repos, storageItem );
        replay( man1, man2 );
        composite.removeItemFromIndex( repos, storageItem );
        verify( man1, man2 );
    }

    public void testResetConfiguration()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        man1.resetConfiguration();
        man2.resetConfiguration();

        replay( man1, man2 );
        composite.resetConfiguration();
        verify( man1, man2 );
    }

    public void testResetGroupIndex()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        man1.resetGroupIndex( "foo" );
        man2.resetGroupIndex( "foo" );

        replay( man1, man2 );
        composite.resetGroupIndex( "foo" );
        verify( man1, man2 );
    }

    public void testSearchArtifactClassFlat()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        expect( man1.searchArtifactClassFlat( "foo", "bar", 0, 100, 100 ) ).andReturn( getFlatSearchResponseDummy( "search1_", 10 ) );
        expect( man2.searchArtifactClassFlat( "foo", "bar", 0, 100, 100 ) ).andReturn( getFlatSearchResponseDummy( "search2_", 10 ) );

        replay( man1, man2 );
        FlatSearchResponse searchResponse = composite.searchArtifactClassFlat( "foo", "bar", 0, 100, 100 );
        // check accumulated
        assertNotNull( searchResponse );
        assertEquals( "Search results must be accumulated", 20, searchResponse.getResults().size() );
        verify( man1, man2 );
    }

    public void testSearchArtifactFlat()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        expect( man1.searchArtifactFlat( "foo", "bar", "cheese", "cake", "bacon", "pasta", 0, 100, 100 ) ).andReturn( getFlatSearchResponseDummy( "search1_", 10 ) );
        expect( man2.searchArtifactFlat( "foo", "bar", "cheese", "cake", "bacon", "pasta", 0, 100, 100 ) ).andReturn( getFlatSearchResponseDummy( "search1_", 10 ) );

        replay( man1, man2 );
        FlatSearchResponse searchResponse = composite.searchArtifactFlat( "foo", "bar", "cheese", "cake", "bacon", "pasta", 0, 100, 100 );
        // check accumulated
        assertNotNull( searchResponse );
        assertEquals( "Search results must be accumulated", 20, searchResponse.getResults().size() );
        verify( man1, man2 );
    }

    public void testSearchArtifactFlat2()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        expect( man1.searchArtifactFlat( "foo", "bar", 0, 100, 100 ) ).andReturn( getFlatSearchResponseDummy( "search1_", 10 ) );
        expect( man2.searchArtifactFlat( "foo", "bar", 0, 100, 100 ) ).andReturn( getFlatSearchResponseDummy( "search1_", 10 ) );

        replay( man1, man2 );
        FlatSearchResponse searchResponse = composite.searchArtifactFlat( "foo", "bar", 0, 100, 100 );
        // check accumulated
        assertNotNull( searchResponse );
        assertEquals( "Search results must be accumulated", 20, searchResponse.getResults().size() );
        verify( man1, man2 );
    }

    public void testSetRepositoryIndexContextSearchable()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        man1.setRepositoryIndexContextSearchable( "foo", true );
        man2.setRepositoryIndexContextSearchable( "foo", true );

        replay( man1, man2 );
        composite.setRepositoryIndexContextSearchable( "foo", true );
        verify( man1, man2 );
    }

    public void testShutdown()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        man1.shutdown( true );
        man2.shutdown( true );

        replay( man1, man2 );
        composite.shutdown( true );
        verify( man1, man2 );
    }

    public void testUpdateRepositoryIndexContext()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );
        man1.updateRepositoryIndexContext( "foo" );
        man2.updateRepositoryIndexContext( "foo" );

        replay( man1, man2 );
        composite.updateRepositoryIndexContext( "foo" );
        verify( man1, man2 );
    }

    public void testAddItemToIndex()
        throws IOException, NoSuchRepositoryException
    {
        IndexerManager man1 = createMock( IndexerManager.class );
        IndexerManager man2 = createMock( IndexerManager.class );
        IndexerManager composite = prepareComposite( man1, man2 );

        Repository repos = createMock( Repository.class );
        StorageItem storageItem = createMock( StorageItem.class );

        man1.addItemToIndex( repos, storageItem );
        man2.addItemToIndex( repos, storageItem );

        replay( man1, man2 );
        composite.addItemToIndex( repos, storageItem );
        verify( man1, man2 );
    }

    private FlatSearchResponse getFlatSearchResponseDummy( String resultPrefix, int items )
    {
        Set<ArtifactInfo> s = new HashSet<ArtifactInfo>( items );
        for( int i = 0; i < items; i++ )
        {
            s.add( new ArtifactInfo( resultPrefix, resultPrefix, resultPrefix + i, "1.0.0", "" ) );
        }
        return new FlatSearchResponse( getQueryDummy(), items, s );
    }

    private Query getQueryDummy()
    {
        return new Query()
        {

            @Override
            public String toString( String s )
            {
                return s;
            }
        };
    }

    /**
     * @param man managers you wanna compose
     *
     * @return fully prepared manager composite.
     */
    private IndexerManager prepareComposite( IndexerManager... man )
    {
        CompositeIndexerManager manager = new CompositeIndexerManager();
        Map<String, IndexerManager> map = new HashMap<String, IndexerManager>();
        int is = 0;
        for( int i = 0; i < man.length; i++ )
        {
            map.put( "manager" + i, man[ i ] );
        }
        manager.setManagers( map );
        return manager;
    }

    public ArtifactInfo getDummyArtifactInfo()
    {
        return new ArtifactInfo()
        {
        };
    }
}
