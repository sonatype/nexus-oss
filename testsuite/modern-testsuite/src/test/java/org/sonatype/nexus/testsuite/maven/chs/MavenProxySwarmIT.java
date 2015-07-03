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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.sonatype.nexus.repository.http.HttpMethods;
import org.sonatype.nexus.repository.http.HttpStatus;
import org.sonatype.sisu.goodies.common.ByteSize;

import com.google.common.collect.Maps;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

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
  private static final int CLIENTS = 5;

  private ExecutorService executorService = Executors.newFixedThreadPool(CLIENTS);

  @Test
  public void swarm() throws Exception {
    // deliver me a 10MB zip file
    zipArtifactGenerator.setContentProperties(ByteSize.megaBytes(10L), true, DateTime.now().minusHours(1), null);

    performSwarm();

    // depending on concurrency 1+ clients will go remotely (we use no locking)
    assertThat(pathRecorderBehaviour.getPathsForVerb(HttpMethods.GET), hasSize(greaterThan(1)));
    pathRecorderBehaviour.clear();

    performSwarm();

    // warmed cache, no remote fetch should happen anymore
    assertThat(pathRecorderBehaviour.getPathsForVerb(HttpMethods.GET), hasSize(equalTo(0)));
  }

  private void performSwarm() throws Exception {
    final CountDownLatch startLatch = new CountDownLatch(1);
    final CountDownLatch endLatch = new CountDownLatch(CLIENTS);

    final Map<Integer, Future<HttpResponse>> futures = Maps.newHashMap();
    for (int i = 0; i < CLIENTS; i++) {
      final Future<HttpResponse> future = executorService
          .submit(new MClient(startLatch, endLatch, RELEASE_ZIP_ARTIFACT_PATH));
      futures.put(i, future);
    }

    // let it loose and wait for them to finish
    startLatch.countDown();
    endLatch.await();

    // all of them should succeed
    for (int i = 0; i < CLIENTS; i++) {
      final Future<HttpResponse> future = futures.get(i);
      assertThat(future.get().getStatusLine().getStatusCode(), equalTo(HttpStatus.OK));
    }
  }

  public class MClient
      implements Callable<HttpResponse>
  {
    private final CountDownLatch startLatch;

    private final CountDownLatch endLatch;

    private final String uri;

    public MClient(final CountDownLatch startLatch, final CountDownLatch endLatch, final String uri) {
      this.startLatch = startLatch;
      this.endLatch = endLatch;
      this.uri = uri;
    }

    @Override
    public HttpResponse call() throws Exception {
      startLatch.await();
      try {
        HttpResponse response = repositoryClient.get(uri);
        EntityUtils.consume(response.getEntity());
        return response;
      }
      finally {
        endLatch.countDown();
      }
    }
  }
}
