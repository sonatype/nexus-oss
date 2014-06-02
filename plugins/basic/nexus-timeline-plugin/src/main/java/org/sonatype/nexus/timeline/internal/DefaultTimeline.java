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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import io.kazuki.v0.internal.v2schema.Attribute.Type;
import io.kazuki.v0.internal.v2schema.Schema;
import io.kazuki.v0.store.KazukiException;
import io.kazuki.v0.store.journal.JournalStore;
import io.kazuki.v0.store.journal.PartitionInfo;
import io.kazuki.v0.store.journal.PartitionInfoSnapshot;
import io.kazuki.v0.store.keyvalue.KeyValueIterable;
import io.kazuki.v0.store.keyvalue.KeyValuePair;
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
  private static final long A_DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);

  private final Lifecycle lifecycle;

  private final JournalStore journalStore;

  private final SchemaStore schemaStore;

  private final Object runningDayMonitor = new Object();

  private Long runningDay;

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
  public void on(final NexusInitializedEvent event) {
    try {
      start();
    }
    catch (Exception e) {
      log.error("Could not start timeline", e);
    }
  }

  @Subscribe
  public void on(final NexusStoppedEvent event) {
    try {
      stop();
    }
    catch (Exception e) {
      log.error("Could not stop timeline", e);
    }
  }

  // LifecycleSupport

  @Override
  public void doStart() throws Exception {
    lifecycle.init();
    lifecycle.start();

    // create schema if needed
    if (schemaStore.retrieveSchema(EntryRecord.SCHEMA_NAME) == null) {
      log.info("Creating schema for {} type", EntryRecord.SCHEMA_NAME);

      Schema schema = new Schema.Builder().
          addAttribute("timestamp", Type.I64, false).
          addAttribute("type", Type.UTF8_SMALLSTRING, false).
          addAttribute("subType", Type.UTF8_SMALLSTRING, false).
          addAttribute("data", Type.MAP, false).
          build();

      schemaStore.createSchema(EntryRecord.SCHEMA_NAME, schema);
    }
  }

  @Override
  public void doStop() throws Exception {
    lifecycle.stop();
    lifecycle.shutdown();
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
        // Long and not primitive, to avoid NPE due to auto-unboxing, since runningDay is null initially
        final Long currentEntryRunningDay = record.getTimestamp() / A_DAY_IN_MILLIS;
        synchronized (runningDayMonitor) {
          if (runningDay == null || runningDay < currentEntryRunningDay) {
            if (runningDay != null) {
              journalStore.closeActivePartition();
            }
            runningDay = currentEntryRunningDay;
          }
        }
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
          .entriesRelative(EntryRecord.SCHEMA_NAME, EntryRecord.class, (long) fromItem,
              null)) {
        for (KeyValuePair<EntryRecord> kv : kvs) {
          final EntryRecord record = kv.getValue();
          // TODO: secondary indexes for types and subTypes?
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
          if (!callback.processNext(record)) {
            break;
          }
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
    try {
      if (days == 0) {
        // today included, close current partition too, so that any new events are separate from those that exist already
        journalStore.closeActivePartition();
      }
      final List<String> partitionIds = Lists.newArrayList();
      try (KeyValueIterable<PartitionInfoSnapshot> partitions = journalStore.getAllPartitions()) {
        // gather all partitions, KZ returns them in creation order (oldest first)
        for (PartitionInfo partition : partitions) {
          // KZ can have only one non-closed "active" partition, and it is the last one returned
          // This means that we will put current active last partition that is possible okay
          // but, if we want to DELETE active one, it cannot be open, as at line above we
          // check for days==0, and we would close it. If days != 0, we will pop it anyway
          // without close, so is fine.
          partitionIds.add(partition.getPartitionId());
        }
      }
      // reverse the list, to have latest first, usually first "days" we want to keep
      Collections.reverse(partitionIds);
      // keep "days" partition by removing them from list
      for (int i = 0; i < days; i++) {
        partitionIds.remove(0);
      }
      // drop others on the list
      while (!partitionIds.isEmpty()) {
        journalStore.dropPartition(partitionIds.remove(0));
      }
    }
    catch (KazukiException e) {
      log.warn("Failed to purge Timeline store", e);
    }
  }
}
