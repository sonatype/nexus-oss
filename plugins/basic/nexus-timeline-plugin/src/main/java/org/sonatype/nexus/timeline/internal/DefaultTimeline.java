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

import org.sonatype.nexus.common.app.NexusInitializedEvent;
import org.sonatype.nexus.common.app.NexusStoppedEvent;
import org.sonatype.nexus.orient.DatabaseManager;
import org.sonatype.nexus.orient.DatabasePool;
import org.sonatype.nexus.orient.OIndexNameBuilder;
import org.sonatype.nexus.timeline.Entry;
import org.sonatype.nexus.timeline.Timeline;
import org.sonatype.nexus.timeline.TimelineCallback;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OResultSet;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link Timeline} backed by OrientDB.
 *
 * @since 3.0
 */
@Named
@Singleton
public class DefaultTimeline
    extends LifecycleSupport
    implements Timeline
{
  private static final String DB_NAME = "timeline";

  @VisibleForTesting
  static final String DB_CLASS = "entryrecord";

  @VisibleForTesting
  static final String DB_CLUSTER_PREFIX = DB_CLASS + "_cluster_";

  private static final String P_TIMESTAMP = "timestamp";

  private static final String P_TYPE = "type";

  private static final String P_SUBTYPE = "subtype";

  private static final String P_DATA = "data";

  private static final String I_TYPE = new OIndexNameBuilder()
      .type(DB_CLASS)
      .property(P_TYPE)
      .build();

  private static final String I_SUBTYPE = new OIndexNameBuilder()
      .type(DB_CLASS)
      .property(P_SUBTYPE)
      .build();

  private final DatabaseManager databaseManager;

  private DatabasePool pool;

  @Inject
  public DefaultTimeline(final EventBus eventBus, final DatabaseManager databaseManager) {
    this.databaseManager = checkNotNull(databaseManager);
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
    try (ODatabaseDocumentTx db = databaseManager.connect(DB_NAME, true)) {

      // entities
      final OSchema schema = db.getMetadata().getSchema();
      if (!schema.existsClass(DB_CLASS)) {
        final OClass type = schema.createClass(DB_CLASS);
        type.createProperty(P_TIMESTAMP, OType.LONG);
        type.createProperty(P_TYPE, OType.STRING);
        type.createProperty(P_SUBTYPE, OType.STRING);
        type.createProperty(P_DATA, OType.EMBEDDEDMAP, OType.STRING);
        type.createIndex(I_TYPE, INDEX_TYPE.NOTUNIQUE_HASH_INDEX, P_TYPE);
        type.createIndex(I_SUBTYPE, INDEX_TYPE.NOTUNIQUE_HASH_INDEX, P_SUBTYPE);

        log.info("Created schema: {}, properties: {}", type, type.properties());
      }
    }

    this.pool = databaseManager.newPool(DB_NAME);
  }

  @Override
  public void doStop() throws Exception {
    pool.close();
    pool = null;
  }

  @VisibleForTesting
  ODatabaseDocumentTx openDb() {
    ensureStarted();
    return pool.acquire();
  }

  // API

  @Override
  public void add(long timestamp, String type, String subType, Map<String, String> data) {
    // events may be added before started unfortunately, ignore them
    if (!isStarted()) {
      return;
    }
    addEntryRecord(Collections.singletonList(new EntryRecord(timestamp, type, subType, data)));
  }

  @Override
  public void add(final Entry... records) {
    // events may be added before started unfortunately, ignore them
    if (!isStarted()) {
      return;
    }
    final ArrayList<EntryRecord> entries = Lists.newArrayListWithCapacity(records.length);
    for (Entry record : records) {
      if (record instanceof EntryRecord) {
        entries.add((EntryRecord) record);
      }
      else {
        entries.add(new EntryRecord(record.getTimestamp(), record.getType(), record.getSubType(), record.getData()));
      }
    }
    addEntryRecord(entries);
  }

  private void addEntryRecord(final List<EntryRecord> records) {
    if (records.isEmpty()) {
      return; // spare resources from getting DB for nothing
    }
    // this must be synced to prevent purge drop cluster being created
    synchronized (this) {
      try (ODatabaseDocumentTx db = openDb()) {
        // 1st pass (no TX, DDL): add clusters needed by records
        final Map<Long, String> timestampToClusterMap = Maps.newHashMap();
        for (EntryRecord record : records) {
          if (!timestampToClusterMap.containsKey(record.getTimestamp())) {
            timestampToClusterMap.put(record.getTimestamp(), maybeAddNewCluster(db, record.getTimestamp()));
          }
        }
        // 2nd pass (in TX, DML): insert records into their places.
        db.begin();
        try {
          for (EntryRecord record : records) {
            ODocument doc = db.newInstance(DB_CLASS);
            doc.field(P_TIMESTAMP, record.getTimestamp());
            doc.field(P_TYPE, record.getType());
            doc.field(P_SUBTYPE, record.getSubType());
            doc.field(P_DATA, record.getData());
            doc.save(timestampToClusterMap.get(record.getTimestamp()));
          }
          db.commit();
        }
        catch (Exception e) {
          db.rollback();
          throw Throwables.propagate(e);
        }
      }
    }
  }

  /**
   * Calculates the expected cluster name where given timestamp should be located. It adds new cluster if cluster with
   * calculated name not exists, hence, after the return from this method it is guaranteed that the cluster with name
   * returned does exists. Clusters have common prefixes (see {@link #DB_CLUSTER_PREFIX}) and suffix is timestamp's
   * date
   * (rounded to midnight) as string with pattern {@code YYYYMMDD}. As OrientDB DDL is not atomic, this method must be
   * mutually exclusive with method {@link #purgeOlderThan(int)}, hence both should be synchronized (or called from
   * synchronized block like this method). This method must be called outside of a TX as it performs DDL.
   */
  private String maybeAddNewCluster(final ODatabaseDocumentTx db, final long timestamp) {
    final String name = String.format("%s%s", DB_CLUSTER_PREFIX,
        new DateMidnight(timestamp, DateTimeZone.UTC).toString("YYYYMMdd"));
    int cid = db.getClusterIdByName(name); // undocumented: if cluster not exists, returns -1
    if (cid == -1) {
      cid = db.addCluster(name);
      final OSchema schema = db.getMetadata().getSchema();
      final OClass type = schema.getClass(DB_CLASS);
      type.addClusterId(cid);
      log.info("Created new journal cluster; id: {}, name: {}", cid, name);
    }
    else {
      log.debug("Journal cluster exists; id: {}, name: {}", cid, name);
    }
    return name;
  }

  @Override
  public void retrieve(final int fromItem, final int count, final Set<String> types, final Set<String> subTypes,
                       final Predicate<Entry> filter, final TimelineCallback callback)
  {
    if (!isStarted() || count == 0) {
      return;
    }
    try (ODatabaseDocumentTx db = openDb()) {
      db.begin();
      try {
        final StringBuilder sb = new StringBuilder();
        sb.append("SELECT FROM ").append(DB_CLASS);
        if ((types != null && !types.isEmpty()) || (subTypes != null && !subTypes.isEmpty())) {
          sb.append(" WHERE ");
        }
        if ((types != null && !types.isEmpty())) {
          sb.append(P_TYPE).append(" IN ").append("[\"").append(Joiner.on("\", \"").join(types)).append("\"] ");
        }
        if (subTypes != null && !subTypes.isEmpty()) {
          if ((types != null && !types.isEmpty())) {
            sb.append(" AND ");
          }
          sb.append(P_SUBTYPE).append(" IN ").append("[\"").append(Joiner.on("\", \"").join(subTypes)).append("\"] ");
        }
        sb.append(" ORDER BY @rid DESC SKIP ").append(fromItem).append(" LIMIT ").append(count);

        log.debug("Query: {}", sb);

        final OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(sb.toString());

        final OResultSet<ODocument> results = db.query(query);
        if (results.isEmpty()) {
          return;
        }
        for (ODocument doc : results) {
          final EntryRecord record = new EntryRecord((Long) doc.field(P_TIMESTAMP, OType.LONG), (String) doc.field(
              P_TYPE, OType.STRING), (String) doc.field(P_SUBTYPE, OType.STRING), null);
          final Map<String, String> attributes = doc.field(P_DATA, OType.EMBEDDEDMAP);
          record.getData().putAll(attributes);
          if (!callback.processNext(record)) {
            break;
          }
        }
      }
      finally {
        db.commit();
      }
    }
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Purges old clusters based on {@code days} ("older than days") parameters. If input is {@code 0}, all clusters will
   * be removed, meaning all the timeline is purged. As Orient DDL is not atomic, this method must be mutually
   * exclusive
   * with {@link #maybeAddNewCluster(ODatabaseDocumentTx, long)}, hence both are synchronized.
   */
  @Override
  public synchronized void purgeOlderThan(final int days) {
    if (!isStarted()) {
      return;
    }
    try (ODatabaseDocumentTx db = openDb()) {
      final DateMidnight nowDm = new DateMidnight(DateTimeZone.UTC);
      final int prefixLen = DB_CLUSTER_PREFIX.length();
      final int[] cids = db.getMetadata().getSchema().getClass(DB_CLASS).getClusterIds();
      for (int cid : cids) {
        final String name = db.getClusterNameById(cid);
        log.debug("Cluster: {} {}", cid, name);
        if (name.startsWith(DB_CLUSTER_PREFIX)) {
          final int year = Integer.parseInt(name.substring(prefixLen, prefixLen + 4));
          final int month = Integer.parseInt(name.substring(prefixLen + 4, prefixLen + 6));
          final int day = Integer.parseInt(name.substring(prefixLen + 6, prefixLen + 8));
          final DateMidnight clusterDm = new DateMidnight(year, month, day, DateTimeZone.UTC);
          if (Days.daysBetween(clusterDm, nowDm).getDays() >= days) {
            log.info("Cluster {}, is {} days old, purging it", name, Days.daysBetween(clusterDm, nowDm).getDays());
            OSchema schema = db.getMetadata().getSchema();
            OClass type = schema.getClass(DB_CLASS);
            type.removeClusterId(cid);
            db.dropCluster(cid, true);
          }
        }
      }
    }
  }
}
