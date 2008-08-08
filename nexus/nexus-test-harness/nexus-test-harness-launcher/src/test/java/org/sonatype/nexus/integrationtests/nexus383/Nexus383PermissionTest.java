package org.sonatype.nexus.integrationtests.nexus383;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.NexusArtifact;

public class Nexus383PermissionTest
    extends AbstractPrivilegeTest
{
    protected SearchMessageUtil messageUtil;

    static
    {
        printKnownErrorButDoNotFail( Nexus383PermissionTest.class, "withoutRepositoryReadPermission" );
    }

    public Nexus383PermissionTest()
    {
        this.messageUtil = new SearchMessageUtil();
    }

    @Test
    public void withPermission()
        throws Exception
    {
        overwriteUserRole( TEST_USER_NAME, "anonymous-with-login-search", "1", "2" /* login */, "6", "14",
                           "17" /* search */, "19", "44", "54", "55", "56", "57", "58", "59", "T1", "T2" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // Should be able to find artifacts
        List<NexusArtifact> results = messageUtil.searchFor( "nexus383" );
        Assert.assertEquals( 2, results.size() );
    }

    @Test
    public void withoutSearchPermission()
        throws Exception
    {
        overwriteUserRole( TEST_USER_NAME, "anonymous-with-login-but-search", "1", "2" /* login */, "6", "14", "19",
        /* "17" search, */"44", "54", "55", "56", "57", "58", "59", "T1", "T2" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // NOT Should be able to find artifacts
        Status status = messageUtil.doSearchFor( "nexus383" ).getStatus();
        Assert.assertEquals( 401, status.getCode() );

    }

    // @Test
    // public void withoutRepositoryReadPermission()
    // throws Exception
    // {
    // overwriteUserRole( TEST_USER_NAME, "anonymous-with-login-but-repo", "1", "2" /* login */, "6", "14", "19",
    // "17", "44", "54", "55", "56", "57", "58", "59"/* , "T1", "T2" */);
    //
    // TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
    // TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );
    //
    // // Should found nothing
    // List<NexusArtifact> results = messageUtil.searchFor( "nexus383" );
    // Assert.assertEquals( 0, results.size() );
    //
    // }
}
