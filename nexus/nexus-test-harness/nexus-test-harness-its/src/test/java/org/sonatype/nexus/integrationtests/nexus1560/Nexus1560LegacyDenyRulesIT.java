package org.sonatype.nexus.integrationtests.nexus1560;

import org.sonatype.nexus.integrationtests.TestContainer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class Nexus1560LegacyDenyRulesIT
    extends AbstractLegacyRulesIT
{
	
    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void fromRepository()
        throws Exception
    {
        String downloadUrl =
            REPOSITORY_RELATIVE_URL + REPO_TEST_HARNESS_REPO + "/" + getRelitiveArtifactPath( gavArtifact1 );

        failDownload( downloadUrl );
    }

    @Test
    public void fromGroup()
        throws Exception
    {
        String downloadUrl =
            GROUP_REPOSITORY_RELATIVE_URL + NEXUS1560_GROUP + "/" + getRelitiveArtifactPath( gavArtifact1 );

        failDownload( downloadUrl );
    }

    @Test
    public void checkMetadata()
        throws Exception
    {
        String downloadUrl = GROUP_REPOSITORY_RELATIVE_URL + NEXUS1560_GROUP + "/nexus1560/artifact/maven-metadata.xml";

        failDownload( downloadUrl );
    }

    @Test
    public void artifact2FromGroup()
        throws Exception
    {
        String downloadUrl =
            GROUP_REPOSITORY_RELATIVE_URL + NEXUS1560_GROUP + "/" + getRelitiveArtifactPath( gavArtifact2 );

        failDownload( downloadUrl );
    }

    @Test
    public void artifact2FromRepo()
        throws Exception
    {
        String downloadUrl =
            REPOSITORY_RELATIVE_URL + REPO_TEST_HARNESS_REPO + "/" + getRelitiveArtifactPath( gavArtifact2 );

        failDownload( downloadUrl );
    }

    @Test
    public void artifact2FromRepo2()
        throws Exception
    {
        String downloadUrl =
            REPOSITORY_RELATIVE_URL + REPO_TEST_HARNESS_REPO2 + "/" + getRelitiveArtifactPath( gavArtifact2 );

        failDownload( downloadUrl );
    }

}
