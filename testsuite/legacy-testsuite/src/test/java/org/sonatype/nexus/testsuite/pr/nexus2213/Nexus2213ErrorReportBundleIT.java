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

package org.sonatype.nexus.testsuite.pr.nexus2213;

import java.io.IOException;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.ErrorReportingSettings;
import org.sonatype.nexus.rest.model.GlobalConfigurationResource;
import org.sonatype.nexus.test.utils.ErrorReportUtil;
import org.sonatype.nexus.test.utils.SettingsMessageUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Status;

public class Nexus2213ErrorReportBundleIT
    extends AbstractNexusIntegrationTest
{
  @Before
  public void cleanDirs()
      throws Exception
  {
    ErrorReportUtil.cleanErrorBundleDir(nexusWorkDir);
  }

  @Test
  public void validateBundleCreated()
      throws Exception
  {
    enableAPR();

    Status s = RequestFacade.sendMessage("service/local/exception?status=500", Method.GET, null).getStatus();
    Assert.assertEquals(500, s.getCode());

    ErrorReportUtil.validateZipContents(nexusWorkDir);
  }

  @Test
  public void validateBundleNotCreated()
      throws Exception
  {
    enableAPR();

    RequestFacade.sendMessage("service/local/exception?status=400", Method.GET, null);

    ErrorReportUtil.validateNoZip(nexusWorkDir);
  }

  private void enableAPR()
      throws IOException
  {
    // Default config
    GlobalConfigurationResource resource = SettingsMessageUtil.getCurrentSettings();

    // Set some values
    ErrorReportingSettings settings = new ErrorReportingSettings();
    settings.setJiraUsername("someusername");
    settings.setJiraPassword("somepassword");
    settings.setReportErrorsAutomatically(true);

    resource.setErrorReportingSettings(settings);

    SettingsMessageUtil.save(resource);
  }
}
