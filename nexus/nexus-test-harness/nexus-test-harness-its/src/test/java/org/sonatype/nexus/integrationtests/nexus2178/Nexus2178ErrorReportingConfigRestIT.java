package org.sonatype.nexus.integrationtests.nexus2178;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.ErrorReportingSettings;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus2178ErrorReportingConfigRestIT
    extends AbstractNexusIntegrationTest
{
    @Test
    public void validationConfiguration()
        throws Exception
    {
        // Default config
        GlobalConfigurationResource resource = SettingsMessageUtil.getCurrentSettings();

        Assert.assertFalse( resource.getErrorReportingSettings().isReportErrorsAutomatically(),
                            "Error reporting should be null by default" );

        // Set some values
        ErrorReportingSettings settings = resource.getErrorReportingSettings();
        settings.setJiraUsername( "someusername" );
        settings.setJiraPassword( "somepassword" );
        settings.setReportErrorsAutomatically( true );

        SettingsMessageUtil.save( resource );

        resource = SettingsMessageUtil.getCurrentSettings();

        Assert.assertNotNull( resource.getErrorReportingSettings(), "Error reporting should not be null" );
        Assert.assertEquals( "someusername", resource.getErrorReportingSettings().getJiraUsername() );
        Assert.assertEquals( AbstractNexusPlexusResource.PASSWORD_PLACE_HOLDER,
                             resource.getErrorReportingSettings().getJiraPassword() );

        // Clear them again
        resource.setErrorReportingSettings( null );

        Assert.assertTrue( SettingsMessageUtil.save( resource ).isSuccess() );

        resource = SettingsMessageUtil.getCurrentSettings();

        Assert.assertFalse( resource.getErrorReportingSettings().isReportErrorsAutomatically(),
                            "Error reporting should be null" );
    }
}
