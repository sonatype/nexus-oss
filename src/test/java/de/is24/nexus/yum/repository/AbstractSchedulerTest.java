package de.is24.nexus.yum.repository;

import java.io.File;

import javax.inject.Inject;

import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;

import de.is24.nexus.yum.AbstractYumNexusTestCase;
import de.is24.nexus.yum.repository.task.YumMetadataGenerationTask;


public abstract class AbstractSchedulerTest extends AbstractYumNexusTestCase {
  @Inject
  protected NexusScheduler nexusScheduler;

  protected YumRepository executeJob(YumMetadataGenerationTask task) throws Exception {
    final ScheduledTask<YumRepository> scheduledTask = nexusScheduler.submit(YumMetadataGenerationTask.ID, task);
    return scheduledTask.get();
  }

  protected YumMetadataGenerationTask createTask(File rpmDir, String rpmUrl, File repoDir, String repoUrl, String id, String version,
      File cacheDir, String addedFile, boolean singleRpmPerDirectory) throws Exception {
    YumMetadataGenerationTask yumTask = (YumMetadataGenerationTask) lookup(SchedulerTask.class, YumMetadataGenerationTask.ID);
    yumTask.setRepositoryId(id);
    yumTask.setRepoDir(repoDir);
    yumTask.setRepoUrl(repoUrl);
    yumTask.setRpmDir(rpmDir.getAbsolutePath());
    yumTask.setRpmUrl(rpmUrl);
    yumTask.setVersion(version);
    yumTask.setAddedFiles(addedFile);
    yumTask.setSingleRpmPerDirectory(singleRpmPerDirectory);
    yumTask.setCacheDir(cacheDir.getAbsolutePath());
    return yumTask;
  }

  protected YumMetadataGenerationTask createTask(File rpmDir, String rpmUrl, File repoDir, String id, File cacheDir) throws Exception {
    YumMetadataGenerationTask yumTask = (YumMetadataGenerationTask) lookup(SchedulerTask.class, YumMetadataGenerationTask.ID);
    yumTask.setRepositoryId(id);
    yumTask.setRepoDir(repoDir);
    yumTask.setRpmDir(rpmDir.getAbsolutePath());
    yumTask.setRpmUrl(rpmUrl);
    yumTask.setCacheDir(cacheDir.getAbsolutePath());
    return yumTask;
  }
}
