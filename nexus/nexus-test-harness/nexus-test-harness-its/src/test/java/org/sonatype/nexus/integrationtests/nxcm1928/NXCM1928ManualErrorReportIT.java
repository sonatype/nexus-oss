package org.sonatype.nexus.integrationtests.nxcm1928;

import java.io.IOException;

import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.Jira;
import org.codehaus.swizzle.jira.User;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ErrorReportResponse;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.ErrorReportUtil;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;

public class NXCM1928ManualErrorReportIT
    extends AbstractNexusIntegrationTest
{

    private static final String ITS_USER = "sonatypeits";

    // @Test
    public void generateReportWithAuthentication()
        throws Exception
    {
        ErrorReportResponse response =
            ErrorReportUtil.generateProblemReport( "sometitle", "somedescription", ITS_USER, ITS_USER );

        Assert.assertNotNull( response );

        Assert.assertNotNull( response.getData().getJiraUrl() );

        Jira jira = new Jira( "https://issues.sonatype.org/" );
        jira.login( ITS_USER, ITS_USER );
        Issue issue =
            jira.getIssue( response.getData().getJiraUrl().replace( "http://issues.sonatype.org/browse/", "" ) );

        User reporter = issue.getReporter();
        Assert.assertEquals( ITS_USER, reporter.getName() );
    }

    // @Test
    public void invalidUsers()
        throws Exception
    {
        Response response =
            ErrorReportUtil.generateProblemResponse( "sometitle", "somedescription",
                                                     "someDummyUserToBreakIntegrationTest",
                                                     Long.toHexString( System.nanoTime() ) );

        Assert.assertEquals( Status.CLIENT_ERROR_BAD_REQUEST.getCode(), response.getStatus().getCode() );
    }

    @Test
    public void resetUser()
        throws Exception
    {
        reset( "" );
        reset( null );
    }

    private void reset( String resetVal )
        throws IOException
    {
        final String user = "AAaaBBbb";

        GlobalConfigurationResource cfg = SettingsMessageUtil.getCurrentSettings();

        cfg.getErrorReportingSettings().setJiraUsername( user );
        cfg.getErrorReportingSettings().setJiraPassword( user );

        Assert.assertTrue( SettingsMessageUtil.save( cfg ).isSuccess() );

        cfg = SettingsMessageUtil.getCurrentSettings();

        Assert.assertEquals( user, cfg.getErrorReportingSettings().getJiraUsername() );

        // let's reset it now
        cfg.getErrorReportingSettings().setJiraUsername( resetVal );
        cfg.getErrorReportingSettings().setJiraPassword( resetVal );

        Assert.assertTrue( SettingsMessageUtil.save( cfg ).isSuccess() );

        cfg = SettingsMessageUtil.getCurrentSettings();

        Assert.assertEquals( resetVal, cfg.getErrorReportingSettings().getJiraUsername() );
        Assert.assertEquals( resetVal, cfg.getErrorReportingSettings().getJiraPassword() );
    }

}
