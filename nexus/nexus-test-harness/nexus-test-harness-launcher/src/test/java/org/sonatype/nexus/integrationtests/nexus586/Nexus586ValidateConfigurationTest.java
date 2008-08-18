package org.sonatype.nexus.integrationtests.nexus586;

import junit.framework.Assert;

import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;

public class Nexus586ValidateConfigurationTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void wrongAnonymousAccount()
        throws Exception
    {
        GlobalConfigurationResource globalConfig = SettingsMessageUtil.getCurrentSettings();
        globalConfig.setSecurityAnonymousUsername( "zigfrid" );

        Status status = SettingsMessageUtil.save( globalConfig );
        Assert.assertEquals( 400, status.getCode() );
    }

    @Test
    public void wrongAnonymousPassword()
        throws Exception
    {
        GlobalConfigurationResource globalConfig = SettingsMessageUtil.getCurrentSettings();
        globalConfig.setSecurityAnonymousPassword( "zigfrid" );

        Status status = SettingsMessageUtil.save( globalConfig );
        Assert.assertEquals( 400, status.getCode() );
    }

}
