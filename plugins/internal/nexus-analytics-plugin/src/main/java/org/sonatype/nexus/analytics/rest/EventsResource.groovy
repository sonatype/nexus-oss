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

package org.sonatype.nexus.analytics.rest

import com.yammer.metrics.annotation.Timed
import io.kazuki.v0.store.keyvalue.KeyValuePair
import io.kazuki.v0.store.keyvalue.KeyValueIterable
import org.apache.shiro.authz.annotation.RequiresPermissions
import org.sonatype.nexus.analytics.EventData
import org.sonatype.nexus.analytics.EventExporter
import org.sonatype.nexus.analytics.EventRecorder
import org.sonatype.nexus.analytics.EventStore
import org.sonatype.nexus.analytics.internal.SubmitTask
import org.sonatype.nexus.scheduling.NexusScheduler
import org.sonatype.sisu.goodies.common.ComponentSupport
import org.sonatype.sisu.siesta.common.Resource

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

import static com.google.common.base.Preconditions.checkNotNull

/**
 * Analytics events resource.
 *
 * @since 2.8
 */
@Named
@Singleton
@Path(EventsResource.RESOURCE_URI)
class EventsResource
    extends ComponentSupport
    implements Resource
{
  static final String RESOURCE_URI = '/analytics/events'

  // FIXME: Sort out facade interface

  private final EventRecorder eventRecorder

  private final EventStore eventStore

  private final EventExporter eventExporter

  private final NexusScheduler nexusScheduler

  private final Provider<SubmitTask> submitTaskFactory

  @Inject
  EventsResource(final EventRecorder eventRecorder,
                 final EventStore eventStore,
                 final EventExporter eventExporter,
                 final NexusScheduler nexusScheduler,
                 final Provider<SubmitTask> submitTaskFactory)
  {
    this.eventRecorder = checkNotNull(eventRecorder)
    this.eventStore = checkNotNull(eventStore)
    this.eventExporter = checkNotNull(eventExporter)
    this.nexusScheduler = checkNotNull(nexusScheduler)
    this.submitTaskFactory = checkNotNull(submitTaskFactory)
  }

  /**
   * List events in range.
   *
   * @param start Starting index
   * @param limit Limit number of events, or -1 for unlimited.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RequiresPermissions('nexus:analytics')
  @Timed
  Map list(final @QueryParam('start') @DefaultValue('0') int start,
           final @QueryParam('limit') @DefaultValue('-1') int limit)
  {
    log.debug "Listing events; start=$start limit=$limit"

    List<EventData> events = []
    def count = 0

    KeyValueIterable<KeyValuePair<EventData>> eventEntries = null
    try {
      eventEntries = (eventStore.iterator(start, limit))

      for (KeyValuePair<EventData> entry : eventEntries) {
        def event = entry.value

        // strip non-anonymized sensitive data
        if (event.sessionId) {
          event.sessionId = 'stripped'
        }
        // NOTE: userId is not anonymized here, but will be in exported data

        events << event
        count++
        if (limit > 0 && count >= limit) {
          break
        }
      }
    } finally {
      if (eventEntries != null) {
        eventEntries.close()
      }
    }

    return [
        count: events.size(),
        // total number of records (approximate) needed for paging support
        total: eventStore.approximateSize(),
        events: events
    ]
  }

  /**
   * Clear all event data.
   */
  @DELETE
  @RequiresPermissions('nexus:analytics')
  @Timed
  void clear() {
    eventStore.clear()
  }

  /**
   * Submit all event data.
   */
  @POST
  @Path('submit')
  @RequiresPermissions('nexus:analytics')
  @Timed
  void submit() {
    def task = submitTaskFactory.get()
    def scheduled = nexusScheduler.submit('Manually submit analytics events', task)
    log.debug("Scheduled task: $scheduled")
  }

  /**
   * Export all event data.
   */
  @POST
  @Path('export')
  @Produces(MediaType.APPLICATION_JSON)
  @RequiresPermissions('nexus:analytics')
  @Timed
  Map export() {
    // FIXME: Need to resolve how to deal with this, large # of events could take a while to export
    // FIXME: And we may need to provide another way to express this to the user and/or allow them to download the file

    def file = eventExporter.export(false) // no drop
    return [
        file: file.absolutePath,
        name: file.name,
        size: file.size()
    ]
  }

  // TODO: Add 'download' protected by authtoken, see SupportZipResource

  /**
   * Append events.  This requires no permissions.
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Timed
  void append(final List<EventData> events) {
    if (!eventRecorder.enabled) {
      log.warn 'Ignoring events; recording is disabled'
      return
    }
    events.each {
      eventRecorder.record(it)
    }
  }
}