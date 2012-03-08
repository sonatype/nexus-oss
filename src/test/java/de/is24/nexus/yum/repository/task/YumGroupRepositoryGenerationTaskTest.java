package de.is24.nexus.yum.repository.task;

import static de.is24.nexus.yum.repository.task.YumGroupRepositoryGenerationTask.ID;
import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.assertRepository;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.scheduling.TaskState.RUNNING;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.mockito.Matchers;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.scheduling.ScheduledTask;

import de.is24.nexus.yum.repository.task.YumGroupRepositoryGenerationTask;

public class YumGroupRepositoryGenerationTaskTest {

  private static final String BASE_REPO_DIR = "src/test/yum-repo";
  private static final File REPO_DIR1 = new File(BASE_REPO_DIR + "/repo1");
  private static final File REPO_DIR2 = new File(BASE_REPO_DIR + "/repo2");
  private static final File GROUP_REPO_DIR = new File("target/tmp/group-repo");
  private static final String REPO_ID = "group-repo-id";
  private static final String REPO_ID2 = "group-repo-id2";
  private GroupRepository groupRepo;

  @Test
  public void shouldGenerateGroupRepo() throws Exception {
    givenGroupRepoWith2YumRepos();
    thenGenerateYumRepo();
    assertRepository(new File(GROUP_REPO_DIR, "repodata"), "group-repo");
  }

  @Test
  public void shouldNotAllowConcurrentExecutionForSameRepo() throws Exception {
    final YumGroupRepositoryGenerationTask task = new YumGroupRepositoryGenerationTask();
    final GroupRepository groupRepo = mock(GroupRepository.class);
    when(groupRepo.getId()).thenReturn(REPO_ID);
    task.setGroupRepository(groupRepo);
    assertThat(task.allowConcurrentExecution(createRunningTaskForRepos(groupRepo)), is(false));
  }

  @Test
  public void shouldNotAllowConcurrentExecutionIfAnotherTaskIsRunning() throws Exception {
    final YumGroupRepositoryGenerationTask task = new YumGroupRepositoryGenerationTask();
    final GroupRepository groupRepo = mock(GroupRepository.class);
    when(groupRepo.getId()).thenReturn(REPO_ID);
    final GroupRepository groupRepo2 = mock(GroupRepository.class);
    when(groupRepo2.getId()).thenReturn(REPO_ID2);
    task.setGroupRepository(groupRepo);
    assertThat(task.allowConcurrentExecution(createRunningTaskForRepos(groupRepo2)), is(false));
  }

  private Map<String, List<ScheduledTask<?>>> createRunningTaskForRepos(GroupRepository... groupRepos) {
    final Map<String, List<ScheduledTask<?>>> map = new HashMap<String, List<ScheduledTask<?>>>();
    final List<ScheduledTask<?>> taskList = new ArrayList<ScheduledTask<?>>();
    for (GroupRepository groupRepo : groupRepos) {
      taskList.add(runningTask(groupRepo));
    }
    map.put(ID, taskList);
    return map;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private ScheduledTask<?> runningTask(GroupRepository groupRepo) {
    final ScheduledTask<?> task = mock(ScheduledTask.class);
    final YumGroupRepositoryGenerationTask otherGenerationTask = mock(YumGroupRepositoryGenerationTask.class);
    when(otherGenerationTask.getGroupRepository()).thenReturn(groupRepo);
    when(task.getTaskState()).thenReturn(RUNNING);
    when(task.getTask()).thenReturn((Callable) otherGenerationTask);
    return task;
  }

  private void thenGenerateYumRepo() throws Exception {
    YumGroupRepositoryGenerationTask task = new YumGroupRepositoryGenerationTask();
    task.setGroupRepository(groupRepo);
    task.doRun();
  }

  private void givenGroupRepoWith2YumRepos() throws IOException {
    groupRepo = mock(GroupRepository.class);
    when(groupRepo.getLocalUrl()).thenReturn(GROUP_REPO_DIR.getAbsolutePath());
    List<Repository> repositories = asList(createRepo(REPO_DIR1), createRepo(REPO_DIR2));
    when(groupRepo.getMemberRepositories()).thenReturn(repositories);
    if (GROUP_REPO_DIR.exists()) {
      FileUtils.deleteDirectory(GROUP_REPO_DIR);
    }
    GROUP_REPO_DIR.mkdirs();
  }

  private Repository createRepo(File repoDir) {
    final Repository repo = mock(Repository.class);
    final RepositoryKind kind = mock(RepositoryKind.class);
    when(kind.isFacetAvailable(Matchers.eq(MavenHostedRepository.class))).thenReturn(true);
    when(repo.getLocalUrl()).thenReturn(repoDir.getAbsolutePath());
    when(repo.getRepositoryKind()).thenReturn(kind);
    return repo;
  }
}
