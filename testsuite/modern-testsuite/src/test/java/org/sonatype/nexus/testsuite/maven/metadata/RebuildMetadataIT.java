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
package org.sonatype.nexus.testsuite.maven.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.inject.Inject;

import org.sonatype.nexus.common.hash.HashAlgorithm;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.maven.MavenFacet;
import org.sonatype.nexus.repository.maven.MavenHostedFacet;
import org.sonatype.nexus.repository.maven.MavenPath;
import org.sonatype.nexus.repository.maven.MavenPath.HashType;
import org.sonatype.nexus.repository.maven.internal.maven2.Maven2Format;
import org.sonatype.nexus.repository.util.TypeTokens;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.StringPayload;
import org.sonatype.nexus.testsuite.maven.MavenITSupport;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.io.CharStreams;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

/**
 * Metadata rebuild IT.
 */
@ExamReactorStrategy(PerClass.class)
public class RebuildMetadataIT
    extends MavenITSupport
{
  @Inject
  private RepositoryManager repositoryManager;

  private boolean deployed = false;

  private Repository mavenSnapshots;

  private MavenFacet mavenFacet;

  private MavenHostedFacet mavenHostedFacet;

  private MetadataXpp3Reader reader = new MetadataXpp3Reader();

  @Before
  public void prepare() throws Exception {
    mavenSnapshots = repositoryManager.get("maven-snapshots");
    mavenFacet = mavenSnapshots.facet(MavenFacet.class);
    mavenHostedFacet = mavenSnapshots.facet(MavenHostedFacet.class);

    // HACK: deploy once two times
    if (!deployed) {
      deployed = true;
      mvnDeploy("testproject", mavenSnapshots.getName());
      mvnDeploy("testplugin", mavenSnapshots.getName());
    }
  }

  private void write(final String path, final Payload payload) throws IOException {
    final MavenPath mavenPath = mavenFacet.getMavenPathParser().parsePath(path);
    mavenFacet.put(mavenPath, payload);
  }

  private Content read(final String path) throws IOException {
    final MavenPath mavenPath = mavenFacet.getMavenPathParser().parsePath(path);
    return mavenFacet.get(mavenPath);
  }

  private Metadata parse(final Content content) throws Exception {
    assertThat(content, notNullValue());
    try (InputStream is = content.openInputStream()) {
      return reader.read(is);
    }
  }

  private void verifyHashesExistAndCorrect(final String path) throws Exception {
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

  @Test
  public void rebuildMetadataWholeRepository() throws Exception {
    final String gMetadataPath = "/org/sonatype/nexus/testsuite/maven-metadata.xml";
    final String aMetadataPath = "/org/sonatype/nexus/testsuite/testproject/maven-metadata.xml";
    final String vMetadataPath = "/org/sonatype/nexus/testsuite/testproject/1.0-SNAPSHOT/maven-metadata.xml";

    // mvnDeploy did happen, let's corrupt some of those
    write(gMetadataPath, new StringPayload("rubbish", Maven2Format.METADATA_CONTENT_TYPE));
    write(gMetadataPath + ".sha1", new StringPayload("aaa", Maven2Format.CHECKSUM_CONTENT_TYPE));

    mavenHostedFacet.rebuildMetadata(null, null, null);

    verifyHashesExistAndCorrect(gMetadataPath);
    final Metadata gLevel = parse(read(gMetadataPath));
    assertThat(gLevel.getPlugins(), hasSize(1));

    verifyHashesExistAndCorrect(aMetadataPath);
    final Metadata aLevel = parse(read(aMetadataPath));
    assertThat(aLevel.getVersioning(), notNullValue());
    assertThat(aLevel.getVersioning().getVersions(), hasSize(1));
    assertThat(aLevel.getVersioning().getVersions(), contains("1.0-SNAPSHOT"));

    verifyHashesExistAndCorrect(vMetadataPath);
    final Metadata vLevel = parse(read(vMetadataPath));
    assertThat(vLevel.getVersioning(), notNullValue());
    assertThat(vLevel.getVersioning().getSnapshot(), notNullValue());
    assertThat(vLevel.getVersioning().getSnapshotVersions(), hasSize(2));
  }

  @Test
  public void rebuildMetadataGroup() throws Exception {
    final String gMetadataPath = "/org/sonatype/nexus/testsuite/maven-metadata.xml";
    final String aMetadataPath = "/org/sonatype/nexus/testsuite/testproject/maven-metadata.xml";
    final String vMetadataPath = "/org/sonatype/nexus/testsuite/testproject/1.0-SNAPSHOT/maven-metadata.xml";

    // mvnDeploy did happen, let's corrupt some of those
    write(gMetadataPath, new StringPayload("rubbish", Maven2Format.METADATA_CONTENT_TYPE));
    write(gMetadataPath + ".sha1", new StringPayload("aaa", Maven2Format.CHECKSUM_CONTENT_TYPE));

    mavenHostedFacet.rebuildMetadata("org.sonatype.nexus.testsuite", null, null); // testproject groupId!

    verifyHashesExistAndCorrect(gMetadataPath);
    final Metadata gLevel = parse(read(gMetadataPath));
    assertThat(gLevel.getPlugins(), hasSize(1));

    verifyHashesExistAndCorrect(aMetadataPath);
    final Metadata aLevel = parse(read(aMetadataPath));
    assertThat(aLevel.getVersioning(), notNullValue());
    assertThat(aLevel.getVersioning().getVersions(), hasSize(1));
    assertThat(aLevel.getVersioning().getVersions(), contains("1.0-SNAPSHOT"));

    verifyHashesExistAndCorrect(vMetadataPath);
    final Metadata vLevel = parse(read(vMetadataPath));
    assertThat(vLevel.getVersioning(), notNullValue());
    assertThat(vLevel.getVersioning().getSnapshot(), notNullValue());
    assertThat(vLevel.getVersioning().getSnapshotVersions(), hasSize(2));
  }
}
