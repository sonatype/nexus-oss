package de.is24.nexus.yum.repository;

import static org.sonatype.scheduling.TaskState.RUNNING;

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;


@Component(role = SchedulerTask.class, hint = YumMetadataGenerationTask.ID, instantiationStrategy = "per-lookup")
public class YumMetadataGenerationTask extends AbstractNexusTask<YumRepository> {
  public static final String ID = "YumMetadataGenerationTask";

  private static final int MAXIMAL_PARALLEL_RUNS = 10;

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

  public void setConfiguration(YumGeneratorConfiguration config) {
    job = new YumRepositoryGeneratorJob(config);
  }

  public YumRepositoryGeneratorJob getJob() {
    return job;
  }

  public void setJob(YumRepositoryGeneratorJob job) {
    this.job = job;
  }

  @Override
  public boolean allowConcurrentExecution(Map<String, List<ScheduledTask<?>>> activeTasks) {

		if (activeTasks.containsKey(ID)) {
      int activeRunningTasks = 0;
      for (ScheduledTask<?> scheduledTask : activeTasks.get(ID)) {
				if (RUNNING.equals(scheduledTask.getTaskState())) {
					if (conflictsWith((YumMetadataGenerationTask) scheduledTask.getTask())) {
						return false;
          }
          activeRunningTasks++;
        }
      }
      return activeRunningTasks < MAXIMAL_PARALLEL_RUNS;
    } else {
      return true;
    }
  }

	private boolean conflictsWith(YumMetadataGenerationTask task) {
		return getConfig().conflictsWith(task.getConfig());
  }

  private YumGeneratorConfiguration getConfig() {
    return job.getConfig();
  }

}
