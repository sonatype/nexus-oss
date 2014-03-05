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

package org.sonatype.nexus.quartz.internal.store;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.quartz.internal.QuartzModule;
import org.sonatype.nexus.util.file.DirSupport;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.base.Predicate;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.kazuki.v0.store.keyvalue.KeyValuePair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.TriggerKey;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.SchedulerSignaler;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Simple "sanity" IT exercising KZ JobStore with all the Kazuki bells and whistles being booted up..
 */
public class KazukiJobStoreIT
    extends TestSupport
{
  private Injector injector;

  @Mock
  private ApplicationDirectories applicationDirectories;

  @Mock
  private SchedulerSignaler schedulerSignaler;

  @Inject
  private KVJobStore jobStore;

  @Inject
  private KazukiJobDao jobDao;

  @Before
  public void prepare() throws Exception {
    final File workDir = util.createTempDir(util.getTargetDir(), "kazuki");
    DirSupport.deleteIfExists(workDir.toPath());
    when(applicationDirectories.getWorkDirectory(anyString())).thenReturn(workDir);
    final Module testModule = new AbstractModule()
    {
      @Override
      protected void configure() {
        bind(ApplicationDirectories.class).toInstance(applicationDirectories);
        bind(KazukiJobDao.class);
        bind(KVJobStore.class);
      }
    };
    injector = Guice.createInjector(testModule, new QuartzModule());
    injector.injectMembers(this);
  }

  public <T> void dump(TypedSchema<T> schema, final Predicate<T> predicate) throws Exception {
    logger.info("Dump: {}", schema.getName());
    final List<KeyValuePair<T>> kvs = jobDao.locateRecords(schema, predicate);
    for (KeyValuePair<?> kv : kvs) {
      logger.info("{} := {}", kv.getKey(), kv.getValue());
    }
  }

  public void dumpAll() throws Exception {
    logger.info("Dump ALL");
    dump(CalendarRecord.SCHEMA, null);
    dump(JobDetailRecord.SCHEMA, null);
    dump(TriggerRecord.SCHEMA, null);
  }

  @Test
  public void smoke() throws Exception {
    jobStore.initialize(null, schedulerSignaler);
    try {
      final OperableTrigger t1 = (OperableTrigger) newTrigger().withIdentity("t1").build();
      jobStore.storeTrigger(t1, false);

      final OperableTrigger t2 = jobStore.retrieveTrigger(TriggerKey.triggerKey("t1"));
      assertThat(t2, notNullValue());
      assertThat(t2.getKey(), equalTo(t1.getKey()));
      dumpAll();
      assertThat(jobStore.removeTrigger(TriggerKey.triggerKey("t1")), is(true));

      final OperableTrigger t3 = jobStore.retrieveTrigger(TriggerKey.triggerKey("t1"));
      assertThat(t3, nullValue());
    }
    finally {
      jobStore.clearAllSchedulingData();
      jobStore.shutdown();
    }
  }

  @Test(expected = ObjectAlreadyExistsException.class)
  public void triggerConflict() throws Exception {
    jobStore.initialize(null, schedulerSignaler);
    try {
      final OperableTrigger t1 = (OperableTrigger) newTrigger().withIdentity("t1").build();
      jobStore.storeTrigger(t1, false);
      final OperableTrigger t2 = jobStore.retrieveTrigger(TriggerKey.triggerKey("t1"));
      jobStore.storeTrigger(t2, false);
    }
    finally {
      jobStore.clearAllSchedulingData();
      jobStore.shutdown();
    }
  }

}
