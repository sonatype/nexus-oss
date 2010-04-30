package org.sonatype.nexus.integrationtests.nexus2213;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.ErrorReportingSettings;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.ErrorReportUtil;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;

public class Nexus2213ErrorReportBundleIT
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
        enableAPR();

        Status s = RequestFacade.sendMessage( "service/local/exception?status=500", Method.GET, null ).getStatus();
        Assert.assertEquals( 500, s.getCode() );

        ErrorReportUtil.validateZipContents( nexusWorkDir );
    }

    @Test
    public void validateBundleNotCreated()
        throws Exception
    {
        enableAPR();

        RequestFacade.sendMessage( "service/local/exception?status=400", Method.GET, null );

        ErrorReportUtil.validateNoZip( nexusWorkDir );
    }

    private void enableAPR()
        throws IOException
    {
        // Default config
        GlobalConfigurationResource resource = SettingsMessageUtil.getCurrentSettings();

        // Set some values
        ErrorReportingSettings settings = new ErrorReportingSettings();
        settings.setJiraUsername( "someusername" );
        settings.setJiraPassword( "somepassword" );
        settings.setReportErrorsAutomatically( true );

        resource.setErrorReportingSettings( settings );

        SettingsMessageUtil.save( resource );
    }
}
