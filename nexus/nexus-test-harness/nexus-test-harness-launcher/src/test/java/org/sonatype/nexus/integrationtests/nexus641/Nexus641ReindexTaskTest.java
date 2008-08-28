package org.sonatype.nexus.integrationtests.nexus641;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.SearchMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

/**
 * @author marvin
 * @description Test task Reindex Repositories
 */
public class Nexus641ReindexTaskTest
    extends AbstractNexusIntegrationTest
{

    private SearchMessageUtil messageUtil = new SearchMessageUtil();

    @Test
    public void testReindex()
        throws Exception
    {
        // NEXUS-664
        if ( true )
        {
            printKnownErrorButDoNotFail( getClass(), "testReindex" );
            return;
        }

        File repositoryPath = new File( nexusBaseDir, "runtime/work/storage/nexus-test-harness-repo" );
        File oldSnapshot = getTestFile( "repo" );

        // Copy artifact to avoid indexing
        FileUtils.copyDirectory( oldSnapshot, repositoryPath );

        // try to seach and fail
        List<NexusArtifact> search = messageUtil.searchFor( "nexus641" );
        Assert.assertEquals( "The artifact was already indexed", 1, search.size() );

        ScheduledServicePropertyResource prop = new ScheduledServicePropertyResource();
        prop.setId( "repositoryOrGroupId" );
        prop.setValue( "nexus-test-harness-repo" );

        // reindex
        TaskScheduleUtil.runTask( "org.sonatype.nexus.tasks.ReindexTask", prop );
        // RepositoryMessageUtil.updateIndexes( "nexus-test-harness-repo" );

        // try to download again and success
        search = messageUtil.searchFor( "nexus641" );
        Assert.assertEquals( "The artifact should be indexed", 2, search.size() );
    }

}
