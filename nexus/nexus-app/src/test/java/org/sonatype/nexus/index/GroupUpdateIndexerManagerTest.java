package org.sonatype.nexus.index;

import org.sonatype.nexus.proxy.repository.GroupRepository;

public class GroupUpdateIndexerManagerTest
    extends AbstractIndexerManagerTest
{

    public void testGroupUpdate()
        throws Exception
    {
        fillInRepo();

        GroupRepository group = (GroupRepository) repositoryRegistry.getRepository( "public" );

        indexerManager.reindexAllRepositories( null, true );

        searchFor( "org.sonatype.plexus", 1, "public" );
        searchFor( "org.sonatype.test-evict", 1, "apache-snapshots" );
        searchFor( "org.sonatype.test-evict", 0, "public" );

        group.removeMemberRepositoryId( snapshots.getId() );
        super.nexusConfiguration.saveConfiguration();
        Thread.sleep( 10000 );

        group = (GroupRepository) repositoryRegistry.getRepository( "public" );
        assertFalse( group.getMemberRepositoryIds().contains( snapshots.getId() ) );

        searchFor( "org.sonatype.plexus", 0, "public" );

        group.addMemberRepositoryId( apacheSnapshots.getId() );
        super.nexusConfiguration.saveConfiguration();
        Thread.sleep( 10000 );

        searchFor( "org.sonatype.test-evict", 1, "public" );
    }
}
