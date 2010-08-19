package org.sonatype.nexus.integrationtests.nexus1560;

import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus1560LegacyAllowGroupRulesIT
    extends AbstractLegacyRulesIT
{

    @BeforeMethod
    public void init()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();
        addPriv( TEST_USER_NAME, NEXUS1560_GROUP + "-priv", TargetPrivilegeDescriptor.TYPE, "1", null, NEXUS1560_GROUP, "read" );
    }

    @Test
    public void fromGroup()
        throws Exception
    {   
        // the user also needs the view priv
        addPrivilege( TEST_USER_NAME, "repository-"+ NEXUS1560_GROUP );
        
        String downloadUrl = GROUP_REPOSITORY_RELATIVE_URL + NEXUS1560_GROUP + "/" + getRelitiveArtifactPath( gavArtifact1 );

        successDownload( downloadUrl );
    }

    @Test
    public void fromRepository()
        throws Exception
    {
     // the user also needs the view priv
        addPrivilege( TEST_USER_NAME, "repository-"+ REPO_TEST_HARNESS_REPO );
        
        String downloadUrl =
            REPOSITORY_RELATIVE_URL + REPO_TEST_HARNESS_REPO + "/" + getRelitiveArtifactPath( gavArtifact1 );

        successDownload( downloadUrl );
    }

}
