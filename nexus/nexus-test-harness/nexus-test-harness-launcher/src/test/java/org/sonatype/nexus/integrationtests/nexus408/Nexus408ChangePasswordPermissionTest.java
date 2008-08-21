package org.sonatype.nexus.integrationtests.nexus408;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.ChangePasswordUtils;

public class Nexus408ChangePasswordPermissionTest
    extends AbstractPrivilegeTest
{

    @Test
    public void withPermission()
        throws Exception
    {
        overwriteUserRole( TEST_USER_NAME, "anonymous-with-login-changepw", "1", "2" /* login */, "6", "14", "17",
                           "19", "44", "54", "55", "56", "57", "58", "59", "64"/* change pw */, "T1", "T2" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // Should be able to change my own password
        Status status = ChangePasswordUtils.changePassword( "test-user", "admin123", "123admin" );
        Assert.assertTrue( status.isSuccess() );

        // NOT Should be able to change my own password
        status = ChangePasswordUtils.changePassword( "admin", "admin123", "123admin" );
        Assert.assertEquals( 401, status.getCode() );

    }

    @Test
    public void withoutPermission()
        throws Exception
    {
        overwriteUserRole( TEST_USER_NAME, "anonymous-with-login-but-changepw", "1", "2" /* login */, "6", "14",
                           "17", "19", "44", "54", "55", "56", "57", "58", "59", /* "64" change pw, */"T1", "T2" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // NOT Should be able to forgot my own username
        Status status = ChangePasswordUtils.changePassword( "test-user", "admin123", "123admin" );
        Assert.assertEquals( 401, status.getCode() );

        // NOT Should be able to forgot anyone username
        status = ChangePasswordUtils.changePassword( "admin", "admin123", "123admin" );
        Assert.assertEquals( 401, status.getCode() );
    }

}
