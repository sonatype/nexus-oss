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
package org.sonatype.nexus;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.packer.IndexPacker;
import org.sonatype.nexus.index.packer.IndexPackingRequest;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;

public class ReindexTest
    extends AbstractMavenRepoContentTests
{
    public static final long A_DAY_MILLIS = 24 * 60 * 60 * 1000;
    
    private IndexerManager indexerManager;

    private ServletServer servletServer;

    private NexusIndexer nexusIndexer;

    private IndexPacker indexPacker;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        indexerManager = lookup( IndexerManager.class );
        
        nexusIndexer = lookup( NexusIndexer.class );

        indexPacker = lookup( IndexPacker.class );

        servletServer = lookup( ServletServer.class );

        servletServer.start();
    }

    protected void tearDown()
        throws Exception
    {
        servletServer.stop();

        super.tearDown();
    }

    protected void makeCentralPointTo( String url )
        throws Exception
    {
        MavenProxyRepository central =
            repositoryRegistry.getRepositoryWithFacet( "central", MavenProxyRepository.class );

        // redirect it to our "sppof" jetty (see ReindexTest.xml in src/test/resources....
        central.setRemoteUrl( url );

        // make the central download the remote indexes is found
        central.setDownloadRemoteIndexes( true );

        nexusConfiguration.saveConfiguration();
    }

    protected File getIndexFamilyDirectory( String path )
    {
        File indexDirectory = new File( new File( getBasedir() ), "target/indexFamily/" + path );

        return indexDirectory;
    }

    protected File getRemoteRepositoryRoot( String path )
    {
        // Be aware, that "name" != "repoId"! For example, "central-inc1", "central-inc2"... are all "slices" of
        // "central" repo in different time!
        File root = new File( new File( getBasedir() ), "target/test-classes/reposes-remote/" + path );

        return root;
    }

    protected void shiftContextInTime( IndexingContext ctx, int shiftDays )
        throws IOException
    {
        if ( shiftDays != 0 )
        {
            IndexWriter iw = ctx.getIndexWriter();

            for ( int docNum = 0; docNum < ctx.getIndexReader().maxDoc(); docNum++ )
            {
                if ( !ctx.getIndexReader().isDeleted( docNum ) )
                {
                    Document doc = ctx.getIndexReader().document( docNum );

                    String lastModified = doc.get( ArtifactInfo.LAST_MODIFIED );

                    if ( lastModified != null )
                    {
                        long lm = Long.parseLong( lastModified );

                        lm = lm + ( shiftDays * A_DAY_MILLIS );

                        doc.removeFields( ArtifactInfo.LAST_MODIFIED );

                        doc.add( new Field( ArtifactInfo.LAST_MODIFIED, Long.toString( lm ), Field.Store.YES,
                                            Field.Index.NO ) );

                        iw.updateDocument( new Term( ArtifactInfo.UINFO, doc.get( ArtifactInfo.UINFO ) ), doc );
                    }
                }
            }

            iw.optimize();

            iw.close();

            // shift timestamp too
            if ( ctx.getTimestamp() != null )
            {
                ctx.updateTimestamp( true, new Date( ctx.getTimestamp().getTime() + ( shiftDays * A_DAY_MILLIS ) ) );
            }
            else
            {
                ctx.updateTimestamp( true, new Date( System.currentTimeMillis() + ( shiftDays * A_DAY_MILLIS ) ) );
            }
        }
    }

    /**
     * Will reindex, shift if needed and publish indexes for a "remote" repository (published over jetty component).
     * 
     * @param repositoryRoot
     * @param repositoryId
     * @param deleteIndexFiles
     * @param shiftDays
     * @throws IOException
     */
    protected void reindexRemoteRepositoryAndPublish( File repositoryRoot, String repositoryId,
                                                      boolean deleteIndexFiles, int shiftDays )
        throws IOException
    {
        File indexDirectory = getIndexFamilyDirectory( repositoryId );

        Directory directory = FSDirectory.getDirectory( indexDirectory );

        IndexingContext ctx =
            nexusIndexer.addIndexingContextForced( repositoryId + "-temp", repositoryId, repositoryRoot, directory,
                                                   null, null, FULL_CREATORS );

        // shifting if needed (very crude way to do it, but heh)
        shiftContextInTime( ctx, shiftDays );

        // and scan "today"
        nexusIndexer.scan( ctx );

        ctx.updateTimestamp( true );

        // pack it up
        File targetDir = new File( repositoryRoot, ".index" );

        targetDir.mkdirs();

        IndexPackingRequest ipr = new IndexPackingRequest( ctx, targetDir );

        ipr.setCreateIncrementalChunks( true );

        indexPacker.packIndex( ipr );

        nexusIndexer.removeIndexingContext( ctx, deleteIndexFiles );
    }

    protected void validateIndexWithIdentify( boolean shouldBePresent, String sha1Hash, String gid, String aid,
                                              String version )
        throws Exception
    {
        ArtifactInfo ai = indexerManager.identifyArtifact( ArtifactInfo.SHA1, sha1Hash );

        if ( shouldBePresent )
        {
            assertNotNull( "Should find " + gid + ":" + aid + ":" + version, ai );

            assertEquals( gid, ai.groupId );
            assertEquals( aid, ai.artifactId );
            assertEquals( version, ai.version );
        }
        else
        {
            assertNull( "Should not find " + gid + ":" + aid + ":" + version, ai );
        }
    }

    public void testHostedRepositoryReindex()
        throws Exception
    {
        fillInRepo();

        indexerManager.reindexRepository( null, "releases" );

        validateIndexWithIdentify( true, "86e12071021fa0be4ec809d4d2e08f07b80d4877", "org.sonatype.nexus",
                                   "nexus-indexer", "1.0-beta-4" );
    }

    public void testProxyRepositoryReindex()
        throws Exception
    {
        fillInRepo();

        reindexRemoteRepositoryAndPublish( getRemoteRepositoryRoot( "central" ), "central", true, 0 );

        makeCentralPointTo( "http://localhost:12345/central/" );

        indexerManager.reindexRepository( null, "central" );

        validateIndexWithIdentify( true, "057b8740427ee6d7b0b60792751356cad17dc0d9", "log4j", "log4j", "1.2.12" );
    }

    public void testGroupReindex()
        throws Exception
    {
        fillInRepo();

        reindexRemoteRepositoryAndPublish( getRemoteRepositoryRoot( "central" ), "central", true, 0 );

        makeCentralPointTo( "http://localhost:12345/central/" );

        // central is member of public group
        indexerManager.reindexRepositoryGroup( null, "public" );

        validateIndexWithIdentify( true, "057b8740427ee6d7b0b60792751356cad17dc0d9", "log4j", "log4j", "1.2.12" );
    }

    public void testCurrentIncrementalIndexes()
        throws Exception
    {
        // day 1
        reindexRemoteRepositoryAndPublish( getRemoteRepositoryRoot( "central-inc1" ), "central", false, 0 );

        makeCentralPointTo( "http://localhost:12345/central-inc1/" );

        indexerManager.reindexRepository( null, "central" );

        // validation
        validateIndexWithIdentify( true, "cf4f67dae5df4f9932ae7810f4548ef3e14dd35e", "antlr", "antlr", "2.7.6" );
        validateIndexWithIdentify( false, "83cd2cd674a217ade95a4bb83a8a14f351f48bd0", "antlr", "antlr", "2.7.7" );

        validateIndexWithIdentify( true, "3640dd71069d7986c9a14d333519216f4ca5c094", "log4j", "log4j", "1.2.8" );
        validateIndexWithIdentify( false, "057b8740427ee6d7b0b60792751356cad17dc0d9", "log4j", "log4j", "1.2.12" );
        validateIndexWithIdentify( false, "f0a0d2e29ed910808c33135a3a5a51bba6358f7b", "log4j", "log4j", "1.2.15" );

        // day 2 (1 day passed), so shift both ctxes "in time"
        reindexRemoteRepositoryAndPublish( getRemoteRepositoryRoot( "central-inc2" ), "central", false, -1 );
        shiftContextInTime( indexerManager.getRepositoryRemoteIndexContext( "central" ), -1 );

        makeCentralPointTo( "http://localhost:12345/central-inc2/" );

        indexerManager.reindexRepository( null, "central" );

        // validation
        validateIndexWithIdentify( true, "cf4f67dae5df4f9932ae7810f4548ef3e14dd35e", "antlr", "antlr", "2.7.6" );
        validateIndexWithIdentify( true, "83cd2cd674a217ade95a4bb83a8a14f351f48bd0", "antlr", "antlr", "2.7.7" );

        validateIndexWithIdentify( true, "3640dd71069d7986c9a14d333519216f4ca5c094", "log4j", "log4j", "1.2.8" );
        validateIndexWithIdentify( true, "057b8740427ee6d7b0b60792751356cad17dc0d9", "log4j", "log4j", "1.2.12" );
        validateIndexWithIdentify( false, "f0a0d2e29ed910808c33135a3a5a51bba6358f7b", "log4j", "log4j", "1.2.15" );

        // day 3
        reindexRemoteRepositoryAndPublish( getRemoteRepositoryRoot( "central-inc3" ), "central", false, -1 );
        shiftContextInTime( indexerManager.getRepositoryRemoteIndexContext( "central" ), -1 );

        makeCentralPointTo( "http://localhost:12345/central-inc3/" );

        indexerManager.reindexRepository( null, "central" );

        // validation
        validateIndexWithIdentify( true, "cf4f67dae5df4f9932ae7810f4548ef3e14dd35e", "antlr", "antlr", "2.7.6" );
        validateIndexWithIdentify( true, "83cd2cd674a217ade95a4bb83a8a14f351f48bd0", "antlr", "antlr", "2.7.7" );

        validateIndexWithIdentify( true, "3640dd71069d7986c9a14d333519216f4ca5c094", "log4j", "log4j", "1.2.8" );
        validateIndexWithIdentify( true, "057b8740427ee6d7b0b60792751356cad17dc0d9", "log4j", "log4j", "1.2.12" );
        validateIndexWithIdentify( true, "f0a0d2e29ed910808c33135a3a5a51bba6358f7b", "log4j", "log4j", "1.2.15" );
    }

    public void testV1IncrementalIndexes()
        throws Exception
    {
        // day 1
        makeCentralPointTo( "http://localhost:12345/central-inc1-v1/" );

        indexerManager.reindexRepository( null, "central" );

        // validation
        validateIndexWithIdentify( true, "cf4f67dae5df4f9932ae7810f4548ef3e14dd35e", "antlr", "antlr", "2.7.6" );
        validateIndexWithIdentify( false, "83cd2cd674a217ade95a4bb83a8a14f351f48bd0", "antlr", "antlr", "2.7.7" );

        validateIndexWithIdentify( true, "3640dd71069d7986c9a14d333519216f4ca5c094", "log4j", "log4j", "1.2.8" );
        validateIndexWithIdentify( false, "057b8740427ee6d7b0b60792751356cad17dc0d9", "log4j", "log4j", "1.2.12" );
        validateIndexWithIdentify( false, "f0a0d2e29ed910808c33135a3a5a51bba6358f7b", "log4j", "log4j", "1.2.15" );

        // day 2
        makeCentralPointTo( "http://localhost:12345/central-inc2-v1/" );

        indexerManager.reindexRepository( null, "central" );

        // validation
        validateIndexWithIdentify( true, "cf4f67dae5df4f9932ae7810f4548ef3e14dd35e", "antlr", "antlr", "2.7.6" );
        validateIndexWithIdentify( true, "83cd2cd674a217ade95a4bb83a8a14f351f48bd0", "antlr", "antlr", "2.7.7" );

        validateIndexWithIdentify( true, "3640dd71069d7986c9a14d333519216f4ca5c094", "log4j", "log4j", "1.2.8" );
        validateIndexWithIdentify( true, "057b8740427ee6d7b0b60792751356cad17dc0d9", "log4j", "log4j", "1.2.12" );
        validateIndexWithIdentify( false, "f0a0d2e29ed910808c33135a3a5a51bba6358f7b", "log4j", "log4j", "1.2.15" );

        // day 3
        makeCentralPointTo( "http://localhost:12345/central-inc3-v1/" );

        indexerManager.reindexRepository( null, "central" );

        // validation
        validateIndexWithIdentify( true, "cf4f67dae5df4f9932ae7810f4548ef3e14dd35e", "antlr", "antlr", "2.7.6" );
        validateIndexWithIdentify( true, "83cd2cd674a217ade95a4bb83a8a14f351f48bd0", "antlr", "antlr", "2.7.7" );

        validateIndexWithIdentify( true, "3640dd71069d7986c9a14d333519216f4ca5c094", "log4j", "log4j", "1.2.8" );
        validateIndexWithIdentify( true, "057b8740427ee6d7b0b60792751356cad17dc0d9", "log4j", "log4j", "1.2.12" );
        validateIndexWithIdentify( true, "f0a0d2e29ed910808c33135a3a5a51bba6358f7b", "log4j", "log4j", "1.2.15" );
    }

}
