package org.sonatype.nexus.task;

import org.sonatype.nexus.AbstractMavenRepoContentTests;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.scheduling.NexusScheduler;

public class ExpireCacheTaskTest
    extends AbstractMavenRepoContentTests
{
    NexusScheduler scheduler;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        nexusConfiguration.setSecurityEnabled( false );

        nexusConfiguration.saveConfiguration();

        scheduler = lookup( NexusScheduler.class );
    }

    public void testBlockRepoInAGroup()
        // NEXUS-3798
        throws Exception
    {
        fillInRepo();

        while ( scheduler.getActiveTasks().size() > 0 )
        {
            Thread.sleep( 100 );
        }

        central.setLocalStatus( LocalStatus.OUT_OF_SERVICE );
        nexusConfiguration.saveConfiguration();

        GroupRepository group = repositoryRegistry.getRepositoryWithFacet( "public", GroupRepository.class );
        group.expireCaches( new ResourceStoreRequest( "/" ) );
    }
}
