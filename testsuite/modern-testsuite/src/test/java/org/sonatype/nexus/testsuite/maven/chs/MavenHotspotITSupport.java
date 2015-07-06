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

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.sonatype.nexus.log.LogManager;
import org.sonatype.nexus.log.LoggerLevel;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.maven.tasks.RebuildMaven2MetadataTask;
import org.sonatype.nexus.repository.maven.tasks.RebuildMaven2MetadataTaskDescriptor;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskScheduler;
import org.sonatype.nexus.testsuite.maven.Maven2Client;
import org.sonatype.nexus.testsuite.maven.MavenITSupport;
import org.sonatype.tests.http.server.fluent.Server;
import org.sonatype.tests.http.server.jetty.behaviour.PathRecorderBehaviour;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.ops4j.pax.exam.Option;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * Maven Concurrency Hotspot IT support.
 */
public abstract class MavenHotspotITSupport
    extends MavenITSupport
{
  @org.ops4j.pax.exam.Configuration
  public static Option[] configureNexus() {
    return options(nexusDistribution("org.sonatype.nexus.assemblies", "nexus-base-template"),
        // set start-level to just after the nexus edition has been installed so we can re-use its dependencies
        mavenBundle("org.sonatype.http-testing-harness", "server-provider").versionAsInProject().startLevel(101)
    );
  }

  protected static final String RELEASE_XML_ARTIFACT_PATH = "groupId/artifactId/1.0/artifactId-1.0.xml";

  protected static final String RELEASE_ZIP_ARTIFACT_PATH = "groupId/artifactId/1.0/artifactId-1.0.zip";

  protected static final String RELEASE_TXT_ARTIFACT_PATH = "groupId/artifactId/1.0/artifactId-1.0.txt";

  @Rule
  public TestName testName = new TestName();

  @Inject
  protected RepositoryManager repositoryManager;

  @Inject
  private LogManager logManager;

  protected PathRecorderBehaviour pathRecorderBehaviour;

  protected GeneratorBehaviour xmlArtifactGenerator;

  protected GeneratorBehaviour zipArtifactGenerator;

  protected GeneratorBehaviour txtArtifactGenerator;

  protected Server upstream;

  @Before
  public void setupMavenDebugStorage() {
    logManager.setLoggerLevel("org.sonatype.nexus.repository.storage", LoggerLevel.DEBUG);
  }

  @Before
  public void prepare() throws Exception {
    pathRecorderBehaviour = new PathRecorderBehaviour();
    xmlArtifactGenerator = new GeneratorBehaviour(new XmlGenerator());
    zipArtifactGenerator = new GeneratorBehaviour(new ZipGenerator());
    txtArtifactGenerator = new GeneratorBehaviour(new TextGenerator());

    upstream = Server.withPort(0)
        .serve("/" + RELEASE_XML_ARTIFACT_PATH).withBehaviours(
            pathRecorderBehaviour,
            xmlArtifactGenerator
        )
        .serve("/" + RELEASE_ZIP_ARTIFACT_PATH).withBehaviours(
            pathRecorderBehaviour,
            zipArtifactGenerator
        )
        .serve("/" + RELEASE_TXT_ARTIFACT_PATH).withBehaviours(
            pathRecorderBehaviour,
            txtArtifactGenerator
        )
        .start();
  }

  @After
  public void cleanup()
      throws Exception
  {
    if (upstream != null) {
      upstream.stop();
    }
  }

  protected <T> List<T> performSwarm(final List<? extends Callable<T>> clients) throws Exception {
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

    public UriPut(final Maven2Client client, final String uri) {
      this.client = checkNotNull(client);
      this.uri = checkNotNull(uri);
    }

    @Override
    public HttpResponse call() throws Exception {
      HttpResponse response = client.put(uri, new StringEntity("test"));
      EntityUtils.consume(response.getEntity());
      return response;
    }
  }

  /**
   * A simple client rebuilding Maven2 metadata using blocking execution.
   */
  public static class RebuildMavenMetadata
      implements Callable<HttpResponse>
  {
    private final TaskScheduler taskScheduler;

    private final String repositoryName;

    public RebuildMavenMetadata(final TaskScheduler taskScheduler, final String repositoryName) {
      this.taskScheduler = checkNotNull(taskScheduler);
      this.repositoryName = checkNotNull(repositoryName);
    }

    @Override
    public HttpResponse call() throws Exception {
      TaskConfiguration configuration = taskScheduler.createTaskConfigurationInstance(RebuildMaven2MetadataTask.class);
      configuration.setString(RebuildMaven2MetadataTaskDescriptor.REPOSITORY_NAME_FIELD_ID, repositoryName);
      taskScheduler.submit(configuration).getCurrentState().getFuture().get();
      return null;
    }
  }
}
