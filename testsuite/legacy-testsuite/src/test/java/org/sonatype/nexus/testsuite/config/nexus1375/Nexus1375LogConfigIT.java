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

package org.sonatype.nexus.testsuite.config.nexus1375;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.LogConfigResource;
import org.sonatype.nexus.test.utils.LogConfigMessageUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.MediaType;

/**
 * @author juven
 */
public class Nexus1375LogConfigIT
    extends AbstractNexusIntegrationTest
{

  protected LogConfigMessageUtil messageUtil;

  @BeforeClass
  public static void setSecureTest() {
    TestContainer.getInstance().getTestContext().setSecureTest(true);
    TestContainer.getInstance().getTestContext().useAdminForRequests();
  }

  @Before
  public void setUp() {
    messageUtil = new LogConfigMessageUtil(this.getXMLXStream(), MediaType.APPLICATION_XML);
  }

  @Test
  public void getLogConfig()
      throws Exception
  {
    LogConfigResource resource = messageUtil.getLogConfig();

    Assert.assertEquals("DEBUG", resource.getRootLoggerLevel());

    Assert.assertEquals("%4d{yyyy-MM-dd HH:mm:ss} %-5p [%thread] - %c - %m%n", resource.getFileAppenderPattern());

    // exposing actual OS file location over REST is very bad idea...
    // File actualLoggerLocation = new File( resource.getFileAppenderLocation() ).getCanonicalFile();
    // Assert.assertTrue( nexusLog.getAbsoluteFile().equals( actualLoggerLocation.getAbsoluteFile() ) );
  }

  @Test
  public void updateLogConfig()
      throws Exception
  {
    LogConfigResource resource = messageUtil.getLogConfig();

    Assert.assertEquals("DEBUG", resource.getRootLoggerLevel());

    resource.setRootLoggerLevel("ERROR");

    messageUtil.updateLogConfig(resource);

    Assert.assertEquals("ERROR", resource.getRootLoggerLevel());

    resource.setRootLoggerLevel("DEBUG");

    messageUtil.updateLogConfig(resource);

    Assert.assertEquals("DEBUG", resource.getRootLoggerLevel());
  }
}
