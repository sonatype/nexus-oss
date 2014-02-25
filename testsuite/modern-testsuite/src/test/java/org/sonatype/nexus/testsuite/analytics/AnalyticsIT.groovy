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

import com.sun.jersey.api.client.UniformInterfaceException
import org.junit.Test
import org.sonatype.nexus.client.core.subsystem.content.Location
import org.sonatype.nexus.testsuite.analytics.client.Events.EventsXO
import org.sonatype.nexus.testsuite.analytics.client.Events.ExportXO
import org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.sonatype.nexus.client.core.subsystem.content.Location.repositoryLocation

/**
 * Analytics related ITs.
 *
 * @since 2.8
 */
class AnalyticsIT
extends AnalyticsITSupport
{

  private static final String AOP_POM = 'aopalliance/aopalliance/1.0/aopalliance-1.0.pom'

  private static final String AOP_CONTENT_PATH = 'content/repositories/releases' + AOP_POM

  private static final Location AOP_POM_LOCATION = repositoryLocation('releases', AOP_POM)

  AnalyticsIT(String nexusBundleCoordinates) {
    super(nexusBundleCoordinates)
  }

  /**
   * Verifies that events are collected when analytics collection is enabled.
   */
  @Test
  void whenCollectingIsEnabledEventsAreCollected() {

    configureAnalytics(true, false)
    EventsXO before = allEvents
    EventsXO after = allEvents

    assertThat after.count, is(greaterThan(before.count))
    assertThat pathsOf(after), hasItem('GET|/service/siesta/analytics/events')
  }

  /**
   * Verifies that events cannot be accessed when analytics collection is disabled.
   */
  @Test
  void whenCollectingIsDisabledEventsCannotBeRetrieved() {

    configureAnalytics(false, false)

    thrown.expect UniformInterfaceException.class
    thrown.expectMessage 'Not started';
    allEvents
  }

  /**
   * Verifies that events are exported.
   */
  //@Test TODO the returned file is some tmp file not the exported one (e.g. sonatype-work/nexus/tmp/analytics-2670231769022899054.zip vs. sonatype-work/nexus/support/analytics-2670231769022899054.zip)
  void export() {

    configureAnalytics(true, false)
    // access events a couple of times, which in turn will generate events
    allEvents
    allEvents

    ExportXO export = events.export()

    assertThat export.file, is(notNullValue())
    assertThat export.name, is(notNullValue())
    assertThat export.size, is(greaterThan(0L))

    assertThat export.file as File, FileMatchers.exists()
    // TODO open zip and check content?
  }

  /**
   * Verifies that events events about upload/download via '/content/*' are recorded.
   */
  //@Test TODO no events for accessing /content/* ?
  void uploadAndDownloadAreCollected() {

    configureAnalytics(true, false)

    content().upload(AOP_POM_LOCATION, testData().resolveFile("artifacts/" + AOP_POM))
    content().download(AOP_POM_LOCATION, new File(testIndex().getDirectory("downloads"), "aopalliance-1.0.pom"))

    EventsXO events = allEvents

    assertThat pathsOf(events), hasItems('PUT|' + AOP_CONTENT_PATH, 'GET|' + AOP_CONTENT_PATH)
  }

}
