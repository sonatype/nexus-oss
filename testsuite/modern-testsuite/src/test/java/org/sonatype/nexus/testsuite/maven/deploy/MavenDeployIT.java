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
package org.sonatype.nexus.testsuite.maven.deploy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.inject.Inject;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.maven.MavenFacet;
import org.sonatype.nexus.repository.maven.MavenPath;
import org.sonatype.nexus.repository.maven.MavenPath.HashType;
import org.sonatype.nexus.repository.util.TypeTokens;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.testsuite.maven.MavenITSupport;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.io.CharStreams;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.it.VerificationException;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

/**
 * Simple Maven deploy ITs, running against same NX instance, with purpose to "sanity test" Maven repositories.
 */
@ExamReactorStrategy(PerClass.class)
public class MavenDeployIT
    extends MavenITSupport
{
  @Inject
  private RepositoryManager repositoryManager;

  private Repository mavenSnapshots;

  private Repository mavenReleases;

  private MetadataXpp3Reader reader = new MetadataXpp3Reader();

  @Before
  public void prepare() throws Exception {
    mavenSnapshots = repositoryManager.get("maven-snapshots");
    mavenReleases = repositoryManager.get("maven-releases");
  }

  private void write(final Repository repository, final String path, final Payload payload) throws IOException {
    final MavenFacet mavenFacet = repository.facet(MavenFacet.class);
    final MavenPath mavenPath = mavenFacet.getMavenPathParser().parsePath(path);
    mavenFacet.put(mavenPath, payload);
  }

  private Content read(final Repository repository, final String path) throws IOException {
    final MavenFacet mavenFacet = repository.facet(MavenFacet.class);
    final MavenPath mavenPath = mavenFacet.getMavenPathParser().parsePath(path);
    return mavenFacet.get(mavenPath);
  }

  private Metadata parse(final Content content) throws Exception {
    assertThat(content, notNullValue());
    try (InputStream is = content.openInputStream()) {
      return reader.read(is);
    }
  }

  private void verifyHashesExistAndCorrect(final Repository repository, final String path) throws Exception {
    final MavenFacet mavenFacet = repository.facet(MavenFacet.class);
    final MavenPath mavenPath = mavenFacet.getMavenPathParser().parsePath(path);
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

  final String G_MD_PATH = "/org/sonatype/nexus/testsuite/maven-metadata.xml";

  final String A_MD_JAR_PATH = "/org/sonatype/nexus/testsuite/testproject/maven-metadata.xml";

  final String V_MD_JAR_PATH = "/org/sonatype/nexus/testsuite/testproject/1.0-SNAPSHOT/maven-metadata.xml";

  final String A_MD_PLUGIN_PATH = "/org/sonatype/nexus/testsuite/testplugin/maven-metadata.xml";

  final String V_MD_PLUGIN_PATH = "/org/sonatype/nexus/testsuite/testplugin/1.0-SNAPSHOT/maven-metadata.xml";

  /**
   * NX should prevent snapshot deploy to release repo.
   */
  @Test(expected = VerificationException.class)
  public void snapshotToRelease() throws Exception {
    mvnDeploy("testproject", "1.0-SNAPSHOT", mavenReleases.getName());
  }

  /**
   * NX should prevent release deploy to snapshot repo.
   */
  @Test(expected = VerificationException.class)
  public void releaseToSnapshot() throws Exception {
    mvnDeploy("testproject", "1.0", mavenSnapshots.getName());
  }

  /**
   * Sanity: deploy two subsequent releases, all should be ok.
   */
  @Test
  public void deployPlainJarRelease() throws Exception {
    mvnDeploy("testproject", "1.0", mavenReleases.getName());
    mvnDeploy("testproject", "1.1", mavenReleases.getName());

    // no G level md, not a plugin
    // but no neg check due to test ordering, plugin might be deployed already

    // A level
    verifyHashesExistAndCorrect(mavenReleases, A_MD_JAR_PATH);
    final Metadata aLevel = parse(read(mavenReleases, A_MD_JAR_PATH));
    assertThat(aLevel.getVersioning(), notNullValue());
    assertThat(aLevel.getVersioning().getVersions(), hasSize(2));
    assertThat(aLevel.getVersioning().getVersions(), hasItems("1.0", "1.1"));

    // no V level, not a snapshot
  }

  /**
   * Sanity: deploy two subsequent snapshots, all should be fine.
   */
  @Test
  public void deployPlainJarSnapshot() throws Exception {
    mvnDeploy("testproject", "1.0-SNAPSHOT", mavenSnapshots.getName());
    mvnDeploy("testproject", "1.0-SNAPSHOT", mavenSnapshots.getName());

    // no G level md, not a plugin
    // but no neg check due to test ordering, plugin might be deployed already

    // A level
    verifyHashesExistAndCorrect(mavenSnapshots, A_MD_JAR_PATH);
    final Metadata aLevel = parse(read(mavenSnapshots, A_MD_JAR_PATH));
    assertThat(aLevel.getVersioning(), notNullValue());
    assertThat(aLevel.getVersioning().getVersions(), hasSize(1));
    assertThat(aLevel.getVersioning().getVersions(), hasItems("1.0-SNAPSHOT"));

    // V level
    verifyHashesExistAndCorrect(mavenSnapshots, V_MD_JAR_PATH);
    final Metadata vLevel = parse(read(mavenSnapshots, V_MD_JAR_PATH));
    assertThat(vLevel.getVersioning(), notNullValue());
    assertThat(vLevel.getVersioning().getSnapshot(), notNullValue());
    assertThat(vLevel.getVersioning().getSnapshotVersions(), hasSize(2));
  }

  @Test
  public void deployPluginRelease() throws Exception {
    mvnDeploy("testplugin", "1.0", mavenReleases.getName());
    mvnDeploy("testplugin", "1.1", mavenReleases.getName());

    // G level
    verifyHashesExistAndCorrect(mavenReleases, G_MD_PATH);
    final Metadata gLevel = parse(read(mavenReleases, G_MD_PATH));
    assertThat(gLevel.getPlugins(), hasSize(1));

    // A level
    verifyHashesExistAndCorrect(mavenReleases, A_MD_PLUGIN_PATH);
    final Metadata aLevel = parse(read(mavenReleases, A_MD_PLUGIN_PATH));
    assertThat(aLevel.getVersioning(), notNullValue());
    assertThat(aLevel.getVersioning().getVersions(), hasSize(2));
    assertThat(aLevel.getVersioning().getVersions(), hasItems("1.0", "1.1"));

    // V level, not a snapshot
  }

  @Test
  public void deployPluginSnapshot() throws Exception {
    mvnDeploy("testplugin", "1.0-SNAPSHOT", mavenSnapshots.getName());
    mvnDeploy("testplugin", "1.0-SNAPSHOT", mavenSnapshots.getName());

    // G level
    verifyHashesExistAndCorrect(mavenSnapshots, G_MD_PATH);
    final Metadata gLevel = parse(read(mavenSnapshots, G_MD_PATH));
    assertThat(gLevel.getPlugins(), hasSize(1));

    // A level
    verifyHashesExistAndCorrect(mavenSnapshots, A_MD_PLUGIN_PATH);
    final Metadata aLevel = parse(read(mavenSnapshots, A_MD_PLUGIN_PATH));
    assertThat(aLevel.getVersioning(), notNullValue());
    assertThat(aLevel.getVersioning().getVersions(), hasSize(1));
    assertThat(aLevel.getVersioning().getVersions(), contains("1.0-SNAPSHOT"));

    // V level
    verifyHashesExistAndCorrect(mavenSnapshots, V_MD_PLUGIN_PATH);
    final Metadata vLevel = parse(read(mavenSnapshots, V_MD_PLUGIN_PATH));
    assertThat(vLevel.getVersioning(), notNullValue());
    assertThat(vLevel.getVersioning().getSnapshot(), notNullValue());
    assertThat(vLevel.getVersioning().getSnapshotVersions(), hasSize(2));
  }
}
