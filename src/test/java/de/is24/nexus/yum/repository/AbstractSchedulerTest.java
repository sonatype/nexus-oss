package de.is24.nexus.yum.repository;

import javax.inject.Inject;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;
import de.is24.nexus.yum.AbstractYumNexusTestCase;


public abstract class AbstractSchedulerTest extends AbstractYumNexusTestCase {
  @Inject
  protected NexusScheduler nexusScheduler;

  protected YumRepository executeJob(YumDefaultGeneratorConfiguration config) throws Exception {
    ScheduledTask<YumRepository> scheduledTask = nexusScheduler.submit(YumMetadataGenerationTask.ID,
      createTask(config));
    return scheduledTask.get();
  }

  private YumMetadataGenerationTask createTask(YumDefaultGeneratorConfiguration config) throws Exception {
    YumMetadataGenerationTask yumTask = (YumMetadataGenerationTask) lookup(SchedulerTask.class,
      YumMetadataGenerationTask.ID);
    yumTask.setConfiguration(config);
    return yumTask;
  }

}
