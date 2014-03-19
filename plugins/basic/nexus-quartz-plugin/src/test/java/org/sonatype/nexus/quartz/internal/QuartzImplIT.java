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

package org.sonatype.nexus.quartz.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.configuration.application.ApplicationDirectories;
import org.sonatype.nexus.quartz.internal.guice.QuartzModule;
import org.sonatype.nexus.quartz.internal.store.CalendarRecord;
import org.sonatype.nexus.quartz.internal.store.JobDetailRecord;
import org.sonatype.nexus.quartz.internal.store.KVJobStore;
import org.sonatype.nexus.quartz.internal.store.KazukiJobDao;
import org.sonatype.nexus.quartz.internal.store.TriggerRecord;
import org.sonatype.nexus.quartz.internal.store.TypedSchema;
import org.sonatype.nexus.util.file.DirSupport;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import io.kazuki.v0.store.keyvalue.KeyValuePair;
import org.eclipse.sisu.BeanEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobPersistenceException;
import org.quartz.Trigger;
import org.quartz.impl.calendar.HolidayCalendar;
import org.quartz.spi.SchedulerSignaler;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Simple IT for {@link QuartzImplIT} involving job executions and verifying backing store for changes.
 */
public class QuartzImplIT
    extends TestSupport
{
  private Injector injector;

  @Mock
  private ApplicationDirectories applicationDirectories;

  @Mock
  private SchedulerSignaler schedulerSignaler;

  @Inject
  private KazukiJobDao jobDao;

  @Inject
  private KVJobStore kvJobStore;

  private final Job1 job1;

  private final Job2 job2;

  private final Job3 job3;

  private final PersistingJob persistingJob;

  private QuartzSupportImpl quartz;

  public QuartzImplIT() {
    this.job1 = new Job1();
    this.job2 = new Job2();
    this.job3 = new Job3();
    this.persistingJob = new PersistingJob();
  }

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
      }
    };
    injector = Guice.createInjector(testModule, new QuartzModule());
    injector.injectMembers(this);

    BeanEntry<Named, Job> job1be = mock(BeanEntry.class);
    when(job1be.getImplementationClass()).thenAnswer(new Answer<Object>()
    {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        return Job1.class;
      }
    });
    when(job1be.getProvider()).thenReturn(new Provider<Job>()
    {
      @Override
      public Job get() {
        return job1;
      }
    });
    BeanEntry<Named, Job> job2be = mock(BeanEntry.class);
    when(job2be.getImplementationClass()).thenAnswer(new Answer<Object>()
    {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        return Job2.class;
      }
    });
    when(job2be.getProvider()).thenReturn(new Provider<Job>()
    {
      @Override
      public Job get() {
        return job2;
      }
    });
    BeanEntry<Named, Job> job3be = mock(BeanEntry.class);
    when(job3be.getImplementationClass()).thenAnswer(new Answer<Object>()
    {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        return Job3.class;
      }
    });
    when(job3be.getProvider()).thenReturn(new Provider<Job>()
    {
      @Override
      public Job get() {
        return job3;
      }
    });
    BeanEntry<Named, Job> persistingJobBe = mock(BeanEntry.class);
    when(persistingJobBe.getImplementationClass()).thenAnswer(new Answer<Object>()
    {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        return PersistingJob.class;
      }
    });
    when(persistingJobBe.getProvider()).thenReturn(new Provider<Job>()
    {
      @Override
      public Job get() {
        return persistingJob;
      }
    });
    final ArrayList<BeanEntry<Named, Job>> jobs = Lists.newArrayList();
    jobs.add(job1be);
    jobs.add(job2be);
    jobs.add(job3be);
    jobs.add(persistingJobBe);
    this.quartz = new QuartzSupportImpl(jobs, kvJobStore);
    this.quartz.start();
  }

  @After
  public void cleanUp() throws Exception {
    quartz.stop();
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

  protected void verifyCalendarCount(int expected) throws Exception {
    final List<KeyValuePair<CalendarRecord>> records = jobDao
        .readCalendars(null, Functions.<CalendarRecord>identity());
    if (records.size() != expected) {
      logger.error("Expected {} calendar, but found {} : {}", expected, records.size(), records);
      dump(CalendarRecord.SCHEMA, null);
      assertThat(records, hasSize(expected));
    }
  }

  protected void verifyTriggerCount(int expected) throws Exception {
    final List<KeyValuePair<TriggerRecord>> records = jobDao
        .readOperableTriggers(null, Functions.<TriggerRecord>identity());
    if (records.size() != expected) {
      logger.error("Expected {} trigger, but found {} : {}", expected, records.size(), records);
      dump(TriggerRecord.SCHEMA, null);
      assertThat(records, hasSize(expected));
    }
  }

  protected void verifyJobDetailCount(int expected) throws Exception {
    final List<KeyValuePair<JobDetailRecord>> records = jobDao
        .readJobDetails(null, Functions.<JobDetailRecord>identity());
    if (records.size() != expected) {
      logger.error("Expected {} job, but found {} : {}", expected, records.size(), records);
      dump(JobDetailRecord.SCHEMA, null);
      assertThat(records, hasSize(expected));
    }
  }

  /**
   * When job is done, it should be cleaned up from store.
   */
  @Test
  public void doneJobsCleanedUp() throws Exception {
    verifyCalendarCount(0);
    verifyTriggerCount(0);
    verifyJobDetailCount(0);

    quartz.execute(Job1.class);
    quartz.execute(Job1.class);
    quartz.execute(Job1.class);
    Thread.sleep(1000L);
    verifyCalendarCount(0);
    verifyTriggerCount(0);
    verifyJobDetailCount(0);
    dumpAll();
  }

  /**
   * When "durable" job is done, it should NOT be cleaned up from store, only it's triggers.
   */
  @Test
  public void durableDoneJobsNotCleanedUp() throws Exception {
    verifyCalendarCount(0);
    verifyTriggerCount(0);
    verifyJobDetailCount(0);

    final JobDetail job1 = newJob(Job1.class).storeDurably().build();
    final Trigger trigger1 = newTrigger().forJob(job1.getKey()).startNow().build();
    quartz.getScheduler().scheduleJob(job1, trigger1);

    final JobDetail job2 = newJob(Job1.class).storeDurably().build();
    final Trigger trigger2 = newTrigger().forJob(job2.getKey()).startNow().build();
    quartz.getScheduler().scheduleJob(job2, trigger2);

    final JobDetail job3 = newJob(Job1.class).storeDurably().build();
    final Trigger trigger3 = newTrigger().forJob(job3.getKey()).startNow().build();
    quartz.getScheduler().scheduleJob(job3, trigger3);
    Thread.sleep(1000L);
    verifyCalendarCount(0);
    verifyTriggerCount(0);
    verifyJobDetailCount(3);
    dumpAll();
  }

  /**
   * "Smoke" integrity test, calender added, referenced and should not be allowed to be removed (until ref exists).
   */
  @Test
  public void smoke() throws Exception {
    verifyCalendarCount(0);
    verifyTriggerCount(0);
    verifyJobDetailCount(0);

    quartz.getScheduler().addCalendar("sample", new HolidayCalendar(), false, true);
    verifyCalendarCount(1);
    verifyTriggerCount(0);
    verifyJobDetailCount(0);

    final JobDetail job2 = newJob(Job2.class).withIdentity("foo", "bar").build();
    final Trigger trigger2 = newTrigger().withIdentity("foo", "bar").forJob(job2.getKey()).modifiedByCalendar("sample")
        .startNow().build();
    quartz.getScheduler().scheduleJob(job2, trigger2);
    verifyCalendarCount(1);
    verifyTriggerCount(1);
    verifyJobDetailCount(1);

    try {
      quartz.getScheduler().deleteCalendar("sample");
    }
    catch (JobPersistenceException e) {
      // good, is referenced by trigger2
    }

    quartz.execute(Job3.class);
    verifyCalendarCount(1);
    verifyTriggerCount(2);
    verifyJobDetailCount(2);

    quartz.getScheduler().deleteJob(job2.getKey());
    quartz.getScheduler().unscheduleJob(trigger2.getKey());
    quartz.getScheduler().deleteCalendar("sample");

    // xenu was here, OP 12
    Thread.sleep(2000L);

    verifyCalendarCount(0);
    verifyTriggerCount(0);
    verifyJobDetailCount(0);
  }

  /**
   * Persisting Job that tests JobDetailMap persistence is done by JobStore.
   */
  @Test
  public void persistData() throws Exception {
    final JobDetail job = newJob(PersistingJob.class).withIdentity("foo", "bar").storeDurably().build();
    // 4 runs: 1 + 3 repeats
    final Trigger trigger = newTrigger().withIdentity("foo", "bar").forJob(job.getKey())
        .startNow().withSchedule(simpleSchedule().withIntervalInMilliseconds(500).withRepeatCount(3)).build();
    quartz.getScheduler().scheduleJob(job, trigger);
    Thread.sleep(3000L);
    final JobDetail doneJob = quartz.getScheduler().getJobDetail(job.getKey());
    assertThat(doneJob.getJobDataMap().get("RUN"), notNullValue());
    assertThat(doneJob.getJobDataMap().get("RUN").toString(), equalTo("4"));
  }
}
