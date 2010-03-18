package org.sonatype.nexus.integrationtests.nexus3082;

import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.Jira;
import org.codehaus.swizzle.jira.User;
import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ErrorReportResponse;
import org.sonatype.nexus.test.utils.ErrorReportUtil;

public class Nexus3082GenerateProblemReportIT
    extends AbstractNexusIntegrationTest
{
    // @Test
    public void generateReport()
        throws Exception
    {
        ErrorReportResponse response = ErrorReportUtil.generateProblemReport( "sometitle", "somedescription" );

        Assert.assertNotNull( response );

        Assert.assertNotNull( response.getData().getJiraUrl() );
    }

    // @Test
    public void generateReportWithFailure()
        throws Exception
    {
        ErrorReportUtil.generateProblemReport( null, "somedescription" );
    }

    @Test
    // NXCM-1928
    public void generateReportWithAuthentication()
        throws Exception
    {
        ErrorReportResponse response =
            ErrorReportUtil.generateProblemReport( "sometitle", "somedescription", "sonatypeits", "sonatypeits" );

        Assert.assertNotNull( response );

        Assert.assertNotNull( response.getData().getJiraUrl() );

        Jira jira = new Jira( "https://issues.sonatype.org/" );
        jira.login( "sonatypeits", "sonatypeits" );
        Issue issue =
            jira.getIssue( response.getData().getJiraUrl().replace( "http://issues.sonatype.org/browse/", "" ) );

        User reporter = issue.getReporter();
        Assert.assertEquals( "sonatypeits", reporter.getName() );
    }

}
