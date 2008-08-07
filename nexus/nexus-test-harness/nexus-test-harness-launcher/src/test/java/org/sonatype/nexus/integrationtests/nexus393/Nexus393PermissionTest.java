package org.sonatype.nexus.integrationtests.nexus393;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;

public class Nexus393PermissionTest
    extends AbstractPrivilegeTest
{

    static {
        printKnownErrorButDoNotFail( Nexus393PermissionTest.class, "resetWithoutPermission" );
    }

    @Test
    public void resetWithPermission()
        throws Exception
    {
        overwriteUserRole( "test-user", "anonymous-with-login-reset", "1", "2" /* login */, "6", "14", "17", "19",
                           "44", "54", "55", "56", "57", "58", "59"/* reset */, "T1", "T2" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // Should be able to reset anyone password
        String username = "anonymous";
        Status status = ResetPasswordUtils.resetPassword( username );
        Assert.assertTrue( status.isSuccess() );

        // Should be able to reset my own password
        username = "test-user";
        status = ResetPasswordUtils.resetPassword( username );
        Assert.assertTrue( status.isSuccess() );

    }

    @Test
    public void resetWithoutPermission()
        throws Exception
    {
        overwriteUserRole( "test-user", "anonymous-with-login", "1", "2" /* login */, "6", "14", "17", "19", "44",
                           "54", "55", "56", "57", "58", /* "59" reset , */"T1", "T2" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // NOT Shouldn't be able to reset anyone password
        String username = "anonymous";
        Status status = ResetPasswordUtils.resetPassword( username );
        Assert.assertEquals( 401, status.getCode() );

        // NOT Should be able to reset my own password
/*  // TODO issue nexus-469
        username = "test-user";
        status = ResetPasswordUtils.resetPassword( username );
        Assert.assertEquals( 401, status.getCode() );
*/
    }
}
