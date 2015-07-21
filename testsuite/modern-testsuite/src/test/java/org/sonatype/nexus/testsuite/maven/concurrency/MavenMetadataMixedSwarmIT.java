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
import org.sonatype.nexus.repository.maven.policy.VersionPolicy;
import org.sonatype.nexus.testsuite.maven.Maven2Client;
import org.sonatype.nexus.testsuite.maven.concurrency.generators.GeneratorBehaviour;
import org.sonatype.nexus.testsuite.maven.concurrency.generators.MavenMetadataGenerator;
import org.sonatype.sisu.goodies.common.ByteSize;
import org.sonatype.tests.http.server.fluent.Server;

import com.google.common.base.Suppliers;
import org.apache.http.HttpResponse;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Maven metadata swarm: multiple clients asking via group, while others are deploying the SAME metadata into a hosted
 * repository, and there is a proxy involved too. In difference to {@link MavenMixedSwarmIT}, this one exercises
 * group logic too.
 */
@ExamReactorStrategy(PerClass.class)
public class MavenMetadataMixedSwarmIT
    extends MavenHotspotITSupport
{
  private static final String METADATA_PATH = "org/sonatype/nexus/test/maven-metadata.xml";

  private Metadata metadata;

  private GeneratorBehaviour metadataGenerator;

  private Server upstream;

  @Before
  public void prepare() throws Exception {
    metadata = new Metadata();
    metadata.setModelVersion("1.1.0");
    metadata.setGroupId("org.sonatype.nexus.test");
    Plugin plugin = new Plugin();
    plugin.setPrefix("prefix");
    plugin.setArtifactId("artifact");
    plugin.setName("Name");
    metadata.addPlugin(plugin);

    metadataGenerator = new GeneratorBehaviour(new MavenMetadataGenerator(metadata));
    upstream = Server.withPort(0)
        .serve("/" + METADATA_PATH).withBehaviours(
            metadataGenerator
        )
        .start();
  }

  private void metadataSwarm(final String path,
                             final MavenMetadataGenerator generator) throws Exception
  {
    final Configuration hostedConfiguration = hostedConfig(testName.getMethodName() + "-hosted", VersionPolicy.RELEASE);
    Repository hosted = repositoryManager.create(hostedConfiguration);
    final Maven2Client hostedRepositoryClient = createAdminMaven2Client(hosted.getName());

    final Configuration proxyConfiguration = proxyConfig(testName.getMethodName() + "-proxy",
        "http://localhost:" + upstream.getPort() + "/", VersionPolicy.RELEASE);
    Repository proxy = repositoryManager.create(proxyConfiguration);

    final Configuration groupConfiguration = groupConfig(testName.getMethodName() + "-group",
        proxy.getName(), hosted.getName());
    Repository group = repositoryManager.create(groupConfiguration);
    final Maven2Client groupRepositoryClient = createAdminMaven2Client(group.getName());

    // create clients
    final List<Callable<HttpResponse>> clients = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      clients.add(
          new UriPut(hostedRepositoryClient, path, generator, Suppliers.ofInstance(ByteSize.bytes(100L)) /* unused */));
    }
    for (int i = 0; i < 15; i++) {
      clients.add(new UriGet(groupRepositoryClient, path));
    }

    assertAllHttpResponseIs2xx(performSwarm(clients));
  }

  @Test
  public void mavenMetadata() throws Exception {
    metadataSwarm(METADATA_PATH, new MavenMetadataGenerator(metadata));
  }
}
