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

package org.sonatype.nexus.yum.internal.task;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.yum.YumHosted;
import org.sonatype.nexus.yum.internal.RpmScanner;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskState;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This job scans a {@link Repository} for RPMs and adds each version to Yam.
 *
 * @since yum 3.0
 */
@Named(RepositoryScanningTask.ID)
public class RepositoryScanningTask
    extends AbstractNexusTask<Object>
{

  private static final int MAXIMAL_PARALLEL_RUNS = 3;

  public static final String ID = "RepositoryScanningTask";

  private YumHosted yum;

  private final RpmScanner scanner;

  @Inject
  public RepositoryScanningTask(final RpmScanner scanner, final EventBus eventBus) {
    super(eventBus, null);
    this.scanner = checkNotNull(scanner);
  }

  @Override
  protected Object doRun()
      throws Exception
  {
    checkNotNull(yum, "Yum must be set");

    getLogger().debug(
        "Scanning repository '{}' base dir '{}' for RPMs", yum.getNexusRepository().getId(), yum.getBaseDir()
    );

    for (final File rpm : scanner.scan(yum.getBaseDir())) {
      yum.addVersion(rpm.getParentFile().getName());
    }

    return null;
  }

  @Override
  public boolean allowConcurrentExecution(Map<String, List<ScheduledTask<?>>> activeTasks) {
    if (activeTasks.containsKey(ID)) {
      int activeRunningTasks = 0;
      for (ScheduledTask<?> task : activeTasks.get(ID)) {
        if (TaskState.RUNNING.equals(task.getTaskState())) {
          activeRunningTasks++;
        }
      }
      return activeRunningTasks < MAXIMAL_PARALLEL_RUNS;
    }
    else {
      return true;
    }
  }

  @Override
  protected String getAction() {
    return "scanning";
  }

  @Override
  protected String getMessage() {
    return "Scanning repository '" + yum.getNexusRepository().getId() + "'";
  }

  public void setYum(final YumHosted yum) {
    this.yum = yum;
  }

}
