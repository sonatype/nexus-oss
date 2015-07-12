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
package org.sonatype.nexus.testsuite.maven.conditional;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.log.LoggerLevel;
import org.sonatype.nexus.testsuite.maven.Maven2Client;
import org.sonatype.nexus.testsuite.maven.MavenITSupport;
import org.sonatype.tests.http.server.api.Behaviour;
import org.sonatype.tests.http.server.fluent.Behaviours;
import org.sonatype.tests.http.server.fluent.Server;

import com.google.common.net.HttpHeaders;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Metadata conditional request IT.
 */
@ExamReactorStrategy(PerClass.class)
public class MavenConditionalRequestIT
    extends MavenITSupport
{
  private static final String RELEASE_ARTIFACT_PATH = "group/artifact/1.0/artifact-1.0.txt";

  private static final String SNAPSHOT_ARTIFACT_PATH = "group/artifact/1.0-SNAPSHOT/artifact-1.0-20150617.120000-1.txt";

  private static final String LAST_MODIFIED_VALUE = "Wed, 17 Jun 2015 12:00:00 GMT";

  private static final String ETAG_VALUE = "\"1234567890\"";

  private Server upstream;

  @Before
  public void setupMavenDebugStorage() {
    logManager.setLoggerLevel("org.sonatype.nexus.repository.storage", LoggerLevel.DEBUG);
  }

  @Before
  public void prepare() throws Exception {
    upstream = Server.withPort(0)
        .serve("/" + RELEASE_ARTIFACT_PATH).withBehaviours(
            new Behaviour()
            {
              @Override
              public boolean execute(final HttpServletRequest request, final HttpServletResponse response,
                                     final Map<Object, Object> ctx)
                  throws Exception
              {
                response.addHeader(HttpHeaders.ETAG, ETAG_VALUE);
                response.addHeader(HttpHeaders.LAST_MODIFIED, LAST_MODIFIED_VALUE);
                return true;
              }
            },
            Behaviours.content("This is a text", "text/plain"))
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

  @Test
  public void conditionalGet() throws Exception {
    redirectProxy("maven-central", "http://localhost:" + upstream.getPort() + "/");
    Maven2Client centralClient = createAdminMaven2Client("maven-central");

    HttpResponse response;

    response = centralClient.get(RELEASE_ARTIFACT_PATH);
    EntityUtils.consume(response.getEntity());

    assertThat(response.getStatusLine().getStatusCode(), equalTo(200));

    response = centralClient.getIfNewer(RELEASE_ARTIFACT_PATH, DateUtils.parseDate(LAST_MODIFIED_VALUE));
    EntityUtils.consume(response.getEntity());

    assertThat(response.getStatusLine().getStatusCode(), equalTo(304));

    response = centralClient.getIfNoneMatch(RELEASE_ARTIFACT_PATH, ETAG_VALUE);
    EntityUtils.consume(response.getEntity());

    assertThat(response.getStatusLine().getStatusCode(), equalTo(304));
  }

  @Test
  public void conditionalGetViaGroup() throws Exception {
    redirectProxy("maven-central", "http://localhost:" + upstream.getPort() + "/");
    Maven2Client publicClient = createAdminMaven2Client("maven-public");

    HttpResponse response;

    response = publicClient.get(RELEASE_ARTIFACT_PATH);
    EntityUtils.consume(response.getEntity());

    assertThat(response.getStatusLine().getStatusCode(), equalTo(200));

    response = publicClient.getIfNewer(RELEASE_ARTIFACT_PATH, DateUtils.parseDate(LAST_MODIFIED_VALUE));
    EntityUtils.consume(response.getEntity());

    assertThat(response.getStatusLine().getStatusCode(), equalTo(304));

    response = publicClient.getIfNoneMatch(RELEASE_ARTIFACT_PATH, ETAG_VALUE);
    EntityUtils.consume(response.getEntity());

    assertThat(response.getStatusLine().getStatusCode(), equalTo(304));
  }

  @Test
  public void conditionalPut() throws Exception {
    Maven2Client snapshotsClient = createAdminMaven2Client("maven-snapshots");

    HttpResponse response;

    final String payload = "This is a payload";
    final HttpEntity payloadEntity = new StringEntity(payload);

    response = snapshotsClient.put(SNAPSHOT_ARTIFACT_PATH, payloadEntity);
    EntityUtils.consume(response.getEntity());

    assertThat(response.getStatusLine().getStatusCode(), equalTo(201));

    response = snapshotsClient.get(SNAPSHOT_ARTIFACT_PATH);
    EntityUtils.consume(response.getEntity());

    assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
    assertThat(response.getFirstHeader(HttpHeaders.ETAG), notNullValue());
    final String etag = response.getFirstHeader(HttpHeaders.ETAG).getValue();
    assertThat(etag, startsWith("\"{SHA1{"));

    response = snapshotsClient.putIfMatches(SNAPSHOT_ARTIFACT_PATH, "wrongEtag", payloadEntity);
    EntityUtils.consume(response.getEntity());

    assertThat(response.getStatusLine().getStatusCode(), equalTo(412));

    response = snapshotsClient.putIfMatches(SNAPSHOT_ARTIFACT_PATH, etag, payloadEntity);
    EntityUtils.consume(response.getEntity());

    assertThat(response.getStatusLine().getStatusCode(), equalTo(201));
  }
}
