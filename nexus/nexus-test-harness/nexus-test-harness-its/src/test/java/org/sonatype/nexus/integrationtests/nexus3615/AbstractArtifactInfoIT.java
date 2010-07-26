package org.sonatype.nexus.integrationtests.nexus3615;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.RepositoryUrlResource;

public abstract class AbstractArtifactInfoIT
    extends AbstractNexusIntegrationTest
{

    public AbstractArtifactInfoIT()
    {
        super();
    }

    public AbstractArtifactInfoIT( String testRepositoryId )
    {
        super( testRepositoryId );
    }

    @Override
    protected void deployArtifacts()
        throws Exception
    {
        super.deployArtifacts();
    
        File pom = getTestFile( "artifact.pom" );
        File jar = getTestFile( "artifact.jar" );
        getDeployUtils().deployUsingPomWithRest( REPO_TEST_HARNESS_REPO, jar, pom, null, null );
        getDeployUtils().deployUsingPomWithRest( REPO_TEST_HARNESS_REPO2, jar, pom, null, null );
        getDeployUtils().deployUsingPomWithRest( REPO_TEST_HARNESS_RELEASE_REPO, jar, pom, null, null );
    }

    protected Iterable<String> getRepositoryId( List<RepositoryUrlResource> repositories )
    {
        List<String> repoIds = new ArrayList<String>();
        for ( RepositoryUrlResource repositoryUrlResource : repositories )
        {
            repoIds.add( repositoryUrlResource.getRepositoryId() );
        }
    
        return repoIds;
    }

}