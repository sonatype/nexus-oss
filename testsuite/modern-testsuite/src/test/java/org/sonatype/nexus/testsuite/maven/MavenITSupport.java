/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.maven;

import java.io.File;
import java.util.Arrays;

import javax.inject.Inject;

import org.sonatype.nexus.common.io.DirSupport;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.log.LoggerLevel;
import org.sonatype.nexus.testsuite.NexusCoreITSupport;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

/**
 * Naveb IT support.
 */
public abstract class MavenITSupport
    extends NexusCoreITSupport
{
  @Inject
  private LogManager logManager;

  @Configuration
  public static Option[] configureNexus() {
    return options(nexusDistribution("org.sonatype.nexus.assemblies", "nexus-base-template"),
        wrappedBundle(maven("org.apache.maven.shared", "maven-verifier").versionAsInProject()),
        wrappedBundle(maven("org.apache.maven.shared", "maven-shared-utils").versionAsInProject()));
  }

  @Before
  public void debugLogging() {
    logManager.setLoggerLevel("org.sonatype.nexus.repository.maven", LoggerLevel.DEBUG);
  }

  protected void mvnDeploy(final String project, final String deployRepositoryName) throws Exception {
    File mavenBaseDir = resolveBaseFile("target/maven-rebuild-metadata/" + project).getAbsoluteFile();
    DirSupport.mkdir(mavenBaseDir.toPath());

    final String settingsXml = Files.toString(resolveTestFile("settings.xml"), Charsets.UTF_8).replace(
        "${nexus.port}", String.valueOf(nexusUrl.getPort()));
    File mavenSettings = new File(mavenBaseDir, "settings.xml").getAbsoluteFile();
    Files.write(settingsXml, mavenSettings, Charsets.UTF_8);

    DirSupport.copy(resolveTestFile(project).toPath(), mavenBaseDir.toPath());

    Verifier verifier = new Verifier(mavenBaseDir.getAbsolutePath());
    verifier.addCliOption("-s " + mavenSettings.getAbsolutePath());
    verifier.addCliOption(
        // TODO: verifier replaces // -> /
        "-DaltDeploymentRepository=local-nexus-admin::default::http:////localhost:" + nexusUrl.getPort() +
            "/repository/" + deployRepositoryName);
    verifier.executeGoals(Arrays.asList("clean", "deploy"));
    verifier.verifyErrorFreeLog();
  }
}
