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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.timeline.Entry;
import org.sonatype.nexus.timeline.EntryListCallback;
import org.sonatype.nexus.timeline.Timeline;
import org.sonatype.nexus.util.file.DirSupport;
import org.sonatype.sisu.goodies.common.Time;
import org.sonatype.sisu.goodies.eventbus.EventBus;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class DefaultTimelineTest
    extends TestSupport
{
  @Mock
  private ApplicationDirectories applicationDirectories;

  @Mock
  private EventBus eventBus;

  private DefaultTimeline defaultNexusTimeline;

  @Before
  public void prepare() throws Exception {
    final File timelineWorkdir = util.resolveFile("target/workdir");
    DirSupport.deleteIfExists(timelineWorkdir.toPath());
    timelineWorkdir.mkdirs();
    when(applicationDirectories.getWorkDirectory(anyString())).thenReturn(timelineWorkdir);

    final Module testModule = new AbstractModule()
    {
      @Override
      protected void configure() {
        bind(ApplicationDirectories.class).toInstance(applicationDirectories);
        bind(EventBus.class).toInstance(eventBus);
        bind(Timeline.class).to(DefaultTimeline.class).asEagerSingleton();
      }
    };
    defaultNexusTimeline = (DefaultTimeline) Guice.createInjector(testModule, new TimelineModule())
        .getInstance(Timeline.class);
    defaultNexusTimeline.start();
  }

  @After
  public void cleanup() throws Exception {
    defaultNexusTimeline.stop();
  }

  // ==

  @Test
  public void simpleTimestamp() throws Exception {
    final Map<String, String> data = Maps.newHashMap();
    data.put("a", "a");
    defaultNexusTimeline.add(new EntryRecord(System.currentTimeMillis() - Time.hours(1).toMillis(), "TEST", "1", data));
    data.put("b", "b");
    defaultNexusTimeline.add(new EntryRecord(System.currentTimeMillis() - Time.hours(1).toMillis(), "TEST", "2", data));

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
    defaultNexusTimeline.add(new EntryRecord(System.currentTimeMillis() - Time.hours(1).toMillis(), "TEST", "1", data));
    data.put("b", "b");
    defaultNexusTimeline.add(new EntryRecord(System.currentTimeMillis() - Time.hours(1).toMillis(), "TEST", "2", data));

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
    defaultNexusTimeline.add(new EntryRecord(System.currentTimeMillis() - Time.hours(2).toMillis(), "TEST", "1", data));
    data.put("place", "1st");
    defaultNexusTimeline.add(new EntryRecord(System.currentTimeMillis() - Time.hours(1).toMillis(), "TEST", "1", data));

    final List<Entry> res = asList(0, 10, Collections.singleton("TEST"), null, null);

    System.out.println(res);
    assertThat(res, hasSize(2));
    assertThat(res.get(0).getData(), hasEntry("place", "1st"));
    assertThat(res.get(1).getData(), hasEntry("place", "2nd"));
  }

  // ==

  /**
   * Handy method that does what was done before: keeps all in memory, but this is usable for small amount of data,
   * like these in UT. This should NOT be used in production code, unless you want app that kills itself with OOM.
   */
  protected List<Entry> asList(int fromItem, int count, Set<String> types, Set<String> subTypes,
                               Predicate<Entry> filter) throws Exception
  {
    final EntryListCallback result = new EntryListCallback();
    defaultNexusTimeline.retrieve(fromItem, count, types, subTypes, filter, result);
    return result.getEntries();
  }
}
