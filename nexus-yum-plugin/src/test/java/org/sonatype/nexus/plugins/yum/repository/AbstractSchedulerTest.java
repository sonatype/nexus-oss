/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
 package org.sonatype.nexus.plugins.yum.repository;

import java.io.File;

import javax.inject.Inject;

import org.sonatype.nexus.plugins.yum.AbstractYumNexusTestCase;
import org.sonatype.nexus.plugins.yum.repository.task.YumMetadataGenerationTask;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;


public abstract class AbstractSchedulerTest extends AbstractYumNexusTestCase {
  @Inject
  protected NexusScheduler nexusScheduler;

  protected YumRepository executeJob(YumMetadataGenerationTask task) throws Exception {
    final ScheduledTask<YumRepository> scheduledTask = nexusScheduler.submit(YumMetadataGenerationTask.ID, task);
    return scheduledTask.get();
  }

  protected YumMetadataGenerationTask createTask(File rpmDir, String rpmUrl, File repoDir, String repoUrl, String id, String version,
      String addedFile, boolean singleRpmPerDirectory) throws Exception {
    YumMetadataGenerationTask yumTask = (YumMetadataGenerationTask) lookup(SchedulerTask.class, YumMetadataGenerationTask.ID);
    yumTask.setRepositoryId(id);
    yumTask.setRepoDir(repoDir);
    yumTask.setRepoUrl(repoUrl);
    yumTask.setRpmDir(rpmDir.getAbsolutePath());
    yumTask.setRpmUrl(rpmUrl);
    yumTask.setVersion(version);
    yumTask.setAddedFiles(addedFile);
    yumTask.setSingleRpmPerDirectory(singleRpmPerDirectory);
    return yumTask;
  }

  protected YumMetadataGenerationTask createTask(File rpmDir, String rpmUrl, File repoDir, String id) throws Exception {
    return createTask(rpmDir, rpmUrl, repoDir, null, id, null, null, true);
  }
}
