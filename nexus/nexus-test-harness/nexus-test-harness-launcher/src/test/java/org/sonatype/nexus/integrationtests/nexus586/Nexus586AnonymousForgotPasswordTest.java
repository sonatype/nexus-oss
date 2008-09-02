package org.sonatype.nexus.integrationtests.nexus586;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.nexus394.ForgotPasswordUtils;

/**
 * Saving the Nexus config needs to validate the anonymous user information 
 */
public class Nexus586AnonymousForgotPasswordTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void forgotPassword()
        throws Exception
    {
        String username = "anonymous";
        Response response = ForgotPasswordUtils.recoverUserPassword( username, "changeme2@yourcompany.com" );
        Assert.assertEquals( 400, response.getStatus().getCode() );
    }
}
