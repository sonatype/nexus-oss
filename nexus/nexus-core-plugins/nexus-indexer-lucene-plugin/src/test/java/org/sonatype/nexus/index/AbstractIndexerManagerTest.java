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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.search.Query;
import org.apache.maven.index.ArtifactInfo;
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

    protected void searchForKeywordNG( String term, int expected )
        throws Exception
    {
        IteratorSearchResponse result =
            indexerManager.searchArtifactIterator( term, null, null, null, null, false, SearchType.SCORED, null );

        try
        {
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
        }
        finally
        {
            result.close();
        }
    }

    protected void searchFor( String groupId, int expected, String repoId )
        throws IOException, Exception
    {
        Query q = indexerManager.constructQuery( MAVEN.GROUP_ID, groupId, SearchType.EXACT );

        IteratorSearchResponse response = indexerManager.searchQueryIterator( q, repoId, null, null, null, false, null );

        ArrayList<ArtifactInfo> ais = new ArrayList<ArtifactInfo>( response.getTotalHits() );

        for ( ArtifactInfo ai : response )
        {
            ais.add( ai );
        }

        assertEquals( ais.toString(), expected, ais.size() );
    }

    protected void assertTemporatyContexts( final Repository repo )
        throws Exception
    {
        IndexingContext context =
            ( (DefaultIndexerManager) indexerManager ).getRepositoryIndexContext( repo.getId() );
        File dir = context.getIndexDirectoryFile().getParentFile();

        File[] contextDirs = dir.listFiles( new FilenameFilter()
        {
            public boolean accept( File dir, String name )
            {
                return name.startsWith( repo.getId() + "-ctx" );
            }
        } );

        assertEquals( 1, contextDirs.length );
    }
}