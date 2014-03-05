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

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Named;
import javax.inject.Singleton;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Job marked with {@link PersistJobDataAfterExecution} annotation, which job data map should be persisted (again)
 * after each execution.
 */
@Singleton
@Named
@PersistJobDataAfterExecution
public class PersistingJob
    implements Job
{
  protected Logger log = LoggerFactory.getLogger(getClass());

  private static final AtomicInteger counter = new AtomicInteger(1);

  @Override
  public void execute(final JobExecutionContext context) throws JobExecutionException {
    log.info("START {}", getClass().getSimpleName());
    final JobDataMap data = context.getJobDetail().getJobDataMap();
    final Integer count = counter.getAndIncrement();
    if (count == 1) {
      data.put("RUN", count);
    }
    else {
      final Integer existingCount = (Integer) data.get("RUN");
      if ((existingCount + 1) != count) {
        log.error("RUN {} {}", count, existingCount);
      }
      data.put("RUN", count);
    }
    log.info("DONE {}", getClass().getSimpleName());
  }
}
