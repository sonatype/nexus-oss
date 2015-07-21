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
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.sonatype.nexus.log.LoggerLevel;
import org.sonatype.nexus.repository.maven.MavenHostedFacet;
import org.sonatype.nexus.testsuite.maven.Maven2Client;
import org.sonatype.nexus.testsuite.maven.MavenITSupport;
import org.sonatype.nexus.testsuite.maven.concurrency.generators.Generator;
import org.sonatype.sisu.goodies.common.ByteSize;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.nexus.testsuite.maven.concurrency.generators.Generator.generatedEntity;

/**
 * Maven Concurrency Hotspot IT support.
 */
public abstract class MavenHotspotITSupport
    extends MavenITSupport
{
  @Before
  public void setupMavenDebugStorage() {
    logManager.setLoggerLevel("org.sonatype.nexus.repository.storage", LoggerLevel.DEBUG);
  }

  protected <T> List<T> performSwarm(final List<Callable<T>> clients) throws Exception {
    final ExecutorService executorService = Executors
        .newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    final CountDownLatch startLatch = new CountDownLatch(1);
    final CountDownLatch endLatch = new CountDownLatch(clients.size());

    final Map<Integer, Future<T>> futures = Maps.newLinkedHashMap();
    int i = 0;
    for (Callable<T> client : clients) {
      final Future<T> future = executorService.submit(new ControlledCallable(startLatch, endLatch, client));
      futures.put(i++, future);
    }

    // let it loose and wait for them to finish
    startLatch.countDown();
    endLatch.await(30, TimeUnit.SECONDS);
    executorService.shutdown();
    executorService.awaitTermination(30, TimeUnit.SECONDS);

    return Lists.newArrayList(
        Iterables.transform(futures.values(), new Function<Future<T>, T>()
        {
          @Override
          public T apply(final Future<T> input) {
            try {
              return input.get();
            }
            catch (Exception e) {
              throw Throwables.propagate(e);
            }
          }
        })
    );
  }

  private static class ControlledCallable<T>
      implements Callable<T>
  {
    private final CountDownLatch startLatch;

    private final CountDownLatch endLatch;

    private final Callable<T> controlled;

    public ControlledCallable(final CountDownLatch startLatch,
                              final CountDownLatch endLatch,
                              final Callable<T> controlled)
    {
      this.startLatch = startLatch;
      this.endLatch = endLatch;
      this.controlled = controlled;
    }

    @Override
    public T call() throws Exception {
      startLatch.await();
      try {
        return controlled.call();
      }
      finally {
        endLatch.countDown();
      }
    }
  }

  // == clients

  protected void assertAllHttpResponse(final List<HttpResponse> responses, final Matcher<HttpResponse> matcher) {
    for (HttpResponse response : responses) {
      assertThat(response, matcher);
    }
  }

  protected void assertAllHttpResponseIs2xx(final List<HttpResponse> responses) {
    assertAllHttpResponse(responses, new BaseMatcher<HttpResponse>()
    {
      @Override
      public boolean matches(final Object o) {
        if (o instanceof HttpResponse) {
          HttpResponse r = (HttpResponse) o;
          return r.getStatusLine().getStatusCode() >= 200 && r.getStatusLine().getStatusCode() <= 299;
        }
        return false;
      }

      @Override
      public void describeTo(final Description description) {
        description.appendText("HTTP 2xx response code");
      }
    });
  }

  /**
   * A simple client performing a GET against single URL.
   */
  public static class UriGet
      implements Callable<HttpResponse>
  {
    private final Maven2Client client;

    private final String uri;

    public UriGet(final Maven2Client client, final String uri) {
      this.client = checkNotNull(client);
      this.uri = checkNotNull(uri);
    }

    @Override
    public HttpResponse call() throws Exception {
      HttpResponse response = client.get(uri);
      checkState(response.getEntity() != null);
      // consume by actually reading the stream
      ByteStreams.copy(response.getEntity().getContent(), ByteStreams.nullOutputStream());
      EntityUtils.consume(response.getEntity());
      return response;
    }
  }

  /**
   * A simple client performing a PUT against single URL.
   */
  public static class UriPut
      implements Callable<HttpResponse>
  {
    private final Maven2Client client;

    private final String uri;

    private final Generator generator;

    private final Supplier<ByteSize> lengthSupplier;

    public UriPut(final Maven2Client client,
                  final String uri,
                  final Generator generator,
                  final Supplier<ByteSize> lengthSupplier)
    {
      this.client = checkNotNull(client);
      this.uri = checkNotNull(uri);
      this.generator = checkNotNull(generator);
      this.lengthSupplier = checkNotNull(lengthSupplier);
    }

    @Override
    public HttpResponse call() throws Exception {
      final HttpEntity entity = generatedEntity(generator, lengthSupplier.get());
      HttpResponse response = client.put(uri, entity);
      EntityUtils.consume(response.getEntity());
      return response;
    }
  }

  /**
   * Repeat.
   */
  public static class Repeat<V>
      implements Callable<List<V>>
  {
    private final int count;

    private final Callable<V> delegate;

    public Repeat(final int count, final Callable<V> delegate) {
      checkArgument(count > 0);
      this.count = count;
      this.delegate = checkNotNull(delegate);
    }

    @Override
    public List<V> call() throws Exception {
      final List<V> result = new ArrayList<>(count);
      for (int i = 0; i < count; i++) {
        result.add(delegate.call());
      }
      return result;
    }
  }

  /**
   * A simple client rebuilding Maven2 metadata using blocking execution.
   */
  public static class RebuildMavenMetadata
      implements Callable<HttpResponse>
  {
    private final MavenHostedFacet mavenHostedFacet;

    public RebuildMavenMetadata(final MavenHostedFacet mavenHostedFacet) {
      this.mavenHostedFacet = checkNotNull(mavenHostedFacet);
    }

    @Override
    public HttpResponse call() throws Exception {
      mavenHostedFacet.rebuildMetadata(null, null, null);
      return null;
    }
  }
}
