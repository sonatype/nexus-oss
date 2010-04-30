package org.sonatype.nexus.integrationtests.nexus1954;

import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus1954DeletedArtifactsIncrementalIndexIT
    extends AbstractDeleteArtifactsIT
{

    @Override
    protected void runUpdateIndex()
        throws Exception
    {
        RepositoryMessageUtil.updateIncrementalIndexes( REPO_TEST_HARNESS_REPO );
        RepositoryMessageUtil.updateIncrementalIndexes( REPO_TEST_HARNESS_PROXY );

        TaskScheduleUtil.waitForAllTasksToStop();
    }
}
