/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nxcm1928;

import java.io.IOException;

import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.Jira;
import org.codehaus.swizzle.jira.User;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ErrorReportResponse;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.ErrorReportUtil;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

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
