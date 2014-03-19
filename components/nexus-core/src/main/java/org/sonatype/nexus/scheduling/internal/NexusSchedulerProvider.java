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

package org.sonatype.nexus.scheduling.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.scheduling.NoSuchTaskException;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.schedules.Schedule;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.Subscribe;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provider that returns {@link NexusScheduler} instance based on presence of Nexus Quartz Plugin.
 *
 * @since 3.0
 */
@Named
@Singleton
public class NexusSchedulerProvider
    extends ComponentSupport
    implements Provider<NexusScheduler>
{
  private final IndirectNexusScheduler indirectNexusScheduler;

  private final List<LegacyNexusScheduler> nexusSchedulers;

  @Inject
  public NexusSchedulerProvider(final EventBus eventBus, final List<LegacyNexusScheduler> nexusSchedulers) {
    this.indirectNexusScheduler = new IndirectNexusScheduler();
    this.nexusSchedulers = checkNotNull(nexusSchedulers);
    eventBus.register(this);
  }

  @Override
  public NexusScheduler get() {
    return indirectNexusScheduler;
  }

  @Subscribe
  public void on(final NexusStartedEvent e) {
    if (!nexusSchedulers.isEmpty()) {
      log.debug("{}", nexusSchedulers);
      final LegacyNexusScheduler real = nexusSchedulers.get(0);
      log.info("Scheduling support present and installed: {}", real.getClass().getSimpleName());
      indirectNexusScheduler.setNexusScheduler(real);
      indirectNexusScheduler.initializeTasks();
    }
    else {
      log.info("Scheduling support not present, scheduling functionality disabled.");
    }
  }

  // ==

  public static final class IndirectNexusScheduler
      implements NexusScheduler
  {
    private volatile LegacyNexusScheduler nexusScheduler;

    public IndirectNexusScheduler() {
      this.nexusScheduler = new NoopNexusScheduler();
    }

    public void setNexusScheduler(final LegacyNexusScheduler nexusScheduler) {
      this.nexusScheduler = checkNotNull(nexusScheduler);
    }

    @Override
    public void initializeTasks() {
      nexusScheduler.initializeTasks();
    }

    @Override
    public void shutdown() {
      nexusScheduler.shutdown();
    }

    @Override
    public <T> ScheduledTask<T> submit(final String name,
                                       final NexusTask<T> nexusTask)
        throws RejectedExecutionException, NullPointerException
    {
      return nexusScheduler.submit(name, nexusTask);
    }

    @Override
    public <T> ScheduledTask<T> schedule(final String name,
                                         final NexusTask<T> nexusTask,
                                         final Schedule schedule)
        throws RejectedExecutionException, NullPointerException
    {
      return nexusScheduler.schedule(name, nexusTask, schedule);
    }

    @Override
    public <T> ScheduledTask<T> updateSchedule(final ScheduledTask<T> task)
        throws RejectedExecutionException, NullPointerException
    {
      return nexusScheduler.updateSchedule(task);
    }

    @Override
    public Map<String, List<ScheduledTask<?>>> getActiveTasks() {
      return nexusScheduler.getActiveTasks();
    }

    @Override
    public Map<String, List<ScheduledTask<?>>> getAllTasks() {
      return nexusScheduler.getAllTasks();
    }

    @Override
    public ScheduledTask<?> getTaskById(final String id) throws NoSuchTaskException {
      return nexusScheduler.getTaskById(id);
    }

    @Override
    @Deprecated
    public NexusTask<?> createTaskInstance(final String taskType) throws IllegalArgumentException {
      return nexusScheduler.createTaskInstance(taskType);
    }

    @Override
    public <T> T createTaskInstance(final Class<T> taskType) throws IllegalArgumentException {
      return nexusScheduler.createTaskInstance(taskType);
    }
  }

  // ==

  public static final class NoopNexusScheduler
      implements LegacyNexusScheduler
  {

    @Override
    public void initializeTasks() {
      // nop
    }

    @Override
    public void shutdown() {
      // nop
    }

    @Override
    public <T> ScheduledTask<T> submit(final String name, final NexusTask<T> nexusTask)
        throws RejectedExecutionException, NullPointerException
    {
      throw new RejectedExecutionException("No scheduling support present");
    }

    @Override
    public <T> ScheduledTask<T> schedule(final String name, final NexusTask<T> nexusTask, final Schedule schedule)
        throws RejectedExecutionException, NullPointerException
    {
      throw new RejectedExecutionException("No scheduling support present");
    }

    @Override
    public <T> ScheduledTask<T> updateSchedule(final ScheduledTask<T> task)
        throws RejectedExecutionException, NullPointerException
    {
      throw new RejectedExecutionException("No scheduling support present");
    }

    @Override
    public Map<String, List<ScheduledTask<?>>> getActiveTasks() {
      return Collections.emptyMap();
    }

    @Override
    public Map<String, List<ScheduledTask<?>>> getAllTasks() {
      return Collections.emptyMap();
    }

    @Override
    public ScheduledTask<?> getTaskById(final String id) throws NoSuchTaskException {
      return null;
    }

    @Override
    public NexusTask<?> createTaskInstance(final String taskType) throws IllegalArgumentException {
      throw new IllegalArgumentException("No scheduling support present");
    }

    @Override
    public <T> T createTaskInstance(final Class<T> taskType) throws IllegalArgumentException {
      throw new IllegalArgumentException("No scheduling support present");
    }
  }
}
