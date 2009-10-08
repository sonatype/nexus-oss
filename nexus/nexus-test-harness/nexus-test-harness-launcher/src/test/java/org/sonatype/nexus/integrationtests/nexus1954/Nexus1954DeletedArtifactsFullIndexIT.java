package org.sonatype.nexus.integrationtests.nexus1954;

import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public class Nexus1954DeletedArtifactsFullIndexIT
    extends AbstractDeleteArtifactsIT
{

    @Override
    protected void runUpdateIndex()
        throws Exception
    {
        RepositoryMessageUtil.updateIndexes( REPO_TEST_HARNESS_REPO );
        RepositoryMessageUtil.updateIndexes( REPO_TEST_HARNESS_PROXY );
    }

}
