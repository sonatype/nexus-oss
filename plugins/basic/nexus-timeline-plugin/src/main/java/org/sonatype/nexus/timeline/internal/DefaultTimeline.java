/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.timeline.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.timeline.Entry;
import org.sonatype.nexus.timeline.Timeline;
import org.sonatype.nexus.timeline.TimelineCallback;
import org.sonatype.nexus.timeline.TimelinePlugin;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import io.kazuki.v0.store.KazukiException;
import io.kazuki.v0.store.journal.JournalStore;
import io.kazuki.v0.store.journal.PartitionInfo;
import io.kazuki.v0.store.journal.PartitionInfoSnapshot;
import io.kazuki.v0.store.keyvalue.KeyValueIterable;
import io.kazuki.v0.store.keyvalue.KeyValuePair;
import io.kazuki.v0.store.keyvalue.KeyValueStoreIteration.SortDirection;
import io.kazuki.v0.store.lifecycle.Lifecycle;
import io.kazuki.v0.store.schema.SchemaStore;
import io.kazuki.v0.store.schema.TypeValidation;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link Timeline} backed by Kazuki.
 *
 * @since 3.0
 */
@Named
@Singleton
public class DefaultTimeline
    extends LifecycleSupport
    implements Timeline
{
  private final Lifecycle lifecycle;

  private final JournalStore journalStore;

  private final SchemaStore schemaStore;

  @Inject
  public DefaultTimeline(final EventBus eventBus,
                         final @Named(TimelinePlugin.ARTIFACT_ID) Lifecycle lifecycle,
                         final @Named(TimelinePlugin.ARTIFACT_ID) JournalStore journalStore,
                         final @Named(TimelinePlugin.ARTIFACT_ID) SchemaStore schemaStore)
  {
    this.lifecycle = checkNotNull(lifecycle);
    this.journalStore = checkNotNull(journalStore);
    this.schemaStore = checkNotNull(schemaStore);
    eventBus.register(this);
  }

  // EBus handlers

  @Subscribe
  public void on(final NexusInitializedEvent evt) {
    try {
      start();
    }
    catch (Exception e) {
      log.error("Could not start timeline", e);
    }
  }

  @Subscribe
  public void on(final NexusStoppedEvent evt) {
    try {
      stop();
    }
    catch (Exception e) {
      log.error("Could not stop timeline", e);
    }
  }

  // LifecycleSupport

  @Override
  public void doStart()
      throws IOException
  {
    log.debug("Starting Timeline");
    try {
      lifecycle.init();
      lifecycle.start();

      // create schema if needed
      if (schemaStore.retrieveSchema(EntryRecord.SCHEMA_NAME) == null) {
        log.info("Creating schema for {} type", EntryRecord.SCHEMA_NAME);
        schemaStore.createSchema(EntryRecord.SCHEMA_NAME, EntryRecord.SCHEMA);
      }
      log.info("Started Timeline");
    }
    catch (KazukiException e) {
      throw new IOException("Could not start Timeline", e);
    }
  }

  @Override
  public void doStop()
      throws IOException
  {
    log.debug("Stopping Timeline");
    lifecycle.stop();
    lifecycle.shutdown();
    log.info("Stopped Timeline");
  }

  // API

  @Override
  public void add(long timestamp, String type, String subType, Map<String, String> data) {
    if (!isStarted()) {
      return;
    }
    addEntryRecord(Collections.singletonList(new EntryRecord(timestamp, type, subType, data)));
  }

  @Override
  public void add(final Entry... records) {
    if (!isStarted()) {
      return;
    }
    final ArrayList<EntryRecord> entries = Lists.newArrayListWithCapacity(records.length);
    for (Entry record : records) {
      if (record instanceof EntryRecord) {
        entries.add((EntryRecord) record);
      }
      else {
        entries.add(new EntryRecord(
            record.getTimestamp(),
            record.getType(),
            record.getSubType(),
            record.getData()));
      }
    }
    addEntryRecord(entries);
  }

  private void addEntryRecord(final List<EntryRecord> records) {
    try {
      for (EntryRecord record : records) {
        journalStore.append(EntryRecord.SCHEMA_NAME, EntryRecord.class, record, TypeValidation.STRICT);
      }
    }
    catch (KazukiException e) {
      log.warn("Failed to append a Timeline record", e);
    }
  }


  @Override
  public void retrieve(final int fromItem,
                       final int count,
                       final Set<String> types,
                       final Set<String> subTypes,
                       final Predicate<Entry> filter,
                       final TimelineCallback callback)
  {
    if (!isStarted()) {
      return;
    }
    try {
      // We do manual filtering here, so not passing in limit and limiting manually
      int currentCount = 0;
      try (final KeyValueIterable<KeyValuePair<EntryRecord>> kvs = journalStore
          .entriesRelative(EntryRecord.SCHEMA_NAME, EntryRecord.class, SortDirection.DESCENDING, (long) fromItem,
              null)) {
        for (KeyValuePair<EntryRecord> kv : kvs) {
          final EntryRecord record = kv.getValue();
          if (types != null && !types.contains(record.getType())) {
            continue; // skip it
          }
          if (subTypes != null && !subTypes.contains(record.getSubType())) {
            continue; // skip it
          }
          if (filter != null && !filter.apply(record)) {
            continue; // skip it
          }
          currentCount++;
          if (count < currentCount) {
            break;
          }
          callback.processNext(record);
        }
      }
    }
    catch (Exception e) {
      log.warn("Failure during retrieve or callback, processing stopped", e);
    }
  }

  @Override
  public void purgeOlderThan(final int days) {
    if (!isStarted()) {
      return;
    }
    // NOTE: This is merely a hack, we need to come up with some general partitioning strategy
    // Could we partition per-day? Or just modify the semantics of the input maybe, as purge
    // was painfully needed for Lucene backed Timeline, but with Kazuki it might get non-issue?
    try {
      if (days == 0) {
        // today included, lose the current partition, so that any new events are separate from those that exist already
        journalStore.closeActivePartition();
      }
      try (KeyValueIterable<PartitionInfoSnapshot> partitions = journalStore.getAllPartitions()) {
        for (PartitionInfo partition : partitions) {
          if (!partition.isClosed()) {
            continue;
          }
          journalStore.dropPartition(partition.getPartitionId());
        }
      }
    }
    catch (KazukiException e) {
      log.warn("Failed to purge Timeline store", e);
    }
  }
}
