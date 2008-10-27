package org.sonatype.nexus.integrationtests.nexus650;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.test.utils.ChangePasswordUtils;
import org.sonatype.nexus.test.utils.NexusStateUtil;

/**
 * Changes users password, restarts nexus, and verify password is correct.
 */
public class Nexus650ChangePasswordAndRebootTest
    extends AbstractPrivilegeTest
{

    @Test
    public void changePasswordAndReboot() throws Exception
    {
        this.giveUserRole( TEST_USER_NAME, "admin" );

        TestContext context = TestContainer.getInstance().getTestContext();

        context.setUsername( TEST_USER_NAME );
        context.setPassword( TEST_USER_PASSWORD );


        String newPassword = "123password";
        Status status = ChangePasswordUtils.changePassword( TEST_USER_NAME, TEST_USER_PASSWORD, newPassword );
        Assert.assertTrue( "Status: ", status.isSuccess() );

        // now change the password
        context.setPassword( newPassword );

        // reboot
        NexusStateUtil.doSoftRestart();

        // now we can verify everything worked out
        Assert.assertTrue( "Nexus is not running", NexusStateUtil.isNexusRunning() );

    }

}
