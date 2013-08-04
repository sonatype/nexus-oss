/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.testsuite.pr.nxcm1928;

import java.io.IOException;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ErrorReportResponse;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.ErrorReportUtil;
import org.sonatype.nexus.test.utils.NexusRequestMatchers;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;

import org.codehaus.swizzle.jira.Issue;
import org.codehaus.swizzle.jira.Jira;
import org.codehaus.swizzle.jira.User;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.restlet.data.Status;

public class NXCM1928ManualErrorReportIT
    extends AbstractNexusIntegrationTest
{

  private static final String ITS_USER = "sonatypeits";

  @Test
  @Ignore
  public void generateReportWithAuthentication()
      throws Exception
  {
    ErrorReportResponse response =
        ErrorReportUtil.generateProblemReport("sometitle", "somedescription", ITS_USER, ITS_USER);

    Assert.assertNotNull(response);

    Assert.assertNotNull(response.getData().getJiraUrl());

    Jira jira = new Jira("https://issues.sonatype.org/");
    jira.login(ITS_USER, ITS_USER);
    Issue issue =
        jira.getIssue(response.getData().getJiraUrl().replace("http://issues.sonatype.org/browse/", ""));

    User reporter = issue.getReporter();
    Assert.assertEquals(reporter.getName(), ITS_USER);
  }

  @Test
  public void invalidUsers()
      throws Exception
  {
    ErrorReportUtil.matchProblemResponse(
        "sometitle", "somedescription",
        "someDummyUserToBreakIntegrationTest",
        Long.toHexString(System.nanoTime()),
        NexusRequestMatchers.respondsWithStatus(Status.CLIENT_ERROR_BAD_REQUEST));
  }

  @Test
  public void resetUser()
      throws Exception
  {
    reset("");
    reset(null);
  }

  private void reset(String resetVal)
      throws IOException
  {
    final String user = "AAaaBBbb";

    GlobalConfigurationResource cfg = SettingsMessageUtil.getCurrentSettings();

    cfg.getErrorReportingSettings().setJiraUsername(user);
    cfg.getErrorReportingSettings().setJiraPassword(user);

    Assert.assertTrue(SettingsMessageUtil.save(cfg).isSuccess());

    cfg = SettingsMessageUtil.getCurrentSettings();

    Assert.assertEquals(cfg.getErrorReportingSettings().getJiraUsername(), user);

    // let's reset it now
    cfg.getErrorReportingSettings().setJiraUsername(resetVal);
    cfg.getErrorReportingSettings().setJiraPassword(resetVal);

    Assert.assertTrue(SettingsMessageUtil.save(cfg).isSuccess());

    cfg = SettingsMessageUtil.getCurrentSettings();

    Assert.assertEquals(cfg.getErrorReportingSettings().getJiraUsername(), resetVal);
    Assert.assertEquals(cfg.getErrorReportingSettings().getJiraPassword(), resetVal);
  }

}
