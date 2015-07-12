/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.raw;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import org.sonatype.nexus.common.io.DirSupport;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.ops4j.pax.exam.Option;

import static java.util.Arrays.asList;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

/**
 * Deploys a maven site to a raw repository.
 */
public abstract class MavenSiteTestSupport
    extends RawITSupport
{
  @org.ops4j.pax.exam.Configuration
  public static Option[] configureNexus() {
    return options(nexusDistribution("org.sonatype.nexus.assemblies", "nexus-base-template"),
        withHttps(),
        wrappedBundle(maven("org.apache.maven.shared", "maven-verifier").versionAsInProject()),
        wrappedBundle(maven("org.apache.maven.shared", "maven-shared-utils").versionAsInProject()));
  }

  protected void mvn(final String project, final String projectVersion, final String repositoryName,
                     final String... goals) throws IOException, VerificationException
  {
    final ImmutableMap<String, String> replacements = ImmutableMap.of(
        "${project.version}", projectVersion,
        "${reponame}", repositoryName,
        "${nexus.port}", String.valueOf(nexusUrl.getPort())
    );

    final File mavenBaseDir = resolveBaseFile("target/raw-mvn-site/" + project).getAbsoluteFile();
    DirSupport.mkdir(mavenBaseDir.toPath());

    final File mavenSettings = createMavenSettings(mavenBaseDir, replacements);

    final File projectDir = resolveTestFile(project);
    DirSupport.copy(projectDir.toPath(), mavenBaseDir.toPath());

    writePom(mavenBaseDir, projectDir, replacements);

    Verifier verifier = buildMavenVerifier(repositoryName, mavenBaseDir, mavenSettings);

    log.info("Executing maven goals {}", goals);
    verifier.executeGoals(asList(goals));
    verifier.verifyErrorFreeLog();
  }

  private void writePom(final File mavenBaseDir, final File projectDir, final ImmutableMap<String, String> replacements)
      throws IOException
  {
    final File output = new File(mavenBaseDir, "pom.xml").getAbsoluteFile();
    log.info("Creating test project pom.xml in {}", output);
    writeModifiedFile(new File(projectDir, "pom.xml"),
        output,
        replacements);
  }


  /**
   * Produces a maven settings file, pointing to the test Nexus instance.
   */
  @Nonnull
  private File createMavenSettings(final File mavenBaseDir, final ImmutableMap<String, String> replacements)
      throws IOException
  {
    final File output = new File(mavenBaseDir, "settings.xml").getAbsoluteFile();
    log.info("Creating maven settings file in {}", output);
    return writeModifiedFile(resolveTestFile("settings.xml"),
        output,
        replacements);
  }

  @Nonnull
  private File writeModifiedFile(final File source, final File target, final Map<String, String> replacements)
      throws IOException
  {
    String content = Files.toString(source, Charsets.UTF_8);
    for (Entry<String, String> entry : replacements.entrySet()) {
      content = content.replace(entry.getKey(), entry.getValue());
    }

    Files.write(content, target, Charsets.UTF_8);

    return target;
  }

  @Nonnull
  private Verifier buildMavenVerifier(final String repositoryName, final File mavenBaseDir,
                                      final File mavenSettings) throws VerificationException
  {
    log.info("Building mvn verifier");
    Verifier verifier = new Verifier(mavenBaseDir.getAbsolutePath());
    verifier.addCliOption("-s " + mavenSettings.getAbsolutePath());
    verifier.addCliOption(
        // Verifier replaces // -> /
        "-DaltDeploymentRepository=local-nexus-admin::default::http:////localhost:" + nexusUrl.getPort() +
            "/repository/" + repositoryName);
    return verifier;
  }
}
