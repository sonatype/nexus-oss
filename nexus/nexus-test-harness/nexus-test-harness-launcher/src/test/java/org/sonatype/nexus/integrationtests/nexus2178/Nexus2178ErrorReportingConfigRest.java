package org.sonatype.nexus.integrationtests.nexus2178;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ErrorReportingSettings;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;

public class Nexus2178ErrorReportingConfigRest
    extends AbstractNexusIntegrationTest
{
    @Test
    public void validationConfiguration()
        throws Exception
    {
        // Default config
        GlobalConfigurationResource resource = SettingsMessageUtil.getCurrentSettings();
        
        Assert.assertNull( "Error reporting should be null by default", resource.getErrorReportingSettings() );
        
        // Set some values
        ErrorReportingSettings settings = new ErrorReportingSettings();
        settings.setJiraUrl( "http://someurl" );
        settings.setJiraUsername( "someusername" );
        settings.setJiraPassword( "somepassword" );
        
        resource.setErrorReportingSettings( settings );
        
        SettingsMessageUtil.save( resource );
        
        resource = SettingsMessageUtil.getCurrentSettings();
        
        Assert.assertNotNull( "Error reporting should not be null", resource.getErrorReportingSettings() );
        Assert.assertEquals( "http://someurl", resource.getErrorReportingSettings().getJiraUrl() );
        Assert.assertEquals( "someusername", resource.getErrorReportingSettings().getJiraUsername() );
        Assert.assertEquals( "somepassword", resource.getErrorReportingSettings().getJiraPassword() );
        
        // Clear them again
        resource.setErrorReportingSettings( null );
        
        SettingsMessageUtil.save( resource );
        
        resource = SettingsMessageUtil.getCurrentSettings();
        
        Assert.assertNull( "Error reporting should be null", resource.getErrorReportingSettings() );
    }
}
