/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.deploy.nexus511;

import java.io.File;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.MavenVerifierHelper;
import org.sonatype.nexus.integrationtests.TestContainer;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests deploy to nexus using mvn deploy
 */
public class Nexus511MavenDeployIT
    extends AbstractNexusIntegrationTest
{
  private static final MavenVerifierHelper mavenVerifierHelper = new MavenVerifierHelper();

  private Verifier verifier;

  @BeforeClass
  public static void setSecureTest() {
    TestContainer.getInstance().getTestContext().setSecureTest(true);
  }

  @Before
  public void createVerifier()
      throws Exception
  {
    File mavenProject = getTestFile("maven-project");
    File settings = getTestFile("server.xml");
    verifier = mavenVerifierHelper.createMavenVerifier(mavenProject, settings, getTestId());
  }

  @Test
  public void deploy()
      throws Exception
  {
    try {
      verifier.executeGoal("deploy");
      verifier.verifyErrorFreeLog();
    }
    catch (VerificationException e) {
      mavenVerifierHelper.failTest(verifier);
    }
  }

  @Test
  public void privateDeploy()
      throws Exception
  {
    // try to deploy without servers authentication tokens
    File mavenProject = getTestFile("maven-project");
    File settings = getTestFile("serverWithoutAuthentication.xml");
    verifier = mavenVerifierHelper.createMavenVerifier(mavenProject, settings, getTestId());

    try {
      verifier.executeGoal("deploy");
      verifier.verifyErrorFreeLog();
      mavenVerifierHelper.failTest(verifier);
    }
    catch (VerificationException e) {
      // Expected exception
    }
  }

}
