/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.testsuite.analytics

import org.junit.Test
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration
import org.sonatype.nexus.testsuite.client.Scheduler
import org.sonatype.tests.http.server.fluent.Server
import org.sonatype.tests.http.server.jetty.behaviour.Record

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.hasSize

/**
 * Submission of events analytics related ITs.
 *
 * @since 2.8
 */
class AnalyticsSubmissionIT
    extends AnalyticsITSupport
{

  private Server analyticsServer

  private ServerRecorder analyticsServerRecorder;

  AnalyticsSubmissionIT(String nexusBundleCoordinates) {
    super(nexusBundleCoordinates)
  }

  /**
   * Verifies that events are submitted.
   */
  @Test
  void taskSubmitsToServer() {
    configureAnalytics(true, true)
    scheduler().run('SubmitTask', null)
    assertThat analyticsServerRecorder.requests, hasItem('POST /submit')
    assertThat analyticsServerRecorder.requestsContent, hasSize(1)
    assertThat analyticsServerRecorder.requestsContent[0], containsString('Content-Type: application/zip')
    assertThat analyticsServerRecorder.requestsContent[0], containsString('Content-Disposition: ')
    assertThat analyticsServerRecorder.requestsContent[0], containsString('filename="analytics')
  }

  @Override
  protected NexusBundleConfiguration configureNexus(final NexusBundleConfiguration configuration) {
    analyticsServer = Server
        .withPort(0)
        .serve("/*").withBehaviours(analyticsServerRecorder = new ServerRecorder())
        .start()

    return super.configureNexus(configuration)
        .setSystemProperty('nexus.analytics.serviceUrl', analyticsServer.url.toExternalForm())
  }

  Scheduler scheduler() {
    return client().getSubsystem(Scheduler.class)
  }

  private static class ServerRecorder
      extends Record
  {
    private List<String> requestsContent = []

    @Override
    boolean execute(final HttpServletRequest request, final HttpServletResponse response, final Map<Object, Object> ctx)
        throws Exception
    {
      requestsContent << request.inputStream.text
      return super.execute(request, response, ctx)
    }
  }

}
