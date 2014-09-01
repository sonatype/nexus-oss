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
package org.sonatype.nexus.testsuite.security.nexus1071;

import java.io.File;

import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.ITGroups.SECURITY;
import org.sonatype.nexus.integrationtests.MavenVerifierHelper;
import org.sonatype.nexus.integrationtests.TestContainer;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Juven Xu
 */
public class Nexus1071DeployToRepoAnonCannotAccessIT
    extends AbstractNexusIntegrationTest
{
  private static final MavenVerifierHelper mavenVerifierHelper = new MavenVerifierHelper();

  @BeforeClass
  public static void setSecureTest() {
    TestContainer.getInstance().getTestContext().setSecureTest(true);
  }

  @Test
  @Category(SECURITY.class)
  public void deployRepeatly()
      throws Exception
  {
    File mavenProject1 = getTestFile("maven-project-1");

    File settings1 = getTestFile("settings1.xml");

    Verifier verifier1 = null;

    try {
      verifier1 = mavenVerifierHelper.createMavenVerifier(mavenProject1, settings1, getTestId());

      verifier1.executeGoal("deploy");

      verifier1.verifyErrorFreeLog();
    }

    catch (VerificationException e) {
      mavenVerifierHelper.failTest(verifier1);
    }

    try {
      verifier1.executeGoal("deploy");

      verifier1.verifyErrorFreeLog();

      Assert.fail("Should return 401 error");
    }
    catch (VerificationException e) {
      // 401 error
    }
  }

  @Test
  @Category(SECURITY.class)
  public void deploySnapshot()
      throws Exception
  {
    File mavenProject = getTestFile("maven-project-snapshot");

    File settings = getTestFile("settings-snapshot.xml");

    Verifier verifier = null;

    try {
      verifier = mavenVerifierHelper.createMavenVerifier(mavenProject, settings, getTestId());

      verifier.executeGoal("deploy");

      verifier.verifyErrorFreeLog();
    }

    catch (VerificationException e) {
      mavenVerifierHelper.failTest(verifier);
    }
  }

  @Test
  @Category(SECURITY.class)
  public void deployToAnotherRepo()
      throws Exception
  {
    File mavenProject2 = getTestFile("maven-project-2");

    File settings2 = getTestFile("settings2.xml");

    Verifier verifier2 = null;

    try {
      verifier2 = mavenVerifierHelper.createMavenVerifier(mavenProject2, settings2, getTestId());

      verifier2.executeGoal("deploy");

      verifier2.verifyErrorFreeLog();
    }
    catch (VerificationException e) {
      mavenVerifierHelper.failTest(verifier2);
    }
  }

  @Test
  @Category(SECURITY.class)
  public void anonDeploy()
      throws Exception
  {
    File mavenProjectAnon = getTestFile("maven-project-anon");

    File settingsAnon = getTestFile("settings-anon.xml");

    Verifier verifierAnon = null;

    try {
      verifierAnon = mavenVerifierHelper.createMavenVerifier(mavenProjectAnon, settingsAnon, getTestId());

      verifierAnon.executeGoal("deploy");

      verifierAnon.verifyErrorFreeLog();

      Assert.fail("Should return 401 error");
    }
    catch (VerificationException e) {
      // test pass
    }
  }

}
