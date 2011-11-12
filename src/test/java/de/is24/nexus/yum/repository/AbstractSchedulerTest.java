package de.is24.nexus.yum.repository;

import java.io.IOException;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.source.ApplicationConfigurationSource;
import org.sonatype.nexus.configuration.source.FileConfigurationSource;
import org.sonatype.nexus.proxy.AbstractNexusTestCase;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;


public abstract class AbstractSchedulerTest extends AbstractNexusTestCase {
  protected NexusScheduler scheduler;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    loadDefaultConfig();
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

  private void loadDefaultConfig() throws Exception, ConfigurationException, IOException {
    FileConfigurationSource source = (FileConfigurationSource) lookup(ApplicationConfigurationSource.class, "file");
    source.loadConfiguration();
  }
}
