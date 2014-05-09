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

import java.util.Map;

import javax.inject.Named;

import org.sonatype.nexus.quartz.JobSupport;
import org.sonatype.nexus.scheduling.NexusTask;

import com.google.common.collect.Maps;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link NexusTask} support class for Quartz.
 *
 * @since 3.0
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Named
public class NexusTaskJobSupport<T>
    extends JobSupport
{
  public static final String NX_TASK_FQCN = "nexusTaskFQClassName";

  public static final String NX_TASK_RESULT = "nexusTaskResult";

  private NexusTask<T> nexusTask;

  public NexusTask<T> getNexusTask() {
    return nexusTask;
  }

  public void setNexusTask(final NexusTask<T> nexusTask) {
    this.nexusTask = checkNotNull(nexusTask);
  }

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    final Map<String, String> nexusTaskParameters = Maps.newHashMap();
    for (Map.Entry<String, Object> entry : jobDataMap.entrySet()) {
      if (entry.getValue() instanceof String) {
        nexusTaskParameters.put(entry.getKey(), String.valueOf(entry.getValue()));
      }
    }
    nexusTask.getParameters().putAll(nexusTaskParameters);
    try {
      final T result = nexusTask.call();
      context.put(NX_TASK_RESULT, result);
    }
    catch (Exception e) {
      throw new JobExecutionException("NexusTask execution failed", e);
    }
    jobDataMap.putAll(nexusTask.getParameters());
  }
}