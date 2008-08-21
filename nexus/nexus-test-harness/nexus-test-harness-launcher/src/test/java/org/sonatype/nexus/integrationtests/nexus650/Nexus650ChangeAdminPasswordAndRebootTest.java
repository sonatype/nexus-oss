package org.sonatype.nexus.integrationtests.nexus650;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.integrationtests.TestContext;
import org.sonatype.nexus.test.utils.ChangePasswordUtils;
import org.sonatype.nexus.test.utils.NexusStateUtil;

public class Nexus650ChangeAdminPasswordAndRebootTest
    extends AbstractPrivilegeTest
{

    @Test
    public void doTest() throws Exception
    {   
        TestContext context = TestContainer.getInstance().getTestContext();
        String newPassword = "123admin";
        Status status = ChangePasswordUtils.changePassword( context.getAdminUsername(), context.getAdminPassword(), newPassword );
        Assert.assertTrue( "Status: ", status.isSuccess() );
         
        // now change the admin password
        context.setAdminPassword( newPassword );
        
        // reboot
        NexusStateUtil.doSoftRestart();
        
        // now we can verify everything worked out
        Assert.assertTrue( "Nexus is not running", NexusStateUtil.isNexusRunning() );
        
    }
    
}
