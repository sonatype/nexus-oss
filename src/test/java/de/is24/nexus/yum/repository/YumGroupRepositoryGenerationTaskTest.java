package de.is24.nexus.yum.repository;

import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.REPODATA_DIR;
import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.assertRepository;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;

public class YumGroupRepositoryGenerationTaskTest {

  private static final String BASE_REPO_DIR = "src/test/yum-repo";
  private static final File REPO_DIR1 = new File(BASE_REPO_DIR + "/repo1");
  private static final File REPO_DIR2 = new File(BASE_REPO_DIR + "/repo2");
  private static final File GROUP_REPO_DIR = new File("target/tmp/group-repo");
  private GroupRepository groupRepo;

  @Test
  public void shouldGenerateGroupRepo() throws Exception {
    givenGroupRepoWith2YumRepos();
    thenGenerateYumRepo();
    assertRepository(REPODATA_DIR, "group-repo");
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
    Repository repo = mock(Repository.class);
    when(repo.getLocalUrl()).thenReturn(repoDir.getAbsolutePath());
    return repo;
  }
}
