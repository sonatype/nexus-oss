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
