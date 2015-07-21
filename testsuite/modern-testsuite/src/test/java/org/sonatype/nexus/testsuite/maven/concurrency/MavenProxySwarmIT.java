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
import org.sonatype.nexus.repository.http.HttpMethods;
import org.sonatype.nexus.repository.maven.policy.VersionPolicy;
import org.sonatype.nexus.repository.proxy.ProxyFacet;
import org.sonatype.nexus.testsuite.maven.Maven2Client;
import org.sonatype.nexus.testsuite.maven.concurrency.generators.GeneratorBehaviour;
import org.sonatype.nexus.testsuite.maven.concurrency.generators.XmlGenerator;
import org.sonatype.nexus.testsuite.maven.concurrency.generators.ZipGenerator;
import org.sonatype.sisu.goodies.common.ByteSize;
import org.sonatype.tests.http.server.fluent.Server;
import org.sonatype.tests.http.server.jetty.behaviour.PathRecorderBehaviour;

import org.apache.http.HttpResponse;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * Maven proxy swarm: multiple clients asking for SAME artifact from an empty proxy repository.
 */
@ExamReactorStrategy(PerClass.class)
public class MavenProxySwarmIT
    extends MavenHotspotITSupport
{
  private static final String RELEASE_XML_ARTIFACT_PATH = "groupId/artifactId/1.0/artifactId-1.0.xml";

  private static final String RELEASE_ZIP_ARTIFACT_PATH = "groupId/artifactId/1.0/artifactId-1.0.zip";

  private PathRecorderBehaviour pathRecorderBehaviour;

  private GeneratorBehaviour xmlArtifactGenerator;

  private GeneratorBehaviour zipArtifactGenerator;

  private Server upstream;

  @Before
  public void prepare() throws Exception {
    pathRecorderBehaviour = new PathRecorderBehaviour();
    xmlArtifactGenerator = new GeneratorBehaviour(new XmlGenerator());
    zipArtifactGenerator = new GeneratorBehaviour(new ZipGenerator());

    upstream = Server.withPort(0)
        .serve("/" + RELEASE_XML_ARTIFACT_PATH).withBehaviours(
            pathRecorderBehaviour,
            xmlArtifactGenerator
        )
        .serve("/" + RELEASE_ZIP_ARTIFACT_PATH).withBehaviours(
            pathRecorderBehaviour,
            zipArtifactGenerator
        )
        .start();
  }

  @After
  public void cleanup() throws Exception {
    if (upstream != null) {
      upstream.stop();
    }
  }

  private void proxySwarm(final String path) throws Exception {
    final Configuration configuration = proxyConfig(testName.getMethodName(),
        "http://localhost:" + upstream.getPort() + "/", VersionPolicy.RELEASE);
    Repository repository = repositoryManager.create(configuration);
    final Maven2Client repositoryClient = createAdminMaven2Client(repository.getName());

    // create clients
    final List<Callable<HttpResponse>> clients = new ArrayList<>();
    for (int i = 0; i < 15; i++) {
      clients.add(new UriGet(repositoryClient, path));
    }

    // depending on concurrency 1+ clients will go remotely (we use no locking)
    assertAllHttpResponseIs2xx(performSwarm(clients));
    assertThat(pathRecorderBehaviour.getPathsForVerb(HttpMethods.GET), hasSize(greaterThanOrEqualTo(1)));
    pathRecorderBehaviour.clear();

    // warmed cache, no remote fetch should happen anymore
    assertAllHttpResponseIs2xx(performSwarm(clients));
    assertThat(pathRecorderBehaviour.getPathsForVerb(HttpMethods.GET), hasSize(equalTo(0)));
    pathRecorderBehaviour.clear();

    repository.facet(ProxyFacet.class).invalidateProxyCaches();

    // invalidated cache, back to initial assertion
    assertAllHttpResponseIs2xx(performSwarm(clients));
    assertThat(pathRecorderBehaviour.getPathsForVerb(HttpMethods.GET), hasSize(greaterThanOrEqualTo(1)));
    pathRecorderBehaviour.clear();
  }

  @Test
  public void smallZip() throws Exception {
    zipArtifactGenerator.setContentProperties(ByteSize.kiloBytes(10), true, DateTime.now().minusHours(1), null);
    proxySwarm(RELEASE_ZIP_ARTIFACT_PATH);
  }

  @Test
  public void mediumZip() throws Exception {
    zipArtifactGenerator.setContentProperties(ByteSize.kiloBytes(500), true, DateTime.now().minusHours(1), null);
    proxySwarm(RELEASE_ZIP_ARTIFACT_PATH);
  }

  @Test
  public void largeZip() throws Exception {
    zipArtifactGenerator.setContentProperties(ByteSize.megaBytes(5), true, DateTime.now().minusHours(1), null);
    proxySwarm(RELEASE_ZIP_ARTIFACT_PATH);
  }

  @Test
  public void smallXml() throws Exception {
    xmlArtifactGenerator.setContentProperties(ByteSize.kiloBytes(1), true, DateTime.now().minusHours(1), null);
    proxySwarm(RELEASE_XML_ARTIFACT_PATH);
  }

  @Test
  public void mediumXml() throws Exception {
    xmlArtifactGenerator.setContentProperties(ByteSize.kiloBytes(10), true, DateTime.now().minusHours(1), null);
    proxySwarm(RELEASE_XML_ARTIFACT_PATH);
  }

  @Test
  public void largeXml() throws Exception {
    xmlArtifactGenerator.setContentProperties(ByteSize.kiloBytes(500), true, DateTime.now().minusHours(1), null);
    proxySwarm(RELEASE_XML_ARTIFACT_PATH);
  }
}
