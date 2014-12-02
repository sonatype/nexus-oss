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
import java.util.Date;

import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.ITGroups.SECURITY;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.security.rest.model.UserResource;

import org.apache.maven.index.artifact.Gav;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

/**
 * @author Juven Xu
 */
public class Nexus1071AnonAccessIT
    extends AbstractPrivilegeTest
{

  @Override
  protected void prepareSecurity() throws Exception {
    super.prepareSecurity();
    String p1 = createPrivileges("Public Repos", "1", null, "public", asList("read")).get(0).getId();
    String p2 = createPrivileges("Public Snapshot Repos", "1", null, "public-snapshots", asList("read")).get(0).getId();
    String p3 = createPrivileges("MetaData", "4", asList("update")).get(0).getId();

    createRole("r1", asList(p1, p2));
    createRole("r2", asList(p3));
    createRole("r3", asList("repository-m2-create", "repository-m2-delete", "repository-m2-read", p3));

    UserResource deployment = userUtil.getUser("deployment");
    deployment.setRoles(asList("nx-deployment", "r3"));
    userUtil.updateUser(deployment);

    UserResource anonymous = userUtil.getUser("anonymous");
    anonymous.setRoles(asList("anonymous", "r1"));
    userUtil.updateUser(anonymous);
  }

  @BeforeClass
  public static void setSecureTest() {
    TestContainer.getInstance().getTestContext().setSecureTest(true);
  }

  @Test
  @Category(SECURITY.class)
  public void downloadArtifactFromPublicGroup()
      throws Exception
  {
    Gav gav =
        new Gav(this.getTestId(), "release-jar", "1", null, "jar", 0, new Date().getTime(), "Release Jar",
            false, null, false, null);

    File artifact = this.downloadArtifactFromGroup("public", gav, "./target/downloaded-jars");

    assertTrue(artifact.exists());

    File originalFile =
        this.getTestResourceAsFile("projects/" + gav.getArtifactId() + "/" + gav.getArtifactId() + "."
            + gav.getExtension());

    Assert.assertTrue(FileTestingUtils.compareFileSHA1s(originalFile, artifact));

  }
}
