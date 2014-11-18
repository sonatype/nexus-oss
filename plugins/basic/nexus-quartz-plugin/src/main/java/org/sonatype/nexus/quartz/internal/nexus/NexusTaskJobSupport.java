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
package org.sonatype.nexus.quartz.internal.nexus;

import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.quartz.JobSupport;
import org.sonatype.nexus.scheduling.Cancelable;
import org.sonatype.nexus.scheduling.NexusTaskFactory;
import org.sonatype.nexus.scheduling.Task;
import org.sonatype.nexus.scheduling.TaskConfiguration;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.UnableToInterruptJobException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {#link JobSupport} wrapping NX Task that is also {@link InterruptableJob} (but actual interruption ability depends
 * on underlying NX Task).
 *
 * @since 3.0
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Named
public class NexusTaskJobSupport<T>
    extends JobSupport
    implements InterruptableJob
{
  private final NexusTaskFactory nexusTaskFactory;

  private Task<T> nexusTask;

  @Inject
  public NexusTaskJobSupport(final NexusTaskFactory nexusTaskFactory)
  {
    this.nexusTaskFactory = checkNotNull(nexusTaskFactory);
  }

  @Override
  public void execute() throws Exception {
    final TaskConfiguration taskConfiguration = toTaskConfiguration(context.getJobDetail().getJobDataMap());
    nexusTask = nexusTaskFactory.createTaskInstance(taskConfiguration);
    final T result = nexusTask.call();
    context.setResult(result);
    // put back any state task modified to have it persisted
    context.getJobDetail().getJobDataMap().putAll(nexusTask.getConfiguration().getMap());
  }

  @Override
  public void interrupt() throws UnableToInterruptJobException {
    if (nexusTask == null) {
      return;
    }
    if (nexusTask instanceof Cancelable) {
      ((Cancelable) nexusTask).cancel();
      // TODO: flag cancellation in context
    }
    else {
      throw new UnableToInterruptJobException("Task " + nexusTask + " not Cancellable");
    }
  }

  /**
   * Creates {@link TaskConfiguration} out of provided {@link JobDataMap}, by copying only those values that
   * are Strings (as task configuration is {@code Map<String, String>} while job data map values are Objects.
   */
  public static TaskConfiguration toTaskConfiguration(final JobDataMap jobDataMap) {
    final TaskConfiguration taskConfiguration = new TaskConfiguration();
    for (Entry<String, Object> entry : jobDataMap.entrySet()) {
      if (entry.getValue() instanceof String) {
        taskConfiguration.getMap().put(entry.getKey(), String.valueOf(entry.getValue()));
      }
    }
    return taskConfiguration;
  }
}