package org.sonatype.nexus.index;

import java.util.Collection;

import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.index.context.IndexingContext;

public class DefaultIndexerManagerTest
    extends AbstractMavenRepoContentTests
{
    private IndexerManager indexerManager;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        indexerManager = (IndexerManager) lookup( IndexerManager.class );
    }

    protected void tearDown()
        throws Exception
    {
        indexerManager.shutdown( false );

        super.tearDown();
    }

    public void testRepoReindex()
        throws Exception
    {
        fillInRepo();

        defaultNexus.reindexAllRepositories( null );

        Thread.sleep( 5000 );

        // will use the group index context, that is from now automatically merged
        IndexingContext ctx = indexerManager.getRepositoryGroupContext( "public" );

        Collection<ArtifactInfo> result = indexerManager.getNexusIndexer().searchFlat(
            indexerManager.getNexusIndexer().constructQuery( ArtifactInfo.GROUP_ID, "org.sonatype.nexus" ) );

        // expected result set
        // org.sonatype.nexus:nexus-indexer:1.0-beta-5-SNAPSHOT
        // org.sonatype.nexus:nexus-indexer:1.0-beta-4
        // org.sonatype.nexus:nexus-indexer:1.0-beta-4-SNAPSHOT
        // org.sonatype.nexus:nexus-indexer:1.0-beta-4-SNAPSHOT :: cli
        // org.sonatype.nexus:nexus-indexer:1.0-beta-4-SNAPSHOT :: jdk14
        assertEquals( 5, result.size() );
    }
}
