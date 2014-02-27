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
import org.sonatype.nexus.testsuite.analytics.client.Events.EventsXO
import org.sonatype.nexus.testsuite.analytics.client.Events.ExportXO
import org.sonatype.sisu.litmus.testsupport.hamcrest.FileMatchers

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

/**
 * Analytics related ITs.
 *
 * @since 2.8
 */
class AnalyticsIT
extends AnalyticsITSupport
{

  AnalyticsIT(String nexusBundleCoordinates) {
    super(nexusBundleCoordinates)
  }

  /**
   * Verifies that events are collected when analytics collection is enabled.
   */
  @Test
  void whenCollectingIsEnabledEventsAreCollected() {

    configureAnalytics(true, false)
    EventsXO before = getAllEvents()
    EventsXO after = getAllEvents()

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
    thrown.expectMessage 'Not started'
    getAllEvents()
  }

  /**
   * Verifies that events are exported.
   */
  @Test
  void export() {

    configureAnalytics(true, false)
    // access events a couple of times, which in turn will generate events
    getAllEvents()
    getAllEvents()

    ExportXO export = events.export()

    assertThat export.file, is(notNullValue())
    assertThat export.name, is(notNullValue())
    assertThat export.size, is(greaterThan(0L))

    assertThat export.file as File, FileMatchers.exists()
  }

}
