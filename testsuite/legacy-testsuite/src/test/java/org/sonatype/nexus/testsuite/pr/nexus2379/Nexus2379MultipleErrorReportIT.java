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

package org.sonatype.nexus.testsuite.pr.nexus2379;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.ErrorReportUtil;
import org.sonatype.nexus.test.utils.ResponseMatchers;

import org.junit.Before;
import org.junit.Test;

public class Nexus2379MultipleErrorReportIT
    extends AbstractNexusIntegrationTest
{
  @Before
  public void cleanDirs()
      throws Exception
  {
    ErrorReportUtil.cleanErrorBundleDir(nexusWorkDir);
  }

  @Test
  public void validateMultipleErrors()
      throws Exception
  {
    RequestFacade.doGet("service/local/exception?status=500", ResponseMatchers.inError());

    ErrorReportUtil.validateZipContents(nexusWorkDir);

    ErrorReportUtil.cleanErrorBundleDir(nexusWorkDir);

    ErrorReportUtil.validateNoZip(nexusWorkDir);

    for (int i = 0; i < 10; i++) {
      RequestFacade.doGet("service/local/exception?status=500", ResponseMatchers.inError());
      ErrorReportUtil.validateNoZip(nexusWorkDir);
    }

    RequestFacade.doGet("service/local/exception?status=501", ResponseMatchers.inError());

    ErrorReportUtil.validateZipContents(nexusWorkDir);
  }
}