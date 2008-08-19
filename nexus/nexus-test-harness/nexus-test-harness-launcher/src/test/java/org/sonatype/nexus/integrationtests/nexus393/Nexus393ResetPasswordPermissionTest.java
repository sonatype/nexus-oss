package org.sonatype.nexus.integrationtests.nexus393;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;

public class Nexus393ResetPasswordPermissionTest
    extends AbstractPrivilegeTest
{

    @Test
    public void resetWithPermission()
        throws Exception
    {
        overwriteUserRole( TEST_USER_NAME, "anonymous-with-login-reset", "1", "2" /* login */, "6", "14", "17", "19",
                           "44", "54", "55", "56", "57", "58", "59"/* reset */, "T1", "T2" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // Should be able to reset anyone password
        String username = "another-user";
        Response response = ResetPasswordUtils.resetPassword( username );
        Assert.assertTrue( "Status: "+ response.getStatus(), response.getStatus().isSuccess() );

        // Should be able to reset my own password
        username = TEST_USER_NAME;
        response = ResetPasswordUtils.resetPassword( username );
        Assert.assertTrue( "Status: "+ response.getStatus(), response.getStatus().isSuccess() );

    }

    @Test
    public void resetWithoutPermission()
        throws Exception
    {
        overwriteUserRole( TEST_USER_NAME, "anonymous-with-login-but-reset", "1", "2" /* login */, "6", "14", "17",
                           "19", "44", "54", "55", "56", "57", "58", /* "59" reset , */"T1", "T2" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // NOT Shouldn't be able to reset anyone password
        String username = "another-user";
        Response response = ResetPasswordUtils.resetPassword( username );
        Assert.assertEquals("Status: "+ response.getStatus() +"\n"+ response.getEntity().getText(), 401, response.getStatus().getCode() );

        // NOT Should be able to reset my own password
        username = TEST_USER_NAME;
        response = ResetPasswordUtils.resetPassword( username );
        Assert.assertEquals( "Status: "+ response.getStatus() +"\n"+ response.getEntity().getText(), 401, response.getStatus().getCode() );

    }
}
