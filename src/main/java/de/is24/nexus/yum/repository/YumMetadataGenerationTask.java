package de.is24.nexus.yum.repository;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.scheduling.SchedulerTask;


@Component(role = SchedulerTask.class, hint = YumMetadataGenerationTask.ID, instantiationStrategy = "per-lookup")
public class YumMetadataGenerationTask extends AbstractNexusTask<YumRepository> {
  public static final String ID = "YumMetadataGenerationTask";

  private YumRepositoryGeneratorJob job;

  @Override
  protected YumRepository doRun() throws Exception {
    if (job == null) {
      throw new IllegalStateException("YumMetadataGenerationTask not set.");
    }

    return job.call();
  }

  @Override
  protected String getAction() {
    return "Generation YUM repository metadata";
  }

  @Override
  protected String getMessage() {
    return "Generation YUM repository metadata";
  }

  public void setConfiguration(YumDefaultGeneratorConfiguration config) {
    job = new YumRepositoryGeneratorJob(config);
  }
}
