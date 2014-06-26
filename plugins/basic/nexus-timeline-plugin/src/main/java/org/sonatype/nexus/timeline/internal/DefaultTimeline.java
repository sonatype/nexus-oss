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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.proxy.events.NexusInitializedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.timeline.Entry;
import org.sonatype.nexus.timeline.Timeline;
import org.sonatype.nexus.timeline.TimelineCallback;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import com.google.common.base.Joiner;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentPool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OClass.INDEX_TYPE;
import com.orientechnologies.orient.core.metadata.schema.OClassImpl;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.schema.clusterselection.ODefaultClusterSelectionStrategy;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OResultSet;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.storage.OStorage.CLUSTER_TYPE;
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
  private static final String DB_LOCATION = "db/timeline";

  private static final String DB_CLASS = "entryrecord";

  private static final String DB_CLUSTER_PREFIX = DB_CLASS + "_cluster";

  private static final String P_TIMESTAMP = "timestamp";

  private static final String P_TYPE = "type";

  private static final String P_SUBTYPE = "subtype";

  private static final String P_DATA = "data";

  private static final String I_TYPE = "typeIdx";

  private static final String I_SUBTYPE = "subtypeIdx";

  private final File databaseDirectory;

  private ODatabaseDocumentPool pool;

  @Inject
  public DefaultTimeline(final EventBus eventBus, final ApplicationDirectories applicationDirectories) {
    checkNotNull(applicationDirectories);
    this.databaseDirectory = applicationDirectories.getWorkDirectory(DB_LOCATION);
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
    final String dbUri = "plocal:" + databaseDirectory.getAbsolutePath();

    try (ODatabaseDocumentTx db = new ODatabaseDocumentTx(dbUri)) {
      if (!db.exists()) {
        db.create();
        log.info("Created database: {}", databaseDirectory);
      }
      else {
        db.open("admin", "admin");
        log.info("Opened database: {}", db);
      }

      // entities
      final OSchema schema = db.getMetadata().getSchema();
      if (!schema.existsClass(DB_CLASS)) {
        final OClass type = schema.createClass(DB_CLASS);
        type.createProperty(P_TIMESTAMP, OType.LONG);
        type.createProperty(P_TYPE, OType.STRING);
        type.createProperty(P_SUBTYPE, OType.STRING);
        type.createProperty(P_DATA, OType.EMBEDDEDMAP);
        type.createIndex(I_TYPE, INDEX_TYPE.NOTUNIQUE_HASH_INDEX, P_TYPE);
        type.createIndex(I_SUBTYPE, INDEX_TYPE.NOTUNIQUE_HASH_INDEX, P_SUBTYPE);

        // make sure cluster selection is set, as we rely on clustering
        type.setClusterSelection(ODefaultClusterSelectionStrategy.NAME);
        // cluster will be created and set on 1st addition, if needed

        schema.save();
        log.info("Created schema: {}, properties: {}", type, type.properties());
      }
    }

    pool = new ODatabaseDocumentPool(dbUri, "admin", "admin");
    pool.setName("timeline-database-pool");
    pool.setup(1, 10);
    log.info("Created pool: {}", pool);
  }

  @Override
  public void doStop() throws Exception {
    log.info("Closing pool: {}", pool);
    pool.close();
    pool = null;
  }

  private ODatabaseDocumentTx db() {
    ensureStarted();
    return pool.acquire();
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
        entries.add(new EntryRecord(record.getTimestamp(), record.getType(), record.getSubType(), record.getData()));
      }
    }
    addEntryRecord(entries);
  }

  private void addEntryRecord(final List<EntryRecord> records) {
    for (EntryRecord record : records) {
      maybeAddNewCluster(record.getTimestamp());
      try (ODatabaseDocumentTx db = db()) {
        ODocument doc = db.newInstance(DB_CLASS);
        doc.field(P_TIMESTAMP, record.getTimestamp());
        doc.field(P_TYPE, record.getType());
        doc.field(P_SUBTYPE, record.getSubType());
        doc.field(P_DATA, record.getData());
        doc.save();
      }
    }
  }

  private synchronized int maybeAddNewCluster(final long timestamp) {
    try (ODatabaseDocumentTx db = db()) {
      final String name = String.format("%s_%s", DB_CLUSTER_PREFIX,
          new DateMidnight(timestamp, DateTimeZone.UTC).toString("YYYYMMdd"));
      int cid = db.getClusterIdByName(name); // undocumented: if cluster not exists, returns -1
      if (cid == -1) {
        cid = db.addCluster(name, CLUSTER_TYPE.PHYSICAL);
        final OSchema schema = db.getMetadata().getSchema();
        final OClass type = schema.getClass(DB_CLASS);
        type.addClusterId(cid);
        type.setDefaultClusterId(cid);
        schema.save();
        log.debug("Created new journal cluster; id: {}, name: {}", cid, name);
        return cid;
      }
      else {
        return cid;
      }
    }
  }

  @Override
  public void retrieve(final int fromItem, final int count, final Set<String> types, final Set<String> subTypes,
      final Predicate<Entry> filter, final TimelineCallback callback)
  {
    if (!isStarted()) {
      return;
    }
    try (ODatabaseDocumentTx db = db()) {
      final StringBuilder sb = new StringBuilder();
      sb.append("SELECT FROM ").append(DB_CLASS).append(" WHERE ");
      if ((types != null && !types.isEmpty())) {
        sb.append(P_TYPE).append(" IN ").append("[\"").append(Joiner.on("\", \"").join(types)).append("\"] AND ");
      }
      if (subTypes != null && !subTypes.isEmpty()) {
        sb.append(P_SUBTYPE).append(" IN ").append("[\"").append(Joiner.on("\", \"").join(types)).append("\"] AND ");
      }
      sb.append("1=1 ");
      sb.append("ORDER BY ").append(P_TIMESTAMP).append(" DESC ");
      sb.append("SKIP ").append(fromItem).append(" LIMIT ").append(count);

      log.info("Query: {}", sb.toString());

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
    catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public void purgeOlderThan(final int days) {
    if (!isStarted()) {
      return;
    }
    try (ODatabaseDocumentTx db = db()) {
      final DateMidnight nowDm = new DateMidnight(DateTimeZone.UTC);
      final int[] cids = db.getMetadata().getSchema().getClass(DB_CLASS).getClusterIds();
      for (int cid : cids) {
        final String name = db.getClusterNameById(cid);
        if (name.startsWith(DB_CLUSTER_PREFIX)) {
          final int year = Integer.parseInt(name.substring(DB_CLUSTER_PREFIX.length(), DB_CLUSTER_PREFIX.length() + 4));
          final int month = Integer.parseInt(name.substring(DB_CLUSTER_PREFIX.length() + 4,
              DB_CLUSTER_PREFIX.length() + 6));
          final int day = Integer.parseInt(name.substring(DB_CLUSTER_PREFIX.length() + 6,
              DB_CLUSTER_PREFIX.length() + 8));
          final DateMidnight clusterDm = new DateMidnight(year, month, day, DateTimeZone.UTC);
          if (Days.daysBetween(nowDm, clusterDm).getDays() >= days) {
            OSchema schema = db.getMetadata().getSchema();
            OClass type = schema.getClass(DB_CLASS);
            type.removeClusterId(cid);
            schema.save();
            ((OClassImpl) type).fromStream(); // workaround for OrientDB polymorphicClusterId bug
            db.dropCluster(cid, true);
          }
        }
      }
    }
  }
}
