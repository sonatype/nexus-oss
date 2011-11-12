package de.is24.nexus.yum.repository;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.source.ApplicationConfigurationSource;
import org.sonatype.nexus.configuration.source.FileConfigurationSource;
import org.sonatype.nexus.proxy.AbstractNexusTestCase;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.DefaultTaskConfigManager;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;
import org.sonatype.scheduling.TaskConfigManager;


public abstract class AbstractSchedulerTest extends AbstractNexusTestCase {
  protected DefaultTaskConfigManager taskConfigManager;
  protected NexusScheduler scheduler;
  protected ApplicationConfiguration applicationConfiguration;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    FileConfigurationSource source = (FileConfigurationSource) lookup(ApplicationConfigurationSource.class, "file");
    source.loadConfiguration();

    applicationConfiguration = lookup(ApplicationConfiguration.class);
    taskConfigManager = (DefaultTaskConfigManager) lookup(TaskConfigManager.class);
    taskConfigManager.configure(applicationConfiguration);
    scheduler = lookup(NexusScheduler.class);
  }

  protected YumRepository executeJob(YumDefaultGeneratorConfiguration config) throws Exception {
    ScheduledTask<YumRepository> scheduledTask = scheduler.submit(YumMetadataGenerationTask.ID, createTask(config));
    return scheduledTask.get();
  }

  private YumMetadataGenerationTask createTask(YumDefaultGeneratorConfiguration config) throws Exception {
    YumMetadataGenerationTask yumTask = (YumMetadataGenerationTask) lookup(SchedulerTask.class,
      YumMetadataGenerationTask.ID);
    yumTask.setConfiguration(config);
    return yumTask;
  }

}
