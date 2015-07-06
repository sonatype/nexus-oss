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
package org.sonatype.nexus.testsuite.maven.chs;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.http.HttpMethods;
import org.sonatype.nexus.repository.maven.policy.VersionPolicy;
import org.sonatype.nexus.testsuite.maven.Maven2Client;
import org.sonatype.sisu.goodies.common.ByteSize;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

/**
 * Maven proxy swarm: multiple clients asking for SAME artifact from an empty proxy repository.
 */
@ExamReactorStrategy(PerClass.class)
public class MavenProxySwarmIT
    extends MavenHotspotITSupport
{
  @Test
  public void getSame() throws Exception {
    final Configuration configuration = proxyConfig(testName.getMethodName(),
        "http://localhost:" + upstream.getPort() + "/", VersionPolicy.RELEASE);
    Repository repository = repositoryManager.create(configuration);
    assertThat(repository, notNullValue());
    final Maven2Client repositoryClient = new Maven2Client(HttpClients.custom().build(), HttpClientContext.create(),
        resolveUrl(nexusUrl, "/repository/" + repository.getName() + "/").toURI());

    // deliver me a 10MB zip file
    zipArtifactGenerator.setContentProperties(ByteSize.megaBytes(10L), true, DateTime.now().minusHours(1), null);

    // create clients
    final List<UriGet> clients = new ArrayList<>();
    for (int i = 0; i < 15; i++) {
      clients.add(new UriGet(repositoryClient, RELEASE_ZIP_ARTIFACT_PATH));
    }

    assertAllHttpResponseIs2xx(performSwarm(clients));

    // depending on concurrency 1+ clients will go remotely (we use no locking)
    assertThat(pathRecorderBehaviour.getPathsForVerb(HttpMethods.GET), hasSize(greaterThan(1)));
    pathRecorderBehaviour.clear();

    assertAllHttpResponseIs2xx(performSwarm(clients));

    // warmed cache, no remote fetch should happen anymore
    assertThat(pathRecorderBehaviour.getPathsForVerb(HttpMethods.GET), hasSize(equalTo(0)));
  }
}
