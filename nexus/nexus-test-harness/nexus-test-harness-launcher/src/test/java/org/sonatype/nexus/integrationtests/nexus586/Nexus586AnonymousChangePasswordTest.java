package org.sonatype.nexus.integrationtests.nexus586;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.nexus408.ChangePasswordUtils;

public class Nexus586AnonymousChangePasswordTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void changePassword()
        throws Exception
    {
        String username = "anonymous";
        Status status = ChangePasswordUtils.changePassword( username, "anonymous", "anonymous@123" );
        Assert.assertEquals( 400, status.getCode() );
    }
}
