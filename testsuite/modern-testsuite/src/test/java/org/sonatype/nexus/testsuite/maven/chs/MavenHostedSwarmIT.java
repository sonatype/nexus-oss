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
import java.util.concurrent.Callable;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.http.HttpMethods;
import org.sonatype.nexus.repository.maven.policy.VersionPolicy;
import org.sonatype.nexus.testsuite.maven.Maven2Client;
import org.sonatype.sisu.goodies.common.ByteSize;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.hamcrest.CoreMatchers;
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
 * Maven hosted swarm: multiple clients asking, while others are deploying the SAME artifact from a hosted repository.
 */
@ExamReactorStrategy(PerClass.class)
public class MavenHostedSwarmIT
    extends MavenHotspotITSupport
{
  @Test
  public void swarm() throws Exception {
    final Configuration configuration = hostedConfig(testName.getMethodName(), VersionPolicy.RELEASE);
    Repository repository = repositoryManager.create(configuration);
    assertThat(repository, notNullValue());
    AuthScope scope = new AuthScope(nexusUrl.getHost(), -1);
    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    credentialsProvider.setCredentials(scope, new UsernamePasswordCredentials("admin", "admin123"));
    final Maven2Client repositoryClient = new Maven2Client(HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build(), HttpClientContext.create(),
        resolveUrl(nexusUrl, "/repository/" + repository.getName() + "/").toURI());

    // create clients
    final List<Callable<HttpResponse>> clients = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      clients.add(new UriPut(repositoryClient, RELEASE_TXT_ARTIFACT_PATH));
    }
    for (int i = 0; i < 15; i++) {
      clients.add(new UriGet(repositoryClient, RELEASE_TXT_ARTIFACT_PATH));
    }

    // put initial as we dom't know will GET or PUT "win"
    HttpResponse response = repositoryClient.put(RELEASE_TXT_ARTIFACT_PATH, new StringEntity("test"));
    assertThat(response.getStatusLine().getStatusCode(), equalTo(201));

    assertAllHttpResponseIs2xx(performSwarm(clients));
  }
}
