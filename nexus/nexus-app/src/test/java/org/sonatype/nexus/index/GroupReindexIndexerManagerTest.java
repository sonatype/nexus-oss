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

        assertFalse( indexFile.exists() );
        assertFalse( incrementalIndexFile.exists() );

        group.setIndexable( true );
        nexusConfiguration.saveConfiguration();
        
        indexerManager.reindexRepositoryGroup( null, group.getId(), true );

        assertTrue( indexFile.exists() );
        assertFalse( incrementalIndexFile.exists() );

        File sourceApacheSnapshotsRoot = new File( getBasedir(), "src/test/resources/reposes/apache-snapshots" );
        File snapshotsRoot = new File( new URL( snapshots.getLocalUrl() ).toURI() );
        copyDirectory( sourceApacheSnapshotsRoot, snapshotsRoot );
        indexerManager.reindexRepositoryGroup( null, group.getId(), true );

        assertTrue( indexFile.exists() );
        assertTrue( incrementalIndexFile.exists() );

        assertTrue(incrementalIndexFile.length() < 300 );

    }
}
