package org.sonatype.nexus.index;

import org.sonatype.nexus.proxy.repository.GroupRepository;

public class GroupUpdateIndexerManagerTest
    extends AbstractIndexerManagerTest
{

    public void testGroupUpdate()
        throws Exception
    {
        fillInRepo();

        indexerManager.reindexAllRepositories( null, true );
        
        searchFor( "org.sonatype.plexus", 1, "public" );
        searchFor( "org.sonatype.test-evict", 1, "apache-snapshots" );
        searchFor( "org.sonatype.test-evict", 0, "public" );

        GroupRepository group = (GroupRepository) repositoryRegistry.getRepository( "public" );

        group.removeMemberRepositoryId( snapshots.getId() );
        super.nexusConfiguration.saveConfiguration();
        waitForTasksToStop();

        group = (GroupRepository) repositoryRegistry.getRepository( "public" );
        assertFalse( group.getMemberRepositoryIds().contains( snapshots.getId() ) );

        searchFor( "org.sonatype.plexus", 0, "public" );

        group.addMemberRepositoryId( apacheSnapshots.getId() );
        super.nexusConfiguration.saveConfiguration();
        waitForTasksToStop();

        searchFor( "org.sonatype.test-evict", 1, "public" );
    }
}
