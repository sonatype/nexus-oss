package org.sonatype.nexus.index;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.lucene.search.Query;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.FlatSearchResponse;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.SearchType;
import org.apache.maven.index.context.IndexingContext;
import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
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
        Query query = indexerManager.constructQuery( MAVEN.GROUP_ID, groupId, SearchType.EXACT );

        IteratorSearchResponse response;

        try
        {
            response = indexerManager.searchQueryIterator( query, null, null, null, null, false, null );
        }
        catch ( NoSuchRepositoryException e )
        {
            // will not happen since we are not selecting a repo to search
            throw new IOException( "Huh?" );
        }

        ArrayList<ArtifactInfo> results = new ArrayList<ArtifactInfo>( response.getTotalHits() );

        for ( ArtifactInfo hit : response )
        {
            results.add( hit );
        }

        assertEquals( "Query \"" + query + "\" returned wrong results: " + results + "!", expected, results.size() );
    }

    protected IteratorSearchResponse searchForKeywordNG( String term, int expected )
        throws Exception
    {
        IteratorSearchResponse result =
            indexerManager.searchArtifactIterator( term, null, null, null, null, false, SearchType.SCORED, null );

        if ( expected != result.getTotalHits() )
        {
            // dump the stuff
            StringBuilder sb = new StringBuilder( "Found artifacts:\n" );

            for ( ArtifactInfo ai : result )
            {
                sb.append( ai.context ).append( " : " ).append( ai.toString() ).append( "\n" );
            }

            fail( sb.toString() + "\nUnexpected result set size! We expected " + expected + " but got "
                + result.getTotalHits() );
        }

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
        IndexingContext context =
            ( (DefaultIndexerManager) indexerManager ).getRepositoryLocalIndexContext( repo.getId() );
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