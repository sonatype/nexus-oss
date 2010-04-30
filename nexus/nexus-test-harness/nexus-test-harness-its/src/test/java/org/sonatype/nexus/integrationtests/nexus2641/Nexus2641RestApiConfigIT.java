package org.sonatype.nexus.integrationtests.nexus2641;

import junit.framework.Assert;

import org.codehaus.plexus.util.StringUtils;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.rest.model.RestApiSettings;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;

public class Nexus2641RestApiConfigIT
    extends AbstractNexusIntegrationTest
{
    @Test
    public void checkRestApiConfig()
        throws Exception
    {
        GlobalConfigurationResource settings = SettingsMessageUtil.getCurrentSettings();

        Assert.assertNotNull( settings.getGlobalRestApiSettings() );

        // enable it, not that even the baseUrl is not set, it will be filled with a defaut one
        RestApiSettings restApiSettings = new RestApiSettings();
        settings.setGlobalRestApiSettings( restApiSettings );
        SettingsMessageUtil.save( settings );
        settings = SettingsMessageUtil.getCurrentSettings();

        Assert.assertNotNull( settings.getGlobalRestApiSettings() );
        Assert.assertTrue( StringUtils.isNotEmpty( settings.getGlobalRestApiSettings().getBaseUrl() ) );
        Assert.assertEquals( false, settings.getGlobalRestApiSettings().isForceBaseUrl() );

        // now edit it
        restApiSettings.setBaseUrl( "http://myhost/nexus" );
        restApiSettings.setForceBaseUrl( true );
        settings.setGlobalRestApiSettings( restApiSettings );
        SettingsMessageUtil.save( settings );
        settings = SettingsMessageUtil.getCurrentSettings();

        Assert.assertNotNull( settings.getGlobalRestApiSettings() );
        Assert.assertEquals( "http://myhost/nexus", settings.getGlobalRestApiSettings().getBaseUrl() );
        Assert.assertEquals( true, settings.getGlobalRestApiSettings().isForceBaseUrl() );

        //now unset it
        settings.setGlobalRestApiSettings( null );
        SettingsMessageUtil.save( settings );
        
        Assert.assertNull( settings.getGlobalRestApiSettings() );
    }
}
