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
package org.sonatype.nexus.testsuite.maven.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.maven.MavenHostedFacet;
import org.sonatype.nexus.repository.maven.policy.VersionPolicy;
import org.sonatype.nexus.testsuite.maven.Maven2Client;
import org.sonatype.nexus.testsuite.maven.concurrency.generators.MavenMetadataGenerator;
import org.sonatype.sisu.goodies.common.ByteSize;

import com.google.common.base.Suppliers;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.sonatype.nexus.testsuite.maven.concurrency.generators.Generator.generatedEntity;

/**
 * Maven metadata hosted swarm: multiple clients asking, while others are deploying the SAME metadata from a hosted
 * repository, while metadata rebuild happens too.
 */
@ExamReactorStrategy(PerClass.class)
public class MavenMetadataHostedSwarmIT
    extends MavenHotspotITSupport
{
  private void hostedMetadataSwarm(final String path,
                                   final MavenMetadataGenerator generator) throws Exception
  {
    final Configuration configuration = hostedConfig(testName.getMethodName(), VersionPolicy.RELEASE);
    Repository repository = repositoryManager.create(configuration);
    final Maven2Client repositoryClient = createAdminMaven2Client(repository.getName());

    // create clients
    final List<Callable<HttpResponse>> clients = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      clients
          .add(new UriPut(repositoryClient, path, generator, Suppliers.ofInstance(ByteSize.bytes(100L)) /* unused */));
    }
    for (int i = 0; i < 15; i++) {
      clients.add(new UriGet(repositoryClient, path));
    }
    for (int i = 0; i < 2; i++) {
      clients.add(new Repeat(3, new RebuildMavenMetadata(repository.facet(MavenHostedFacet.class))));
    }

    // put initial as we don't know will GET or PUT "win"
    final HttpEntity entity = generatedEntity(generator, ByteSize.bytes(100L) /* unused */);
    HttpResponse response = repositoryClient.put(path, entity);
    assertThat(response.getStatusLine().getStatusCode(), equalTo(201));

    // except last two clients, that does not return HttpResponse code
    assertAllHttpResponseIs2xx(performSwarm(clients).subList(0, clients.size() - 2));
  }

  @Test
  public void groupLevel() throws Exception {
    Metadata metadata = new Metadata();
    metadata.setModelVersion("1.1.0");
    metadata.setGroupId("org.sonatype.nexus.test");
    Plugin plugin = new Plugin();
    plugin.setPrefix("prefix");
    plugin.setArtifactId("artifact");
    plugin.setName("Name");
    metadata.addPlugin(plugin);
    hostedMetadataSwarm("org/sonatype/nexus/test/maven-metadata.xml", new MavenMetadataGenerator(metadata));
  }

  @Test
  public void artifactLevel() throws Exception {
    Metadata metadata = new Metadata();
    metadata.setModelVersion("1.1.0");
    metadata.setGroupId("org.sonatype.nexus.test");
    metadata.setArtifactId("test-artifact");
    Versioning versioning = new Versioning();
    versioning.setLatest("1.0.0");
    versioning.setRelease("1.0.0");
    versioning.addVersion("0.0.8");
    versioning.addVersion("0.0.9");
    versioning.setLastUpdated("20150715120000");
    metadata.setVersioning(versioning);
    hostedMetadataSwarm("org/sonatype/nexus/test/test-artifact/maven-metadata.xml",
        new MavenMetadataGenerator(metadata));
  }

  @Test
  public void versionLevel() throws Exception {
    Metadata metadata = new Metadata();
    metadata.setModelVersion("1.1.0");
    metadata.setGroupId("org.sonatype.nexus.test");
    metadata.setArtifactId("test-artifact");
    metadata.setVersion("1.0-SNAPSHOT");
    Versioning versioning = new Versioning();
    Snapshot snapshot = new Snapshot();
    snapshot.setTimestamp("20150715.120000");
    snapshot.setBuildNumber(1);
    versioning.setSnapshot(snapshot);
    final List<SnapshotVersion> snapshotVersions = new ArrayList<>();

    SnapshotVersion pom = new SnapshotVersion();
    pom.setExtension("pom");
    pom.setUpdated("20150715120000");
    pom.setVersion("1.0-20150715.120000-1");
    snapshotVersions.add(pom);
    SnapshotVersion jar = new SnapshotVersion();
    jar.setExtension("jar");
    jar.setUpdated("20150715120000");
    jar.setVersion("1.0-20150715.120000-1");
    snapshotVersions.add(jar);
    SnapshotVersion javadoc = new SnapshotVersion();
    javadoc.setExtension("jar");
    javadoc.setClassifier("javadoc");
    javadoc.setUpdated("20150715120000");
    javadoc.setVersion("1.0-20150715.120000-1");
    snapshotVersions.add(javadoc);

    versioning.setSnapshotVersions(snapshotVersions);
    versioning.setLastUpdated("20150715120000");
    metadata.setVersioning(versioning);
    hostedMetadataSwarm("org/sonatype/nexus/test/test-artifact/maven-metadata.xml",
        new MavenMetadataGenerator(metadata));
  }
}
