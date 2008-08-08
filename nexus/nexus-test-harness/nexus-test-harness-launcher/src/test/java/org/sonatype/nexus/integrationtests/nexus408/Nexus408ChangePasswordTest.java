package org.sonatype.nexus.integrationtests.nexus408;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

public class Nexus408ChangePasswordTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void changeUserPassword()
        throws Exception
    {
        Status status = ChangePasswordUtils.changePassword( "test-user", "admin123", "123admin" );
        Assert.assertEquals( Status.SUCCESS_OK.getCode(), status.getCode() );
    }

}
