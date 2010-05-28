package org.sonatype.nexus.index;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.NexusScheduler;

public abstract class AbstractIndexerManagerTest
    extends AbstractMavenRepoContentTests
{

    protected IndexerManager indexerManager;

    protected NexusScheduler nexusScheduler;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusConfiguration.setSecurityEnabled( false );

        nexusConfiguration.saveConfiguration();

        indexerManager = lookup( IndexerManager.class );

        nexusScheduler = lookup( NexusScheduler.class );
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

    protected IteratorSearchResponse searchForKeywordNG( String term, int expected )
        throws Exception
    {
        IteratorSearchResponse result = indexerManager.searchArtifactIterator( term, null, null, null, null, false, SearchType.SCORED, null );

        assertEquals( "Unexpected result set size!", expected, result.getTotalHits() );

        return result;
    }

    protected void searchFor( String groupId, int expected, String repoId )
        throws IOException, Exception
    {
        FlatSearchResponse response =
            indexerManager.searchArtifactFlat( groupId, null, null, null, null, repoId, 0, 100, null );

        Collection<ArtifactInfo> result = response.getResults();

        assertEquals( result.toString(), expected, result.size() );
    }

    protected void assertTemporatyContexts( final Repository repo )
        throws Exception
    {
        IndexingContext context = indexerManager.getRepositoryLocalIndexContext( repo.getId() );
        File dir = context.getIndexDirectoryFile().getParentFile();

        File[] contextDirs = dir.listFiles( new FilenameFilter()
        {
            public boolean accept( File dir, String name )
            {
                return name.startsWith( repo.getId() + "-local" );
            }
        } );

        assertEquals( 1, contextDirs.length );
    }

}