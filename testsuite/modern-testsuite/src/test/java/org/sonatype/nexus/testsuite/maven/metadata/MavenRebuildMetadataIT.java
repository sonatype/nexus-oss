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
package org.sonatype.nexus.testsuite.maven.metadata;

import javax.inject.Inject;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.maven.MavenHostedFacet;
import org.sonatype.nexus.repository.maven.internal.maven2.Maven2MimeRulesSource;
import org.sonatype.nexus.repository.view.payloads.StringPayload;
import org.sonatype.nexus.testsuite.maven.MavenITSupport;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

/**
 * Metadata rebuild IT.
 */
@ExamReactorStrategy(PerClass.class)
public class MavenRebuildMetadataIT
    extends MavenITSupport
{
  @Inject
  private RepositoryManager repositoryManager;

  private boolean deployed = false;

  private Repository mavenSnapshots;

  private MavenHostedFacet mavenHostedFacet;

  @Before
  public void prepare() throws Exception {
    mavenSnapshots = repositoryManager.get("maven-snapshots");
    mavenHostedFacet = mavenSnapshots.facet(MavenHostedFacet.class);

    // HACK: deploy once two times
    if (!deployed) {
      deployed = true;
      mvnDeploy("testproject", "1.0-SNAPSHOT", mavenSnapshots.getName());
      mvnDeploy("testplugin", "1.0-SNAPSHOT", mavenSnapshots.getName());
    }
  }

  @Test
  public void rebuildMetadataWholeRepository() throws Exception {
    final String gMetadataPath = "/org/sonatype/nexus/testsuite/maven-metadata.xml";
    final String aMetadataPath = "/org/sonatype/nexus/testsuite/testproject/maven-metadata.xml";
    final String vMetadataPath = "/org/sonatype/nexus/testsuite/testproject/1.0-SNAPSHOT/maven-metadata.xml";

    // mvnDeploy did happen, let's corrupt some of those
    write(mavenSnapshots, gMetadataPath, new StringPayload("rubbish", Maven2MimeRulesSource.METADATA_TYPE));

    mavenHostedFacet.rebuildMetadata(null, null, null);

    verifyHashesExistAndCorrect(mavenSnapshots, gMetadataPath);
    final Metadata gLevel = parseMetadata(read(mavenSnapshots, gMetadataPath));
    assertThat(gLevel.getPlugins(), hasSize(1));

    verifyHashesExistAndCorrect(mavenSnapshots, aMetadataPath);
    final Metadata aLevel = parseMetadata(read(mavenSnapshots, aMetadataPath));
    assertThat(aLevel.getVersioning(), notNullValue());
    assertThat(aLevel.getVersioning().getVersions(), hasSize(1));
    assertThat(aLevel.getVersioning().getVersions(), contains("1.0-SNAPSHOT"));

    verifyHashesExistAndCorrect(mavenSnapshots, vMetadataPath);
    final Metadata vLevel = parseMetadata(read(mavenSnapshots, vMetadataPath));
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
    write(mavenSnapshots, gMetadataPath, new StringPayload("rubbish", Maven2MimeRulesSource.METADATA_TYPE));

    mavenHostedFacet.rebuildMetadata("org.sonatype.nexus.testsuite", null, null); // testproject groupId!

    verifyHashesExistAndCorrect(mavenSnapshots, gMetadataPath);
    final Metadata gLevel = parseMetadata(read(mavenSnapshots, gMetadataPath));
    assertThat(gLevel.getPlugins(), hasSize(1));

    verifyHashesExistAndCorrect(mavenSnapshots, aMetadataPath);
    final Metadata aLevel = parseMetadata(read(mavenSnapshots, aMetadataPath));
    assertThat(aLevel.getVersioning(), notNullValue());
    assertThat(aLevel.getVersioning().getVersions(), hasSize(1));
    assertThat(aLevel.getVersioning().getVersions(), contains("1.0-SNAPSHOT"));

    verifyHashesExistAndCorrect(mavenSnapshots, vMetadataPath);
    final Metadata vLevel = parseMetadata(read(mavenSnapshots, vMetadataPath));
    assertThat(vLevel.getVersioning(), notNullValue());
    assertThat(vLevel.getVersioning().getSnapshot(), notNullValue());
    assertThat(vLevel.getVersioning().getSnapshotVersions(), hasSize(2));
  }
}
