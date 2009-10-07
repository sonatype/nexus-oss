package org.sonatype.nexus.integrationtests.nexus2213;

import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Method;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.ErrorReportingSettings;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.ErrorReportUtil;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;

public class Nexus2213ErrorReportBundleTest
    extends AbstractNexusIntegrationTest
{
    @Before
    public void cleanDirs()
        throws Exception
    {
        ErrorReportUtil.cleanErrorBundleDir( nexusWorkDir );
    }
    
    @Test
    public void validateBundleCreated()
        throws Exception
    {
        // Default config
        GlobalConfigurationResource resource = SettingsMessageUtil.getCurrentSettings();
        
        // Set some values
        ErrorReportingSettings settings = new ErrorReportingSettings();
        settings.setJiraUsername( "someusername" );
        settings.setJiraPassword( "somepassword" );
        
        resource.setErrorReportingSettings( settings );
        
        SettingsMessageUtil.save( resource );
        
        RequestFacade.sendMessage( "service/local/exception?status=500", Method.GET, null );
        
        ErrorReportUtil.validateZipContents( nexusWorkDir );
    }
    
    @Test
    public void validateBundleNotCreated()
        throws Exception
    {
        // Default config
        GlobalConfigurationResource resource = SettingsMessageUtil.getCurrentSettings();
        
        // Set some values
        ErrorReportingSettings settings = new ErrorReportingSettings();
        settings.setJiraUsername( "someusername" );
        settings.setJiraPassword( "somepassword" );
        
        resource.setErrorReportingSettings( settings );
        
        SettingsMessageUtil.save( resource );
        
        RequestFacade.sendMessage( "service/local/exception?status=400", Method.GET, null );
        
        ErrorReportUtil.validateNoZip( nexusWorkDir );
    }
}
