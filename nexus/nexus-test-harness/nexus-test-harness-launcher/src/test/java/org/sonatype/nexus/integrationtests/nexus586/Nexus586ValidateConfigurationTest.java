package org.sonatype.nexus.integrationtests.nexus586;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;


/**
 * Saving the Nexus config needs to validate the anonymous user information 
 */
public class Nexus586ValidateConfigurationTest
    extends AbstractNexusIntegrationTest
{

    static
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void wrongAnonymousAccount()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        GlobalConfigurationResource globalConfig = SettingsMessageUtil.getCurrentSettings();
        globalConfig.setSecurityAnonymousUsername( "zigfrid" );

        Status status = SettingsMessageUtil.save( globalConfig );
        Assert.assertEquals( "Can't set an invalid user as anonymous", 400, status.getCode() );
    }

}
