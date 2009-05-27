package org.sonatype.nexus.index;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.scheduling.NexusScheduler;

public abstract class AbstractIndexerManagerTest
    extends AbstractMavenRepoContentTests
{

    protected IndexerManager indexerManager;

    protected NexusScheduler nexusScheduler;

    public AbstractIndexerManagerTest()
    {
        super();
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        indexerManager = lookup( IndexerManager.class );

        nexusScheduler = lookup( NexusScheduler.class );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        indexerManager.shutdown( false );

        super.tearDown();
    }

    protected void searchFor( String groupId, int expected )
        throws IOException
    {
        Query query = indexerManager.getNexusIndexer().constructQuery( ArtifactInfo.GROUP_ID, groupId );
        FlatSearchRequest request = new FlatSearchRequest( query );

        FlatSearchResponse response = indexerManager.getNexusIndexer().searchFlat( request );

        Collection<ArtifactInfo> result = response.getResults();

        assertEquals( result.toString(), expected, result.size() );
    }

    protected void searchFor( String groupId, int expected, String repoId )
    throws IOException, Exception
    {
        FlatSearchResponse response = indexerManager.searchArtifactFlat( groupId, null, null, null, null, repoId, 0, 100 );

        Collection<ArtifactInfo> result = response.getResults();

        assertEquals( result.toString(), expected, result.size() );
    }

}