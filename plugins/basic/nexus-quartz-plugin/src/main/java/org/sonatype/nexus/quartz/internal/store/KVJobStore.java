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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.quartz.internal.store.TriggerWrapper.State;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.lifecycle.Lifecycle;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.kazuki.v0.store.KazukiException;
import io.kazuki.v0.store.Key;
import io.kazuki.v0.store.keyvalue.KeyValuePair;
import org.quartz.Calendar;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobPersistenceException;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.SchedulerConfigException;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.JobStore;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.spi.TriggerFiredBundle;
import org.quartz.spi.TriggerFiredResult;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static org.sonatype.nexus.quartz.internal.store.Predicates.byCalendarName;
import static org.sonatype.nexus.quartz.internal.store.Predicates.byCalendarRef;
import static org.sonatype.nexus.quartz.internal.store.Predicates.byJobDetailRef;
import static org.sonatype.nexus.quartz.internal.store.Predicates.byJobKey;
import static org.sonatype.nexus.quartz.internal.store.Predicates.byJobKeyMatcher;
import static org.sonatype.nexus.quartz.internal.store.Predicates.byTriggerKey;
import static org.sonatype.nexus.quartz.internal.store.Predicates.byTriggerKeyMatcher;
import static org.sonatype.nexus.quartz.internal.store.Predicates.inAnyStateOf;
import static org.sonatype.nexus.quartz.internal.store.Predicates.single;

/**
 * KV backed {@link JobStore} implementation. It uses {@link KazukiJobDao} to implement {@link JobStore}, and all the
 * logical constraints and relations are handled in this class.
 *
 * @since 2.8
 */
@Singleton
@Named
public class KVJobStore
    extends ComponentSupport
    implements JobStore
{
  private final KazukiJobDao jobDao;

  /**
   * Lock object as RAMJobStore does. Unsure why not synchronized methods, maybe Quartz internally
   * uses JobStore as monitor somewhere, unsure.
   */
  private final Object lock;

  private SchedulerSignaler signaler;

  private long misfireThreshold = 5000L;

  @Inject
  public KVJobStore(final KazukiJobDao jobDao)
      throws Exception
  {
    this.jobDao = checkNotNull(jobDao);
    this.lock = new Object();
  }

  // Extra

  public long getMisfireThreshold() {
    return misfireThreshold;
  }

  public void setMisfireThreshold(final long misfireThreshold) {
    if (misfireThreshold < 1L) {
      throw new IllegalArgumentException("Misfire threshold must be larger than 0");
    }
    this.misfireThreshold = misfireThreshold;
  }

  private static final AtomicLong ftrCtr = new AtomicLong(System.currentTimeMillis());

  protected String getFiredTriggerRecordId() {
    // TODO: kazuki sequence?
    return String.valueOf(ftrCtr.incrementAndGet());
  }

  // JobStore

  @Override
  public void setInstanceId(final String schedInstId) {
    // nop
  }

  @Override
  public void setInstanceName(final String schedName) {
    // nop
  }

  @Override
  public void setThreadPoolSize(final int poolSize) {
    // nop
  }

  @Override
  public void initialize(final ClassLoadHelper loadHelper, final SchedulerSignaler signaler)
      throws SchedulerConfigException
  {
    this.signaler = checkNotNull(signaler);
    try {
      if (jobDao instanceof Lifecycle) {
        ((Lifecycle) jobDao).start();
      }
    }
    catch (Exception e) {
      Throwables.propagate(e);
    }
  }

  @Override
  public void schedulerStarted() throws SchedulerException {
    // nop
  }

  @Override
  public void schedulerPaused() {
    // nop
  }

  @Override
  public void schedulerResumed() {
    // nop
  }

  @Override
  public void shutdown() {
    try {
      if (jobDao instanceof Lifecycle) {
        ((Lifecycle) jobDao).stop();
      }
    }
    catch (Exception e) {
      Throwables.propagate(e);
    }
  }

  @Override
  public boolean supportsPersistence() {
    return true;
  }

  @Override
  public long getEstimatedTimeToReleaseAndAcquireTrigger() {
    return 15; // TODO: ???
  }

  @Override
  public boolean isClustered() {
    return false;
  }

  @Override
  public void storeJobAndTrigger(final JobDetail newJob, final OperableTrigger newTrigger)
      throws JobPersistenceException
  {
    synchronized (lock) {
      storeJob(newJob, false);
      storeTrigger(newTrigger, false);
    }
  }

  @Override
  public void storeJob(final JobDetail newJob, final boolean replaceExisting)
      throws JobPersistenceException
  {
    synchronized (lock) {
      try {
        final KeyValuePair<JobDetail> existing = single(jobDao.readJobDetails(byJobKey(newJob.getKey())));
        if (existing != null) {
          if (!replaceExisting) {
            throw new ObjectAlreadyExistsException(newJob);
          }
          else {
            jobDao.updateJobDetail(existing.getKey(), newJob);
          }
        }
        else {
          jobDao.createJobDetail(newJob);
        }
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public void storeJobsAndTriggers(final Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, final boolean replace)
      throws JobPersistenceException
  {
    synchronized (lock) {
      if (!replace) {
        for (Entry<JobDetail, Set<? extends Trigger>> e : triggersAndJobs.entrySet()) {
          if (checkExists(e.getKey().getKey())) {
            throw new ObjectAlreadyExistsException(e.getKey());
          }
          for (Trigger trigger : e.getValue()) {
            if (checkExists(trigger.getKey())) {
              throw new ObjectAlreadyExistsException(trigger);
            }
          }
        }
      }
      for (Entry<JobDetail, Set<? extends Trigger>> e : triggersAndJobs.entrySet()) {
        storeJob(e.getKey(), true);
        for (Trigger trigger : e.getValue()) {
          storeTrigger((OperableTrigger) trigger, true);
        }
      }
    }
  }

  @Override
  public boolean removeJob(final JobKey jobKey) throws JobPersistenceException {
    synchronized (lock) {
      final List<OperableTrigger> triggersOfJob = getTriggersForJob(jobKey);
      for (OperableTrigger trig : triggersOfJob) {
        removeTrigger(trig.getKey());
      }
      try {
        final KeyValuePair<JobDetail> existing = single(jobDao.readJobDetails(byJobKey(jobKey)));
        if (existing != null) {
          return jobDao.deleteJobDetail(existing.getKey());
        }
        else {
          return false;
        }
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public boolean removeJobs(final List<JobKey> jobKeys) throws JobPersistenceException {
    synchronized (lock) {
      boolean allFound = true;
      for (JobKey key : jobKeys) {
        allFound = removeJob(key) && allFound;
      }
      return allFound;
    }
  }

  @Override
  public JobDetail retrieveJob(final JobKey jobKey) throws JobPersistenceException {
    synchronized (lock) {
      try {
        final KeyValuePair<JobDetail> existing = single(jobDao.readJobDetails(byJobKey(jobKey)));
        if (existing != null) {
          return existing.getValue();
        }
        else {
          return null;
        }
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public void storeTrigger(final OperableTrigger newTrigger, final boolean replaceExisting)
      throws JobPersistenceException
  {
    synchronized (lock) {
      try {
        final KeyValuePair<TriggerWrapper> existing = single(
            jobDao.readOperableTriggers(byTriggerKey(newTrigger.getKey())));
        if (existing != null) {
          if (!replaceExisting) {
            throw new ObjectAlreadyExistsException(newTrigger);
          }
          else {
            jobDao.updateOperableTrigger(existing.getKey(),
                new TriggerWrapper(newTrigger, existing.getValue().getState()));
          }
        }
        else {
          jobDao.createOperableTrigger(new TriggerWrapper(newTrigger, State.WAITING));
        }
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public boolean removeTrigger(final TriggerKey triggerKey) throws JobPersistenceException {
    synchronized (lock) {
      try {
        final KeyValuePair<TriggerWrapper> existing = single(
            jobDao.readOperableTriggers(byTriggerKey(triggerKey)));
        if (existing != null) {
          return removeTrigger(existing);
        }
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
      return false;
    }
  }

  /**
   * Removes trigger and it's referenced job if the job has no more triggers referencing it (nothing is about to
   * trigger the job execution anymore). This method must be called from protected region.
   */
  private boolean removeTrigger(final KeyValuePair<TriggerWrapper> existing) throws KazukiException {
    final boolean result = jobDao.deleteOperableTrigger(existing.getKey());
    final JobKey jobKey = existing.getValue().getOperableTrigger().getJobKey();
    if (jobKey != null) {
      final List<KeyValuePair<TriggerWrapper>> triggersForSameJob = jobDao
          .readOperableTriggers(byJobDetailRef(jobKey));
      if (triggersForSameJob.isEmpty()) {
        final KeyValuePair<JobDetail> job = single(jobDao.readJobDetails(byJobKey(jobKey)));
        if (job != null && !job.getValue().isDurable()) {
          jobDao.deleteJobDetail(job.getKey());
        }
        signaler.notifySchedulerListenersJobDeleted(jobKey);
      }
    }
    return result;
  }

  @Override
  public boolean removeTriggers(final List<TriggerKey> triggerKeys) throws JobPersistenceException {
    synchronized (lock) {
      boolean allFound = true;
      for (TriggerKey key : triggerKeys) {
        allFound = removeTrigger(key) && allFound;
      }
      return allFound;
    }
  }

  @Override
  public boolean replaceTrigger(final TriggerKey triggerKey, final OperableTrigger newTrigger)
      throws JobPersistenceException
  {
    synchronized (lock) {
      try {
        final KeyValuePair<TriggerWrapper> existing = single(
            jobDao.readOperableTriggers(byTriggerKey(triggerKey)));
        if (existing != null) {
          if (!existing.getValue().getOperableTrigger().getJobKey().equals(newTrigger.getJobKey())) {
            throw new JobPersistenceException("New trigger is not related to the same job as the old trigger.");
          }
          jobDao
              .updateOperableTrigger(existing.getKey(), new TriggerWrapper(newTrigger, existing.getValue().getState()));
          return true;
        }
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
      return false;
    }
  }

  @Override
  public OperableTrigger retrieveTrigger(final TriggerKey triggerKey) throws JobPersistenceException {
    synchronized (lock) {
      try {
        final KeyValuePair<TriggerWrapper> kv = single(jobDao.readOperableTriggers(byTriggerKey(triggerKey)));
        if (kv != null) {
          return kv.getValue().getOperableTrigger();
        }
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
      return null;
    }
  }

  @Override
  public boolean checkExists(final JobKey jobKey) throws JobPersistenceException {
    synchronized (lock) {
      try {
        return single(jobDao.readJobDetails(byJobKey(jobKey), Functions.<JobDetailRecord>identity())) != null;
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public boolean checkExists(final TriggerKey triggerKey) throws JobPersistenceException {
    synchronized (lock) {
      try {
        return single(jobDao.readOperableTriggers(byTriggerKey(triggerKey), Functions.<TriggerRecord>identity())) !=
            null;
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public void clearAllSchedulingData() throws JobPersistenceException {
    synchronized (lock) {
      try {
        jobDao.nukeStore();
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public void storeCalendar(final String name, final Calendar calendar, final boolean replaceExisting,
                            final boolean updateTriggers)
      throws JobPersistenceException
  {
    synchronized (lock) {
      try {
        final KeyValuePair<Calendar> existing = single(jobDao.readCalendars(byCalendarName(name)));
        if (existing != null) {
          if (!replaceExisting) {
            throw new ObjectAlreadyExistsException("Calendar with name '" + name + "' already exists");
          }
          jobDao.updateCalendar(existing.getKey(), name, calendar);
        }
        else {
          jobDao.createCalendar(name, calendar);
        }
        if (updateTriggers) {
          final List<KeyValuePair<TriggerWrapper>> triggers = jobDao
              .readOperableTriggers(byCalendarRef(name));
          for (KeyValuePair<TriggerWrapper> trigger : triggers) {
            trigger.getValue().getOperableTrigger().updateWithNewCalendar(calendar, getMisfireThreshold());
            jobDao.updateOperableTrigger(trigger.getKey(), trigger.getValue());
          }
        }
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public boolean removeCalendar(final String calName) throws JobPersistenceException {
    synchronized (lock) {
      try {
        final List<KeyValuePair<TriggerWrapper>> refTriggers =
            jobDao.readOperableTriggers(byCalendarRef(calName));
        if (!refTriggers.isEmpty()) {
          throw new JobPersistenceException(
              "Calendar cannot be removed, is referenced by " + refTriggers.size() + " Trigger!");
        }
        final KeyValuePair<Calendar> existing = single(jobDao.readCalendars(byCalendarName(calName)));
        if (existing != null) {
          return jobDao.deleteCalendar(existing.getKey());
        }
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
      return false;
    }
  }

  @Override
  public Calendar retrieveCalendar(final String calName) throws JobPersistenceException {
    synchronized (lock) {
      try {
        final KeyValuePair<Calendar> kv = single(jobDao.readCalendars(byCalendarName(calName)));
        if (kv != null) {
          return kv.getValue();
        }
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
      return null;
    }
  }

  @Override
  public int getNumberOfJobs() throws JobPersistenceException {
    synchronized (lock) {
      try {
        return jobDao.readJobDetails(null).size();
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public int getNumberOfTriggers() throws JobPersistenceException {
    synchronized (lock) {
      try {
        return jobDao.readOperableTriggers(null).size();
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public int getNumberOfCalendars() throws JobPersistenceException {
    synchronized (lock) {
      try {
        return jobDao.readCalendars(null).size();
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public Set<JobKey> getJobKeys(final GroupMatcher<JobKey> matcher) throws JobPersistenceException {
    synchronized (lock) {
      try {
        final List<KeyValuePair<JobDetail>> kvs = jobDao.readJobDetails(byJobKeyMatcher(matcher));
        return ImmutableSet.copyOf(
            Collections2.transform(kvs, new Function<KeyValuePair<JobDetail>, JobKey>()
            {
              @Override
              public JobKey apply(final KeyValuePair<JobDetail> input) {
                return input.getValue().getKey();
              }
            }));
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public Set<TriggerKey> getTriggerKeys(final GroupMatcher<TriggerKey> matcher) throws JobPersistenceException {
    synchronized (lock) {
      try {
        final List<KeyValuePair<TriggerWrapper>> kvs = jobDao
            .readOperableTriggers(byTriggerKeyMatcher(matcher));
        return ImmutableSet.copyOf(
            Collections2.transform(kvs, new Function<KeyValuePair<TriggerWrapper>, TriggerKey>()
            {
              @Override
              public TriggerKey apply(final KeyValuePair<TriggerWrapper> input) {
                return input.getValue().getOperableTrigger().getKey();
              }
            }));
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public List<String> getJobGroupNames() throws JobPersistenceException {
    synchronized (lock) {
      try {
        final List<KeyValuePair<JobDetail>> kvs = jobDao.readJobDetails(null);
        final List<String> groups = Lists.newArrayList();
        for (KeyValuePair<JobDetail> kv : kvs) {
          final String group = kv.getValue().getKey().getGroup();
          if (!groups.contains(group)) {
            groups.add(group);
          }
        }
        return groups;
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public List<String> getTriggerGroupNames() throws JobPersistenceException {
    synchronized (lock) {
      try {
        final List<KeyValuePair<TriggerWrapper>> kvs = jobDao.readOperableTriggers(null);
        final List<String> groups = Lists.newArrayList();
        for (KeyValuePair<TriggerWrapper> kv : kvs) {
          final String group = kv.getValue().getOperableTrigger().getKey().getGroup();
          if (!groups.contains(group)) {
            groups.add(group);
          }
        }
        return groups;
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public List<String> getCalendarNames() throws JobPersistenceException {
    synchronized (lock) {
      try {
        final List<KeyValuePair<String>> kvs = jobDao.readCalendars(null, jobDao.toCalendarName());
        final List<String> names = Lists.newArrayListWithCapacity(kvs.size());
        for (KeyValuePair<String> kv : kvs) {
          names.add(kv.getValue());
        }
        return names;
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public List<OperableTrigger> getTriggersForJob(final JobKey jobKey) throws JobPersistenceException {
    synchronized (lock) {
      try {
        final List<KeyValuePair<TriggerWrapper>> kvs = jobDao.readOperableTriggers(byJobDetailRef(jobKey));
        return ImmutableList.copyOf(
            Collections2.transform(kvs, new Function<KeyValuePair<TriggerWrapper>, OperableTrigger>()
            {
              @Override
              public OperableTrigger apply(final KeyValuePair<TriggerWrapper> input) {
                return input.getValue().getOperableTrigger();
              }
            }));
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public TriggerState getTriggerState(final TriggerKey triggerKey) throws JobPersistenceException {
    synchronized (lock) {
      try {
        final KeyValuePair<TriggerRecord> kv = single(
            jobDao.readOperableTriggers(byTriggerKey(triggerKey), Functions.<TriggerRecord>identity()));
        if (kv == null) {
          return TriggerState.NONE;
        }
        final State state = kv.getValue().getState();
        if (state == State.COMPLETE) {
          return TriggerState.COMPLETE;
        }
        if (state == State.PAUSED) {
          return TriggerState.PAUSED;
        }
        if (state == State.PAUSED_BLOCKED) {
          return TriggerState.PAUSED;
        }
        if (state == State.BLOCKED) {
          return TriggerState.BLOCKED;
        }
        if (state == State.ERROR) {
          return TriggerState.ERROR;
        }
        return TriggerState.NORMAL;
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public void pauseTrigger(final TriggerKey triggerKey) throws JobPersistenceException {
    synchronized (lock) {
      try {
        final KeyValuePair<TriggerWrapper> triggerState = single(
            jobDao.readOperableTriggers(byTriggerKey(triggerKey)));
        if (triggerState == null) {
          return;
        }
        final State state = triggerState.getValue().getState();
        final State newState;
        if (state == State.COMPLETE) {
          return;
        }
        if (state == State.BLOCKED) {
          newState = State.PAUSED_BLOCKED;
        }
        else {
          newState = State.PAUSED;
        }
        jobDao.updateOperableTrigger(triggerState.getKey(), triggerState.getValue().transitionTo(newState));
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public Collection<String> pauseTriggers(final GroupMatcher<TriggerKey> groupMatcher) throws JobPersistenceException {
    synchronized (lock) {
      final Set<String> pausedGroups = Sets.newHashSet();
      for (TriggerKey triggerKey : getTriggerKeys(groupMatcher)) {
        pausedGroups.add(triggerKey.getGroup());
      }
      for (String groupName : pausedGroups) {
        for (TriggerKey triggerKey : getTriggerKeys(GroupMatcher.triggerGroupEquals(groupName))) {
          pauseTrigger(triggerKey);
        }
      }
      return pausedGroups;
    }
  }

  @Override
  public void pauseJob(final JobKey jobKey) throws JobPersistenceException {
    synchronized (lock) {
      for (OperableTrigger trigger : getTriggersForJob(jobKey)) {
        pauseTrigger(trigger.getKey());
      }
    }
  }

  @Override
  public Collection<String> pauseJobs(final GroupMatcher<JobKey> groupMatcher) throws JobPersistenceException {
    synchronized (lock) {
      final Set<String> pausedGroups = Sets.newHashSet();
      for (JobKey jobKey : getJobKeys(groupMatcher)) {
        pausedGroups.add(jobKey.getGroup());
      }
      for (String groupName : pausedGroups) {
        for (JobKey jobKey : getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
          pauseJob(jobKey);
        }
      }
      return pausedGroups;
    }
  }

  @Override
  public void resumeTrigger(final TriggerKey triggerKey) throws JobPersistenceException {
    synchronized (lock) {
      try {
        final KeyValuePair<TriggerWrapper> trigger = single(jobDao.readOperableTriggers(
            and(
                byTriggerKey(triggerKey),
                inAnyStateOf(State.PAUSED, State.PAUSED_BLOCKED)
            )
        ));
        if (trigger == null) {
          return;
        }
        State newState = State.WAITING;
        if (trigger.getValue().getState() == State.PAUSED_BLOCKED) {
          newState = State.BLOCKED;
        }
        applyMisfire(trigger.getKey());
        jobDao.updateOperableTrigger(trigger.getKey(), trigger.getValue().transitionTo(newState));
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public Collection<String> resumeTriggers(final GroupMatcher<TriggerKey> groupMatcher) throws JobPersistenceException {
    synchronized (lock) {
      final Set<String> resumedGroups = Sets.newHashSet();
      for (TriggerKey triggerKey : getTriggerKeys(groupMatcher)) {
        resumedGroups.add(triggerKey.getGroup());
      }
      for (String groupName : resumedGroups) {
        for (TriggerKey triggerKey : getTriggerKeys(GroupMatcher.triggerGroupEquals(groupName))) {
          resumeTrigger(triggerKey);
        }
      }
      return resumedGroups;
    }
  }

  @Override
  public Set<String> getPausedTriggerGroups() throws JobPersistenceException {
    synchronized (lock) {
      try {
        final Set<String> pausedGroups = Sets.newHashSet();
        final List<KeyValuePair<TriggerRecord>> triggerStates =
            jobDao.readOperableTriggers(inAnyStateOf(
                State.PAUSED, State.PAUSED_BLOCKED),
                Functions.<TriggerRecord>identity());
        for (KeyValuePair<TriggerRecord> triggerState : triggerStates) {
          pausedGroups.add(triggerState.getValue().getGroup());
        }
        return pausedGroups;
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public void resumeJob(final JobKey jobKey) throws JobPersistenceException {
    synchronized (lock) {
      List<OperableTrigger> triggersOfJob = getTriggersForJob(jobKey);
      for (OperableTrigger trigger : triggersOfJob) {
        resumeTrigger(trigger.getKey());
      }
    }
  }

  @Override
  public Collection<String> resumeJobs(final GroupMatcher<JobKey> groupMatcher) throws JobPersistenceException {
    synchronized (lock) {
      final Set<String> resumedGroups = Sets.newHashSet();
      for (JobKey jobKey : getJobKeys(groupMatcher)) {
        resumedGroups.add(jobKey.getGroup());
      }
      for (String groupName : resumedGroups) {
        for (JobKey jobKey : getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
          resumeJob(jobKey);
        }
      }
      return resumedGroups;
    }
  }

  @Override
  public void pauseAll() throws JobPersistenceException {
    synchronized (lock) {
      for (TriggerKey triggerKey : getTriggerKeys(GroupMatcher.<TriggerKey>anyGroup())) {
        pauseTrigger(triggerKey);
      }
    }
  }

  @Override
  public void resumeAll() throws JobPersistenceException {
    synchronized (lock) {
      for (TriggerKey triggerKey : getTriggerKeys(GroupMatcher.<TriggerKey>anyGroup())) {
        resumeTrigger(triggerKey);
      }
    }
  }

  @Override
  public List<OperableTrigger> acquireNextTriggers(final long noLaterThan,
                                                   final int maxCount,
                                                   final long timeWindow)
      throws JobPersistenceException
  {
    synchronized (lock) {
      List<OperableTrigger> result = Lists.newArrayList();
      Set<JobKey> jobsAcquired = Sets.newHashSet();
      try {
        final List<KeyValuePair<TriggerWrapper>> triggerPairs = jobDao.readOperableTriggers(
            inAnyStateOf(State.WAITING)
        );
        for (KeyValuePair<TriggerWrapper> triggerPair : triggerPairs) {
          final OperableTrigger trigger = triggerPair.getValue().getOperableTrigger();

          if (trigger.getNextFireTime() == null) {
            continue;
          }
          if (applyMisfire(triggerPair.getKey())) {
            continue;
          }
          if (trigger.getNextFireTime().getTime() > noLaterThan + timeWindow) {
            continue;
          }

          final JobKey jobKey = trigger.getJobKey();
          final KeyValuePair<JobDetail> job = single(jobDao.readJobDetails(byJobKey(jobKey)));
          if (job.getValue().isConcurrentExectionDisallowed()) {
            if (jobsAcquired.contains(jobKey)) {
              continue; // skip this trigger, as Job it belongs to is already acquired
            }
            else {
              jobsAcquired.add(jobKey);
            }
          }

          jobDao.updateOperableTrigger(
              triggerPair.getKey(),
              triggerPair.getValue().transitionTo(State.ACQUIRED)
          );
          trigger.setFireInstanceId(getFiredTriggerRecordId());
          result.add(trigger);
          if (result.size() == maxCount) {
            break;
          }
        }
        return result;
      }
      catch (KazukiException e) {
        // No JobPersistenceException in signature?
        throw Throwables.propagate(e);
      }
    }
  }

  @Override
  public void releaseAcquiredTrigger(final OperableTrigger trigger) {
    synchronized (lock) {
      try {
        final KeyValuePair<TriggerWrapper> triggerState = single(jobDao.readOperableTriggers(
            and(
                byTriggerKey(trigger.getKey()),
                inAnyStateOf(State.ACQUIRED)
            )
        ));
        if (triggerState == null) {
          return;
        }
        jobDao.updateOperableTrigger(triggerState.getKey(), triggerState.getValue().transitionTo(State.WAITING));
      }
      catch (KazukiException e) {
        // No JobPersistenceException in signature?
        throw Throwables.propagate(e);
      }
    }
  }

  @Override
  public List<TriggerFiredResult> triggersFired(final List<OperableTrigger> firedTriggers)
      throws JobPersistenceException
  {
    synchronized (lock) {
      final List<TriggerFiredResult> results = Lists.newArrayList();
      try {
        for (OperableTrigger firedTrigger : firedTriggers) {

          final KeyValuePair<TriggerWrapper> trigger = single(
              jobDao.readOperableTriggers(byTriggerKey(firedTrigger.getKey())));

          if (trigger == null) {
            continue; // was deleted since
          }
          if (trigger.getValue().getState() != State.ACQUIRED) {
            continue; // completed, paused, blocked since acquired
          }

          Calendar cal = null;
          if (trigger.getValue().getOperableTrigger().getCalendarName() != null) {
            cal = retrieveCalendar(trigger.getValue().getOperableTrigger().getCalendarName());
            if (cal == null) {
              continue;
            }
          }
          final Date prevFireTime = trigger.getValue().getOperableTrigger().getPreviousFireTime();
          // update both instance, "our" and "scheduler" (input param)
          trigger.getValue().getOperableTrigger().triggered(cal);
          firedTrigger.triggered(cal);
          jobDao.updateOperableTrigger(trigger.getKey(), trigger.getValue().transitionTo(State.WAITING));

          // mark other triggers pointing to this same job as blocked if there is no
          // concurrent execution allowed for this job
          final JobDetail jobDetail = retrieveJob(trigger.getValue().getOperableTrigger().getJobKey());
          if (jobDetail.isConcurrentExectionDisallowed()) {
            final List<KeyValuePair<TriggerWrapper>> triggerStatesForJob = jobDao.readOperableTriggers(
                and(
                    byJobDetailRef(jobDetail.getKey()),
                    inAnyStateOf(State.WAITING, State.PAUSED)
                )
            );
            for (KeyValuePair<TriggerWrapper> triggerStateForJob : triggerStatesForJob) {
              if (trigger.getKey().equals(triggerStateForJob.getKey())) {
                continue; // skip "this" trigger just being transitioned
              }
              State newState = State.BLOCKED;
              if (State.PAUSED == triggerStateForJob.getValue().getState()) {
                newState = State.PAUSED_BLOCKED;
              }
              jobDao.updateOperableTrigger(triggerStateForJob.getKey(),
                  triggerStateForJob.getValue().transitionTo(newState));
            }
          }
          final TriggerFiredBundle bundle = new TriggerFiredBundle(
              jobDetail,
              trigger.getValue().getOperableTrigger(),
              cal,
              false,
              new Date(),
              trigger.getValue().getOperableTrigger().getPreviousFireTime(),
              prevFireTime,
              trigger.getValue().getOperableTrigger().getNextFireTime()
          );
          results.add(new TriggerFiredResult(bundle));
        }
        return results;
      }
      catch (KazukiException e) {
        throw toJobPersistenceException(e);
      }
    }
  }

  @Override
  public void triggeredJobComplete(final OperableTrigger trigger, final JobDetail jobDetail,
                                   final CompletedExecutionInstruction triggerInstCode)
  {
    synchronized (lock) {
      try {
        final KeyValuePair<TriggerWrapper> triggerKv = single(
            jobDao.readOperableTriggers(byTriggerKey(trigger.getKey())));
        final KeyValuePair<JobDetail> jobDetailKv = single(
            jobDao.readJobDetails(byJobKey(jobDetail.getKey())));

        if (jobDetailKv != null) {
          // persist JobDataMap is needed
          if (jobDetail.isPersistJobDataAfterExecution()) {
            if (jobDetail.getJobDataMap() != null) {
              jobDao.updateJobDetail(jobDetailKv.getKey(), jobDetail);
              jobDetail.getJobDataMap().clearDirtyFlag();
            }
          }
          // manage state if no concurrent execution allowed
          if (jobDetail.isConcurrentExectionDisallowed()) {
            final List<KeyValuePair<TriggerWrapper>> triggerStatesForJob = jobDao.readOperableTriggers(
                and(
                    byJobDetailRef(jobDetail.getKey()),
                    inAnyStateOf(State.BLOCKED, State.PAUSED_BLOCKED)
                )
            );
            for (KeyValuePair<TriggerWrapper> triggerStateForJob : triggerStatesForJob) {
              State newState = State.WAITING;
              if (State.PAUSED_BLOCKED == triggerStateForJob.getValue().getState()) {
                newState = State.PAUSED;
              }
              jobDao.updateOperableTrigger(triggerStateForJob.getKey(),
                  triggerStateForJob.getValue().transitionTo(newState));
            }
            signaler.signalSchedulingChange(0L);
          }
        }

        if (triggerKv != null) {
          if (triggerInstCode == CompletedExecutionInstruction.DELETE_TRIGGER) {
            if (trigger.getNextFireTime() == null) {
              // double check for possible reschedule within job
              // execution, which would cancel the need to delete...
              if (triggerKv.getValue().getOperableTrigger().getNextFireTime() == null) {
                removeTrigger(triggerKv);
              }
            }
            else {
              removeTrigger(triggerKv);
              signaler.signalSchedulingChange(0L);
            }
          }
          else if (triggerInstCode == CompletedExecutionInstruction.SET_TRIGGER_COMPLETE) {
            log.debug("Trigger {} set to COMPLETE state.", trigger.getKey());
            jobDao.updateOperableTrigger(triggerKv.getKey(), triggerKv.getValue().transitionTo(State.COMPLETE));
            signaler.signalSchedulingChange(0L);
          }
          else if (triggerInstCode == CompletedExecutionInstruction.SET_TRIGGER_ERROR) {
            log.info("Trigger {} set to ERROR state.", trigger.getKey());
            jobDao.updateOperableTrigger(triggerKv.getKey(), triggerKv.getValue().transitionTo(State.ERROR));
            signaler.signalSchedulingChange(0L);
          }
          else if (triggerInstCode == CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_ERROR) {
            log.info("All triggers of Job {} set to ERROR state.", trigger.getJobKey());
            final List<KeyValuePair<TriggerWrapper>> triggerStatesForJob = jobDao.readOperableTriggers(
                byJobDetailRef(jobDetail.getKey())
            );
            for (KeyValuePair<TriggerWrapper> triggerStateForJob : triggerStatesForJob) {
              jobDao.updateOperableTrigger(triggerStateForJob.getKey(),
                  triggerStateForJob.getValue().transitionTo(State.ERROR));
            }
            signaler.signalSchedulingChange(0L);
          }
          else if (triggerInstCode == CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_COMPLETE) {
            log.info("All triggers of Job {} set to COMPLETE state.", trigger.getJobKey());
            final List<KeyValuePair<TriggerWrapper>> triggerStatesForJob = jobDao.readOperableTriggers(
                byJobDetailRef(jobDetail.getKey())
            );
            for (KeyValuePair<TriggerWrapper> triggerStateForJob : triggerStatesForJob) {
              jobDao.updateOperableTrigger(triggerStateForJob.getKey(),
                  triggerStateForJob.getValue().transitionTo(State.COMPLETE));
            }
            signaler.signalSchedulingChange(0L);
          }
        }

      }
      catch (KazukiException e) {
        throw Throwables.propagate(e);
      }
    }
  }

  // ==

  /**
   * Wraps {@link KazukiException} into {@link JobPersistenceException} making sure that message is not {@code null}.
   */
  protected JobPersistenceException toJobPersistenceException(final KazukiException e) {
    String message = e.getMessage();
    if (message == null) {
      message = String.valueOf(e);
    }
    return new JobPersistenceException(message, e);
  }

  protected boolean applyMisfire(final Key triggerKzKey) throws KazukiException {
    long misfireTime = System.currentTimeMillis();
    if (getMisfireThreshold() > 0) {
      misfireTime -= getMisfireThreshold();
    }
    final KeyValuePair<TriggerWrapper> record = jobDao.readOperableTrigger(triggerKzKey);
    if (record == null) {
      // all the KV ops should happen within JobStore#lock object, so this must not be possible?
      throw new KazukiException("Kazuki Key did not point to any existing record, locking bug?");
    }
    final OperableTrigger trigger = record.getValue().getOperableTrigger();
    final Date tnft = trigger.getNextFireTime();
    if (tnft == null || tnft.getTime() > misfireTime
        || trigger.getMisfireInstruction() ==
        Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY) {
      return false;
    }
    Calendar cal = null;
    if (trigger.getCalendarName() != null) {
      final KeyValuePair<Calendar> calKv = single(
          jobDao.readCalendars(byCalendarName(trigger.getCalendarName())));
      // TODO: if ref exists but calKV is NULL? Inconsistency in KV store
      if (calKv != null) {
        cal = calKv.getValue();
      }
    }
    signaler.notifyTriggerListenersMisfired(trigger);
    trigger.updateAfterMisfire(cal);
    if (trigger.getNextFireTime() == null) {
      jobDao.updateOperableTrigger(record.getKey(), record.getValue().transitionTo(State.COMPLETE));
      signaler.notifySchedulerListenersFinalized(trigger);
      jobDao.updateOperableTrigger(record.getKey(), record.getValue());
    }
    else if (tnft.equals(trigger.getNextFireTime())) {
      return false;
    }
    return true;
  }
}
