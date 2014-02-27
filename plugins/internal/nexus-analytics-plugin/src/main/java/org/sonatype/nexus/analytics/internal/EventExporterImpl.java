/*
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

package org.sonatype.nexus.analytics.internal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.analytics.Anonymizer;
import org.sonatype.nexus.analytics.EventData;
import org.sonatype.nexus.analytics.EventExporter;
import org.sonatype.nexus.analytics.EventHeader;
import org.sonatype.nexus.analytics.EventStore;
import org.sonatype.nexus.wonderland.DownloadService;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.kazuki.v0.store.journal.JournalStore;
import io.kazuki.v0.store.journal.PartitionInfo;
import io.kazuki.v0.store.journal.PartitionInfoSnapshot;
import io.kazuki.v0.store.keyvalue.KeyValueIterable;
import io.kazuki.v0.store.keyvalue.KeyValuePair;
import org.apache.commons.lang.time.StopWatch;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Default {@link EventExporter} implementation.
 *
 * @since 2.8
 */
@Named
@Singleton
public class EventExporterImpl
    extends ComponentSupport
    implements EventExporter
{
  private final EventStoreImpl eventStore;

  private final Anonymizer anonymizer;

  private final EventHeaderFactory headerFactory;

  private final DownloadService downloadService;

  private final ReentrantLock exportLock = new ReentrantLock();

  @Inject
  public EventExporterImpl(final DownloadService downloadService,
                           final EventStoreImpl eventStore,
                           final EventHeaderFactory headerFactory,
                           final Anonymizer anonymizer)
  {
    this.downloadService = checkNotNull(downloadService);
    this.eventStore = checkNotNull(eventStore);
    this.anonymizer = checkNotNull(anonymizer);
    this.headerFactory = checkNotNull(headerFactory);
  }

  /**
   * @throws IllegalStateException If an export is already in progress.
   */
  @Override
  public File export(final boolean dropAfterExport) throws Exception {
    try {
      checkState(exportLock.tryLock(), "Export already in progress");
      return doExport(dropAfterExport);
    }
    finally {
      if (exportLock.isHeldByCurrentThread()) {
        exportLock.unlock();
      }
    }
  }

  /**
   * Helper to anonymize sensitive event data.
   */
  private class AnonymizerHelper
  {
    private final Cache<String, String> cache = CacheBuilder.newBuilder()
        .maximumSize(200)
        .build();

    public EventData anonymize(final EventData event) {
      event.setUserId(anonymize(event.getUserId()));
      event.setSessionId(anonymize(event.getSessionId()));
      return event;
    }

    private String anonymize(final String text) {
      if (text == null) {
        return null;
      }

      String result = cache.getIfPresent(text);
      if (result == null) {
        result = anonymizer.anonymize(text);
        log.debug("Anonymized {} -> {}", text, result);
        cache.put(text, result);
      }
      return result;
    }
  }

  private File doExport(final boolean dropAfterExport) throws Exception {
    log.info("Exporting; dropAfterExport: {}", dropAfterExport);

    // TODO: Skip if there are no events, perhaps should be done outside and/or make return nullable?

    StopWatch watch = new StopWatch();
    watch.start();

    JournalStore journal = eventStore.getJournalStore();

    // Close the current partition, so that any new events are separate from those that exist already
    journal.closeActivePartition();

    File file = File.createTempFile("analytics-", ".zip");
    log.debug("Exporting to: {}", file);

    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    JsonFactory jsonFactory = mapper.getFactory();

    AnonymizerHelper anonymizerHelper = new AnonymizerHelper();
    long eventCount = 0;
    int partitionCount = 0;

    try (ZipOutputStream output = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
      output.setLevel(Deflater.DEFAULT_COMPRESSION);

      writeHeader(jsonFactory, output);

      // write each partition to its own file in the zip
      try (KeyValueIterable<PartitionInfoSnapshot> partitions = journal.getAllPartitions()) {
        for (PartitionInfo partition : partitions) {
          if (!partition.isClosed()) {
            // skip new open partitions, this is new data _after_ the export was requested
            break;
          }
          partitionCount++;

          // new entry in the zip for each partition
          ZipEntry zipEntry = new ZipEntry("events-" + partitionCount + ".json");
          output.putNextEntry(zipEntry);
          log.debug("Writing entry: {}, partition: {} w/{} records",
              zipEntry.getName(), partition.getPartitionId(), partition.getSize());

          JsonGenerator generator = jsonFactory.createGenerator(output);
          generator.writeStartArray();

          try (KeyValueIterable<KeyValuePair<EventData>> iter = journal.entriesRelative(
              EventStore.SCHEMA_NAME, EventData.class, 0L, partition.getSize()))
          {
            for (KeyValuePair<EventData> entry : iter) {
              generator.writeObject(anonymizerHelper.anonymize(entry.getValue()));
              eventCount++;
            }
          }

          generator.writeEndArray();
          generator.flush();
          output.closeEntry();

          if (dropAfterExport) {
            journal.dropPartition(partition.getPartitionId());
          }
        }
      }
    }

    // Move completed export file into place
    File target = downloadService.move(file, downloadService.uniqueName("analytics-") + ".zip");

    log.info("Exported {} partitions, {} events to: {}, took: {}", partitionCount, eventCount, target, watch);
    return target;
  }

  private void writeHeader(final JsonFactory jsonFactory, final ZipOutputStream output) throws Exception {
    EventHeader header = headerFactory.create();

    ZipEntry zipEntry = new ZipEntry("header.json");
    output.putNextEntry(zipEntry);

    JsonGenerator generator = jsonFactory.createGenerator(output);
    generator.writeObject(header);
    generator.flush();
    output.closeEntry();
  }
}
