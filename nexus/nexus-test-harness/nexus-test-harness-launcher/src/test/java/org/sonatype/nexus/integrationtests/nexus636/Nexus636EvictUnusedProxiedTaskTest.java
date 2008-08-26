package org.sonatype.nexus.integrationtests.nexus636;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.nexus533.TaskScheduleUtil;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public class Nexus636EvictUnusedProxiedTaskTest
    extends AbstractNexusIntegrationTest
{

    public static final String PROXY_REPO = "central";

    private File repositoryPath;

//    private File artifactFolder;

    @Before
    public void deployOldArtifacts()
        throws Exception
    {
        repositoryPath = new File( nexusBaseDir, "runtime/work/storage/" + PROXY_REPO );
    }

    @Test
    public void evictUnusedProxiedItems()
        throws Exception
    {
        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setId( "repositoryOrGroupId" );
        repo.setValue( PROXY_REPO );
        ScheduledServicePropertyResource age = new ScheduledServicePropertyResource();
        age.setId( "evictOlderCacheItemsThen" );
        age.setValue( String.valueOf( 0 ) );

        // clean unused
        TaskScheduleUtil.runTask( "org.sonatype.nexus.tasks.EvictUnusedProxiedItemsTask", repo, age );

    }

}
