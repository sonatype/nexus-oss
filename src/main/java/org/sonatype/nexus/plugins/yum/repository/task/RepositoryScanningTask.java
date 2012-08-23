package org.sonatype.nexus.plugins.yum.repository.task;

import static org.apache.commons.io.FileUtils.listFiles;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.plugins.yum.plugin.impl.MavenRepositoryInfo;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;
import org.sonatype.scheduling.TaskState;


/**
 * This job scans a {@link MavenHostedRepository} for RPMs and adds each version
 * to the {@link MavenRepositoryInfo#addVersion(String) MavenRepositoryInfo}.
 *
 * @author sherold
 */
@Component(role = SchedulerTask.class, hint = RepositoryScanningTask.ID, instantiationStrategy = "per-lookup")
public class RepositoryScanningTask extends AbstractNexusTask<Object> {
  private static final int MAXIMAL_PARALLEL_RUNS = 3;
  public static final String ID = "RepositoryScanningTask";
  private static final String[] RPM_EXTENSIONS = new String[] { "rpm" };

  private MavenRepositoryInfo mavenRepositoryInfo;

  @Override
  protected Object doRun() throws Exception {
    if (mavenRepositoryInfo == null) {
      throw new IllegalArgumentException("Please provide a mavenRepositoryInfo");
    }

    getLogger().info("Start new RepositoryScanningJob for repository : {}",
      mavenRepositoryInfo.getRepository().getId());
    scanRepository();
    getLogger().info("Scanning for repository {} done.", mavenRepositoryInfo.getRepository().getId());
    return null;
  }

  @SuppressWarnings("unchecked")
  private void scanRepository() {
    try {
      getLogger().info("Start scanning of repository base url : {}", mavenRepositoryInfo.getRepository().getLocalUrl());

      File repositoryBaseDir = mavenRepositoryInfo.getBaseDir();
      for (File file : (Collection<File>) listFiles(repositoryBaseDir, RPM_EXTENSIONS, true)) {
        mavenRepositoryInfo.addVersion(file.getParentFile().getName());
      }

      getLogger().info("Found following versions in repository '{}' : {}", mavenRepositoryInfo.getId(), mavenRepositoryInfo.getVersions());

    } catch (Exception e) {
      getLogger().error("Could not scan repository " + mavenRepositoryInfo.getId(), e);
    }
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
    } else {
      return true;
    }
  }

  @Override
  protected String getAction() {
    return "scanning";
  }

  @Override
  protected String getMessage() {
    return "Scanning repository" + mavenRepositoryInfo.getRepository();
  }

  public MavenRepositoryInfo getMavenRepositoryInfo() {
    return mavenRepositoryInfo;
  }

  public void setMavenRepositoryInfo(MavenRepositoryInfo mavenRepositoryInfo) {
    this.mavenRepositoryInfo = mavenRepositoryInfo;
  }

}
