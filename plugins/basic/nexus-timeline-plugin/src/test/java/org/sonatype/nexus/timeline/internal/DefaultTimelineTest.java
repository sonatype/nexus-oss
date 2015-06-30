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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.sonatype.nexus.orient.MemoryDatabaseManager;
import org.sonatype.nexus.orient.MinimalDatabaseServer;
import org.sonatype.nexus.timeline.Entry;
import org.sonatype.nexus.timeline.EntryListCallback;
import org.sonatype.nexus.timeline.TimelineCallback;
import org.sonatype.sisu.goodies.common.Time;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link DefaultTimeline}.
 */
public class DefaultTimelineTest
    extends TestSupport
{
  private MinimalDatabaseServer databaseServer;

  private MemoryDatabaseManager databaseManager;

  private DefaultTimeline underTest;

  @Before
  public void prepare() throws Exception {
    this.databaseServer = new MinimalDatabaseServer();
    databaseServer.start();

    this.databaseManager = new MemoryDatabaseManager();
    databaseManager.start();

    underTest = new DefaultTimeline(mock(EventBus.class), databaseManager);
    underTest.start();
    underTest.purgeOlderThan(0);
  }

  @After
  public void cleanup() throws Exception {
    if (underTest != null) {
      underTest.stop();
      underTest = null;
    }

    if (databaseManager != null) {
      databaseManager.stop();
      databaseManager = null;
    }

    if (databaseServer != null) {
      databaseServer.stop();
      databaseServer = null;
    }
  }

  // ==

  @Test
  public void simpleTimestamp() throws Exception {
    final Map<String, String> data = Maps.newHashMap();
    data.put("a", "a");
    underTest.add(new EntryRecord(System.currentTimeMillis() - Time.hours(1).toMillis(), "TEST", "1", data));
    data.put("b", "b");
    underTest.add(new EntryRecord(System.currentTimeMillis() - Time.hours(1).toMillis(), "TEST", "2", data));

    List<Entry> res;

    // 1st possible hit skipped, so empty results expected
    res = asList(1, 10, Collections.singleton("TEST"), Collections.singleton("2"), null);
    assertThat(res, is(empty()));

    // one TEST:1 exists
    res = asList(0, 10, Collections.singleton("TEST"), Collections.singleton("1"), null);
    assertThat(res, hasSize(1));

    // one TEST:2 exists
    res = asList(0, 10, Collections.singleton("TEST"), Collections.singleton("2"), null);
    assertThat(res, hasSize(1));

    // two TEST:* exists
    res = asList(0, 10, Collections.singleton("TEST"), null, null);
    assertThat(res, hasSize(2));
  }

  @Test
  public void simpleItem() throws Exception {
    final Map<String, String> data = Maps.newHashMap();
    data.put("a", "a");
    underTest.add(new EntryRecord(System.currentTimeMillis() - Time.hours(1).toMillis(), "TEST", "1", data));
    data.put("b", "b");
    underTest.add(new EntryRecord(System.currentTimeMillis() - Time.hours(1).toMillis(), "TEST", "2", data));

    List<Entry> res;

    // we have two TEST:* records
    res = asList(0, 10, Collections.singleton("TEST"), null, null);
    assertThat(res, hasSize(2));

    // returned one if we skip one
    res = asList(1, 10, Collections.singleton("TEST"), null, null);
    assertThat(res, hasSize(1));
    assertThat(res.get(0).getData(), hasEntry("a", "a"));

    // returned none if we skip 2 out of 2
    res = asList(2, 10, Collections.singleton("TEST"), null, null);
    assertThat(res, is(empty()));

    // returned 1st when no skip, count is 1
    res = asList(0, 1, Collections.singleton("TEST"), null, null);
    assertThat(res, hasSize(1));
    assertThat(res.get(0).getData(), hasEntry("b", "b"));

    // returns none if count is 0
    res = asList(0, 0, Collections.singleton("TEST"), null, null);
    assertThat(res, is(empty()));

    // returned 1 as ther is only one TEST:1
    res = asList(0, 10, Collections.singleton("TEST"), Collections.singleton("1"), null);
    assertThat(res, hasSize(1));

    // returned 1 as ther is only one TEST:2
    res = asList(0, 10, Collections.singleton("TEST"), Collections.singleton("2"), null);
    assertThat(res, hasSize(1));

    // we have two TEST:* records
    res = asList(0, 10, Collections.singleton("TEST"), null, null);
    assertThat(res, hasSize(2));
  }

  @Test
  public void order() throws Exception {
    final Map<String, String> data = Maps.newHashMap();
    data.put("place", "2nd");
    underTest.add(new EntryRecord(System.currentTimeMillis() - Time.hours(2).toMillis(), "TEST", "1", data));
    data.put("place", "1st");
    underTest.add(new EntryRecord(System.currentTimeMillis() - Time.hours(1).toMillis(), "TEST", "1", data));

    final List<Entry> res = asList(0, 10, Collections.singleton("TEST"), null, null);

    System.out.println(res);
    assertThat(res, hasSize(2));
    assertThat(res.get(0).getData(), hasEntry("place", "1st"));
    assertThat(res.get(1).getData(), hasEntry("place", "2nd"));
  }

  @Test
  public void partitioningByDay() throws Exception {
    final long now = new DateMidnight(DateTimeZone.UTC).getMillis();
    underTest.add(new EntryRecord(now - TimeUnit.DAYS.toMillis(2), "TEST", "1", ImmutableMap.of("day", "1")));
    underTest.add(new EntryRecord(now - TimeUnit.DAYS.toMillis(2), "TEST", "2", ImmutableMap.of("day", "1")));
    underTest.add(new EntryRecord(now - TimeUnit.DAYS.toMillis(1), "TEST", "1", ImmutableMap.of("day", "2")));
    underTest.add(new EntryRecord(now - TimeUnit.DAYS.toMillis(1), "TEST", "2", ImmutableMap.of("day", "2")));
    underTest.add(new EntryRecord(now, "TEST", "1", ImmutableMap.of("day", "3")));
    underTest.add(new EntryRecord(now, "TEST", "2", ImmutableMap.of("day", "3")));

    List<String> partitionsPostAppend;
    List<String> partitionsPostPurge1;
    List<String> partitionsPostPurge2;
    List<String> partitionsPostPurge3;
    try (ODatabaseDocumentTx db = underTest.openDb()) {
      partitionsPostAppend = Lists.newArrayList(Collections2.filter(db.getClusterNames(), new Predicate<String>()
      {
        @Override
        public boolean apply(String input) {
          return input.startsWith(DefaultTimeline.DB_CLUSTER_PREFIX);
        }
      }));
      assertThat(db.countClass(DefaultTimeline.DB_CLASS), equalTo(6L));
    }

    underTest.purgeOlderThan(2);
    // ensure 1st returned entry is the latest (and still exists on timeline)
    underTest.retrieve(0, 1, null, null, null, new TimelineCallback()
    {
      public boolean processNext(final Entry rec) throws IOException {
        assertThat(rec.getType(), equalTo("TEST"));
        assertThat(rec.getSubType(), equalTo("2"));
        assertThat(rec.getData().get("day"), equalTo("3"));
        return false;
      }
    });

    try (ODatabaseDocumentTx db = underTest.openDb()) {
      partitionsPostPurge1 = Lists.newArrayList(Collections2.filter(db.getClusterNames(), new Predicate<String>()
      {
        @Override
        public boolean apply(String input) {
          return input.startsWith(DefaultTimeline.DB_CLUSTER_PREFIX);
        }
      }));
      assertThat(db.countClass(DefaultTimeline.DB_CLASS), equalTo(4L));
    }

    underTest.purgeOlderThan(1);
    // ensure 1st returned entry is the latest (and still exists on timeline)
    underTest.retrieve(0, 1, null, null, null, new TimelineCallback()
    {
      public boolean processNext(final Entry rec) throws IOException {
        assertThat(rec.getType(), equalTo("TEST"));
        assertThat(rec.getSubType(), equalTo("2"));
        assertThat(rec.getData().get("day"), equalTo("3"));
        return false;
      }
    });

    try (ODatabaseDocumentTx db = underTest.openDb()) {
      partitionsPostPurge2 = Lists.newArrayList(Collections2.filter(db.getClusterNames(), new Predicate<String>()
      {
        @Override
        public boolean apply(String input) {
          return input.startsWith(DefaultTimeline.DB_CLUSTER_PREFIX);
        }
      }));
      assertThat(db.countClass(DefaultTimeline.DB_CLASS), equalTo(2L));
    }

    underTest.purgeOlderThan(0);
    // ensure that timeline is empty, callback should not be called
    underTest.retrieve(0, 1, null, null, null, new TimelineCallback()
    {
      public boolean processNext(final Entry rec) throws IOException {
        assertThat("Timeline should be empty. callback should not be invoked!", false);
        return false;
      }
    });

    try (ODatabaseDocumentTx db = underTest.openDb()) {
      partitionsPostPurge3 = Lists.newArrayList(Collections2.filter(db.getClusterNames(), new Predicate<String>()
      {
        @Override
        public boolean apply(String input) {
          return input.startsWith(DefaultTimeline.DB_CLUSTER_PREFIX);
        }
      }));
      assertThat(db.countClass(DefaultTimeline.DB_CLASS), equalTo(0L));
    }
    assertThat(partitionsPostAppend, hasSize(3));
    assertThat(partitionsPostPurge1, hasSize(2));
    assertThat(partitionsPostPurge2, hasSize(1));
    assertThat(partitionsPostPurge3, hasSize(0)); // this is true in test, but KZ would add part on any incoming append!
  }

  // ==

  /**
   * Handy method that does what was done before: keeps all in memory, but this is usable for small amount of data,
   * like these in UT. This should NOT be used in production code, unless you want app that kills itself with OOM.
   */
  protected List<Entry> asList(int fromItem, int count, Set<String> types, Set<String> subTypes, Predicate<Entry> filter)
      throws Exception
  {
    final EntryListCallback result = new EntryListCallback();
    underTest.retrieve(fromItem, count, types, subTypes, filter, result);
    return result.getEntries();
  }
}
