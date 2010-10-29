package org.sonatype.nexus.index;

import java.io.File;
import java.net.URL;

import org.sonatype.nexus.proxy.repository.GroupRepository;

public class GroupReindexIndexerManagerTest
    extends AbstractIndexerManagerTest
{

    public void testGroupReindex()
        throws Exception
    {
        fillInRepo();

        GroupRepository group = (GroupRepository) repositoryRegistry.getRepository( "public" );

        File groupRoot = new File( new URL( group.getLocalUrl() ).toURI() );
        File index = new File( groupRoot, ".index" );

        File indexFile = new File( index, "nexus-maven-repository-index.gz" );
        File incrementalIndexFile = new File( index, "nexus-maven-repository-index.1.gz" );

        assertFalse( "No index .gz file should exist.", indexFile.exists() );
        assertFalse( "No incremental chunk should exists.", incrementalIndexFile.exists() );

        indexerManager.reindexRepositoryGroup( null, group.getId(), true );

        assertTrue( "Index .gz file should exist.", indexFile.exists() );
        assertFalse( "No incremental chunk should exists.", incrementalIndexFile.exists() );

        // copy some _new_ stuff, not found in any of the members
        File sourceApacheSnapshotsRoot = new File( getBasedir(), "src/test/resources/reposes/apache-snapshots-2" );
        File snapshotsRoot = new File( new URL( snapshots.getLocalUrl() ).toURI() );
        copyDirectory( sourceApacheSnapshotsRoot, snapshotsRoot );
        indexerManager.reindexRepositoryGroup( null, group.getId(), false );

        assertTrue( "Index .gz file should exist.", indexFile.exists() );
        assertTrue( "Incremental chunk should exists.", incrementalIndexFile.exists() );

        assertTrue(incrementalIndexFile.length() < 300 );

    }
}
