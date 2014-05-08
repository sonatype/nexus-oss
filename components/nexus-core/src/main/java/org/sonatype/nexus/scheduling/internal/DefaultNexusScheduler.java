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

package org.sonatype.nexus.scheduling.internal;

import java.util.List;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.scheduling.NexusTaskFactory;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The legacy Nexus scheduler.
 */
@Singleton
@Named
public class DefaultNexusScheduler
    extends ComponentSupport
    implements NexusScheduler
{
  private final NexusTaskFactory nexusTaskFactory;

  private final List<NexusSchedulerSPI> schedulers;

  @Inject
  public DefaultNexusScheduler(final NexusTaskFactory nexusTaskFactory,
                               final List<NexusSchedulerSPI> schedulers)
  {
    this.nexusTaskFactory = checkNotNull(nexusTaskFactory);
    this.schedulers = checkNotNull(schedulers);
  }

  protected NexusSchedulerSPI getScheduler() {
    if (schedulers.isEmpty()) {
      throw new IllegalStateException("No scheduler present in system!");
    }
    else {
      return schedulers.get(0);
    }
  }

  @Override
  public <T> Future<T> submit(String name, NexusTask<T> nexusTask) {
    final NexusSchedulerSPI scheduler = getScheduler();
    nexusTask.setName(name);
    return scheduler.submit(nexusTask);
  }

  @Override
  public int getRunningTaskCount() {
    final NexusSchedulerSPI scheduler = getScheduler();
    return getScheduler().getRunningTaskCount();
  }

  @Override
  public void killAll() {
    getScheduler().killAll();
  }

  public <T> T createTaskInstance(final Class<T> taskType) throws IllegalArgumentException {
    return nexusTaskFactory.createTaskInstance(taskType);
  }

  @Override
  public NexusTask<?> createTaskInstanceByFQCN(final String taskFQCN) throws IllegalArgumentException {
    return nexusTaskFactory.createTaskInstanceByFQCN(taskFQCN);
  }
}
