package de.is24.nexus.yum.repository;

import static de.is24.nexus.yum.execution.ExecutionUtil.execCommand;
import static de.is24.nexus.yum.repository.RepositoryUtils.getBaseDir;
import static de.is24.nexus.yum.repository.YumMetadataGenerationTask.isActive;
import static java.lang.String.format;
import static org.sonatype.scheduling.TaskState.RUNNING;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;

@Component(role = SchedulerTask.class, hint = YumGroupRepositoryGenerationTask.ID, instantiationStrategy = "per-lookup")
public class YumGroupRepositoryGenerationTask extends AbstractNexusTask<YumRepository> {

  private static final Logger LOG = LoggerFactory.getLogger(YumGroupRepositoryGenerationTask.class);
  public static final String ID = "YumGroupRepositoryGenerationTask";
  private static final int MAXIMAL_PARALLEL_RUNS = 2;
  private GroupRepository groupRepository;

  public void setGroupRepository(GroupRepository groupRepository) {
    this.groupRepository = groupRepository;
  }

  @Override
  protected YumRepository doRun() throws Exception {
    if (isActive() && isValidRepository(groupRepository)) {
      LOG.info("Merging repository group {}='{}' ...", groupRepository.getId(), groupRepository.getName());
      final File repoBaseDir = getBaseDir(groupRepository);
      execCommand(buildCommand(repoBaseDir));
      LOG.info("Group repository {}='{}' merged.", groupRepository.getId(), groupRepository.getName());
      return new YumRepository(repoBaseDir, groupRepository.getId(), null);
    }
    return null;
  }

  @Override
  public boolean allowConcurrentExecution(Map<String, List<ScheduledTask<?>>> activeTasks) {

    if (activeTasks.containsKey(ID)) {
      int activeRunningTasks = 0;
      for (ScheduledTask<?> scheduledTask : activeTasks.get(ID)) {
        if (RUNNING.equals(scheduledTask.getTaskState())) {
          if (conflictsWith((YumGroupRepositoryGenerationTask) scheduledTask.getTask())) {
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

  private boolean conflictsWith(YumGroupRepositoryGenerationTask task) {
    return task.getGroupRepository() != null && this.getGroupRepository() != null
        && task.getGroupRepository().getId().equals(getGroupRepository().getId());
  }

  @Override
  protected String getAction() {
    return "GENERATE_YUM_GROUP_REPOSITORY";
  }

  @Override
  protected String getMessage() {
    return format("Generate yum metadata for group repository %s='%s'", groupRepository.getId(), groupRepository.getName());
  }

  public GroupRepository getGroupRepository() {
    return groupRepository;
  }

  private boolean isValidRepository(GroupRepository groupRepository2) {
    return groupRepository != null && !groupRepository.getMemberRepositories().isEmpty();
  }

  private String buildCommand(File repoBaseDir) throws MalformedURLException, URISyntaxException {
    final StringBuilder repos = new StringBuilder();
    for (Repository memberRepository : groupRepository.getMemberRepositories()) {
      repos.append(" --repo=");
      repos.append(getFileUrl(memberRepository));
    }
    return format("mergerepo --nogroups -d %s -o %s", repos.toString(), repoBaseDir.getAbsolutePath());
  }

  private String getFileUrl(Repository repository) throws URISyntaxException, MalformedURLException {
    return getBaseDir(repository).toURI().toString();
  }

}
