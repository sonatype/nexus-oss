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
package org.sonatype.nexus.testsuite.analytics.client

import groovy.transform.ToString
import org.sonatype.nexus.client.core.subsystem.SiestaClient
import org.sonatype.nexus.testsuite.analytics.client.Events.EventDataXO
import org.sonatype.nexus.testsuite.analytics.client.Events.EventsXO
import org.sonatype.nexus.testsuite.analytics.client.Events.ExportXO

import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.QueryParam

/**
 * Analytics Events client.
 *
 * @since 2.8
 */
@Path("/service/siesta/analytics/events")
interface Events
extends SiestaClient
{

  @GET
  EventsXO get(@QueryParam('start') int start, @QueryParam('limit') int limit)

  @POST
  @Path('/export')
  ExportXO export()

  @ToString(includePackage = false, includeNames = true)
  static class EventsXO
  {
    Integer count
    Integer total
    List<EventDataXO> events
  }

  @ToString(includePackage = false, includeNames = true)
  static class EventDataXO
  {
    String type
    Long timestamp
    String userId
    String sessionId
    Map<String, Object> attributes
  }

  @ToString(includePackage = false, includeNames = true)
  static class ExportXO
  {
    String file
    String name
    Long size
  }

}
