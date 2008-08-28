package org.sonatype.nexus.integrationtests.nexus598;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.SearchMessageUtil;

public class Nexus598ClassnameSearchTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void searchDeployedArtifact()
        throws Exception
    {
        //FIXME remove this when NEXUS-665 is fixed
        RepositoryMessageUtil.updateIndexes( REPO_TEST_HARNESS_REPO );

        List<NexusArtifact> artifacts =
            SearchMessageUtil.searchClassname( "org.sonatype.nexus.test.classnamesearch.ClassnameSearchTestHelper" );
        Assert.assertFalse( "Nexus598 artifact was not found", artifacts.isEmpty() );
    }

    @Test
    public void unqualifiedSearchDeployedArtifact()
        throws Exception
    {
        //FIXME remove this when NEXUS-665 is fixed
        RepositoryMessageUtil.updateIndexes( REPO_TEST_HARNESS_REPO );

        List<NexusArtifact> artifacts = SearchMessageUtil.searchClassname( "ClassnameSearchTestHelper" );
        Assert.assertFalse( "Nexus598 artifact was not found", artifacts.isEmpty() );
    }

    @Test
    public void searchUnexistentClass()
        throws Exception
    {
        List<NexusArtifact> artifacts =
            SearchMessageUtil.searchClassname( "I.hope.this.class.name.is.not.available.at.nexus.repo.for.test.issue.Nexus598" );
        Assert.assertTrue( "The search found something, but it shouldn't.", artifacts.isEmpty() );
    }

}
