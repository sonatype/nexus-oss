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
package org.sonatype.nexus.testsuite.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;

import javax.inject.Inject;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.common.io.DirSupport;
import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.log.LoggerLevel;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.maven.MavenFacet;
import org.sonatype.nexus.repository.maven.MavenPath;
import org.sonatype.nexus.repository.maven.MavenPath.HashType;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.util.TypeTokens;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.testsuite.NexusCoreITSupport;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.it.Verifier;
import org.junit.Before;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

/**
 * Naveb IT support.
 */
public abstract class MavenITSupport
    extends NexusCoreITSupport
{
  @Inject
  protected LogManager logManager;

  private final MetadataXpp3Reader reader = new MetadataXpp3Reader();

  @Configuration
  public static Option[] configureNexus() {
    return options(nexusDistribution("org.sonatype.nexus.assemblies", "nexus-base-template"),
        wrappedBundle(maven("org.apache.maven.shared", "maven-verifier").versionAsInProject()),
        wrappedBundle(maven("org.apache.maven.shared", "maven-shared-utils").versionAsInProject()));
  }

  @Before
  public void setupMavenDebugLogging() {
    logManager.setLoggerLevel("org.sonatype.nexus.repository.maven", LoggerLevel.DEBUG);
  }

  protected void mvnDeploy(final String project, final String version, final String deployRepositoryName)
      throws Exception
  {
    // TODO: Rule TestName does not work due to PAX ITs?
    final File mavenBaseDir = resolveBaseFile("target/maven-it-support/" + project).getAbsoluteFile();
    final File mavenSettings = new File(mavenBaseDir, "settings.xml").getAbsoluteFile();
    final File mavenPom = new File(mavenBaseDir, "pom.xml").getAbsoluteFile();

    DirSupport.mkdir(mavenBaseDir.toPath());

    {
      // set settings NX port
      final String settingsXml = Files.toString(resolveTestFile("settings.xml"), Charsets.UTF_8).replace(
          "${nexus.port}", String.valueOf(nexusUrl.getPort()));
      Files.write(settingsXml, mavenSettings, Charsets.UTF_8);
    }

    final File projectDir = resolveTestFile(project);
    DirSupport.copy(projectDir.toPath(), mavenBaseDir.toPath());

    {
      // set POM version
      final String pomXml = Files.toString(new File(projectDir, "pom.xml"), Charsets.UTF_8).replace(
          "${project.version}", version);
      Files.write(pomXml, mavenPom, Charsets.UTF_8);
    }

    Verifier verifier = new Verifier(mavenBaseDir.getAbsolutePath());
    verifier.addCliOption("-s " + mavenSettings.getAbsolutePath());
    verifier.addCliOption(
        // Verifier replaces // -> /
        "-DaltDeploymentRepository=local-nexus-admin::default::http:////localhost:" + nexusUrl.getPort() +
            "/repository/" + deployRepositoryName);
    verifier.executeGoals(Arrays.asList("clean", "deploy"));
    verifier.verifyErrorFreeLog();
  }

  protected void write(final Repository repository, final String path, final Payload payload) throws IOException {
    final MavenFacet mavenFacet = repository.facet(MavenFacet.class);
    final MavenPath mavenPath = mavenFacet.getMavenPathParser().parsePath(path);
    UnitOfWork.begin(repository.facet(StorageFacet.class).txSupplier());
    try {
      mavenFacet.put(mavenPath, payload);
    }
    finally {
      UnitOfWork.end();
    }
  }

  protected Content read(final Repository repository, final String path) throws IOException {
    final MavenFacet mavenFacet = repository.facet(MavenFacet.class);
    final MavenPath mavenPath = mavenFacet.getMavenPathParser().parsePath(path);
    UnitOfWork.begin(repository.facet(StorageFacet.class).txSupplier());
    try {
      return mavenFacet.get(mavenPath);
    }
    finally {
      UnitOfWork.end();
    }
  }

  protected Metadata parseMetadata(final Content content) throws Exception {
    assertThat(content, notNullValue());
    try (InputStream is = content.openInputStream()) {
      return reader.read(is);
    }
  }

  protected void verifyHashesExistAndCorrect(final Repository repository, final String path) throws Exception {
    final MavenFacet mavenFacet = repository.facet(MavenFacet.class);
    final MavenPath mavenPath = mavenFacet.getMavenPathParser().parsePath(path);
    UnitOfWork.begin(repository.facet(StorageFacet.class).txSupplier());
    try {
      final Content content = mavenFacet.get(mavenPath);
      assertThat(content, notNullValue());
      final Map<HashAlgorithm, HashCode> hashCodes = content.getAttributes()
          .require(Content.CONTENT_HASH_CODES_MAP, TypeTokens.HASH_CODES_MAP);
      for (HashType hashType : HashType.values()) {
        final Content contentHash = mavenFacet.get(mavenPath.hash(hashType));
        final String storageHash = hashCodes.get(hashType.getHashAlgorithm()).toString();
        assertThat(storageHash, notNullValue());
        try (InputStream is = contentHash.openInputStream()) {
          final String mavenHash = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
          assertThat(storageHash, equalTo(mavenHash));
        }
      }
    }
    finally {
      UnitOfWork.end();
    }
  }
}
