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
package org.sonatype.nexus.integrationtests;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers;

import org.apache.maven.index.artifact.Gav;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.nexus.test.utils.GavUtil.getRelitiveArtifactPath;
import static org.sonatype.nexus.test.utils.GavUtil.getRelitivePomPath;

/**
 * Helpers to deploy content.
 *
 * @since 3.0
 */
public class DeployHelper
{
  private static final Logger log = LoggerFactory.getLogger(DeployHelper.class);

  private final DeployUtils deployUtils = new DeployUtils();

  public void deployArtifacts(final File projectsDir) throws Exception {
    TestContainer.getInstance().getTestContext().useAdminForRequests();

    // skip if nothing to deploy
    if (projectsDir == null || !projectsDir.isDirectory()) {
      return;
    }

    log.info("Deploying artifacts; projects directory: {}", projectsDir);

    // we have the parent dir, for each child (one level) we need to grab the pom.xml out of it and parse it,
    // and then deploy the artifact, sounds like fun, right!

    final File[] projectFolders = projectsDir.listFiles(new FileFilter() {
      @Override
      public boolean accept(final File pathname) {
        return pathname.isDirectory() && new File(pathname, "pom.xml").exists();
      }
    });
    if (projectFolders == null) {
      // bail out
      return;
    }

    // to achieve same ordering on different OSes
    Arrays.sort(projectFolders);

    for (File project : projectFolders) {
      File pom = new File(project, "pom.xml");
      assertThat(pom, FileMatchers.exists());
      log.info("Deploying artifacts from project: {}", pom);

      MavenXpp3Reader reader = new MavenXpp3Reader();
      Model model;
      try (FileInputStream fis = new FileInputStream(pom)) {
        model = reader.read(fis);
      }

      // a helpful note so you don't need to dig into the code to much.
      if (model.getDistributionManagement() == null || model.getDistributionManagement().getRepository() == null) {
        Assert.fail("The test artifact is either missing or has an invalid Distribution Management section.");
      }

      // get the URL to deploy
      String deployUrl = model.getDistributionManagement().getRepository().getUrl();

      // get the protocol
      String deployUrlProtocol = deployUrl.substring(0, deployUrl.indexOf(":"));

      // calculate the wagon hint
      String wagonHint = deployUrlProtocol;

      deployArtifacts(project, wagonHint, deployUrl, model);
    }
  }

  private void deployArtifacts(File project, String wagonHint, String deployUrl, Model model) throws Exception {
    log.info("Deploying project \"{}\" using Wagon:{} to URL=\"{}\".", project.getAbsolutePath(), wagonHint, deployUrl);

    // we already check if the pom.xml was in here.
    File pom = new File(project, "pom.xml");

    // FIXME, this needs to be fluffed up a little, should add the classifier, etc.
    String extension = model.getPackaging();
    // for now, only due to Nexus570IndexArchetypeIT
    // no other IT specifies other packaging where extension != packaging
    if ("maven-archetype".equals(extension)) {
      extension = "jar";
    }
    String artifactFileName = model.getArtifactId() + "." + extension;
    File artifactFile = new File(project, artifactFileName);

    final Gav gav = new Gav(
        model.getGroupId(),
        model.getArtifactId(),
        model.getVersion(),
        null, // classifer
        extension,
        null, // snap #
        null, // snap ts
        artifactFile.getName(),
        false, // hash
        null,  // hash type
        false, // sig
        null   // sig type
    );

    // the Restlet Client does not support multipart forms:
    // http://restlet.tigris.org/issues/show_bug.cgi?id=71
    // int status = DeployUtils.deployUsingPomWithRest( deployUrl, repositoryId, gav, artifactFile, pom );

    if (!"pom".equals(model.getPackaging()) && !artifactFile.isFile()) {
      throw new FileNotFoundException("File " + artifactFile.getAbsolutePath() + " doesn't exists!");
    }

    File artifactSha1 = new File(artifactFile.getAbsolutePath() + ".sha1");
    File artifactMd5 = new File(artifactFile.getAbsolutePath() + ".md5");
    File artifactAsc = new File(artifactFile.getAbsolutePath() + ".asc");

    File pomSha1 = new File(pom.getAbsolutePath() + ".sha1");
    File pomMd5 = new File(pom.getAbsolutePath() + ".md5");
    File pomAsc = new File(pom.getAbsolutePath() + ".asc");

    try {
      if (artifactSha1.exists()) {
        deployUtils.deployWithWagon(wagonHint, deployUrl, artifactSha1, getRelitiveArtifactPath(gav) + ".sha1");
      }
      if (artifactMd5.exists()) {
        deployUtils.deployWithWagon(wagonHint, deployUrl, artifactMd5, getRelitiveArtifactPath(gav) + ".md5");
      }
      if (artifactAsc.exists()) {
        deployUtils.deployWithWagon(wagonHint, deployUrl, artifactAsc, getRelitiveArtifactPath(gav) + ".asc");
      }

      if (artifactFile.exists()) {
        deployUtils.deployWithWagon(wagonHint, deployUrl, artifactFile, getRelitiveArtifactPath(gav));
      }

      if (pomSha1.exists()) {
        deployUtils.deployWithWagon(wagonHint, deployUrl, pomSha1, getRelitivePomPath(gav) + ".sha1");
      }
      if (pomMd5.exists()) {
        deployUtils.deployWithWagon(wagonHint, deployUrl, pomMd5, getRelitivePomPath(gav) + ".md5");
      }
      if (pomAsc.exists()) {
        deployUtils.deployWithWagon(wagonHint, deployUrl, pomAsc, getRelitivePomPath(gav) + ".asc");
      }

      deployUtils.deployWithWagon(wagonHint, deployUrl, pom, getRelitivePomPath(gav));
    }
    catch (Exception e) {
      log.error("Failed to deploy {}", artifactFileName, e);
      throw e;
    }
  }
}
