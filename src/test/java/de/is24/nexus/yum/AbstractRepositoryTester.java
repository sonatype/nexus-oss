package de.is24.nexus.yum;

import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.BASE_CACHE_DIR;
import static de.is24.nexus.yum.repository.utils.RepositoryTestUtils.BASE_TMP_FILE;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.sonatype.scheduling.TaskState.RUNNING;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;

import com.google.code.tempusfugit.temporal.Condition;


public abstract class AbstractRepositoryTester extends AbstractYumNexusTestCase {
  private static final String SNAPSHOTS = "snapshots";

  @Inject
	private NexusScheduler nexusScheduler;

  @After
  public void waitForThreadPool() throws Exception {
    waitFor(new Condition() {
        @Override
        public boolean isSatisfied() {
				int running = 0;
				for (Entry<String, List<ScheduledTask<?>>> entry : nexusScheduler.getActiveTasks().entrySet()) {
					for (ScheduledTask<?> task : entry.getValue()) {
						if (RUNNING.equals(task.getTaskState())) {
							running++;
						}
					}
				}
				return running == 0;
        }
      });
  }


  @Before
  public void cleanUpCacheDirectory() throws Exception {
    deleteDirectory(BASE_TMP_FILE);
    BASE_CACHE_DIR.mkdirs();
  }

  protected MavenRepository createRepository(final boolean isMavenHostedRepository) {
    return createRepository(isMavenHostedRepository, SNAPSHOTS);
  }

  protected MavenRepository createRepository(final boolean isMavenHostedRepository, final String repoId) {
    RepositoryKind kind = createMock(RepositoryKind.class);
    expect(kind.isFacetAvailable(MavenHostedRepository.class)).andReturn(isMavenHostedRepository);

    MavenRepository repository = createMock(MavenRepository.class);
    expect(repository.getRepositoryKind()).andReturn(kind).anyTimes();
    expect(repository.getId()).andReturn(repoId).anyTimes();

    File repoDir = new File(BASE_TMP_FILE, "tmp-repos/" + repoId);
    repoDir.mkdirs();
    expect(repository.getLocalUrl()).andReturn(repoDir.toURI().toString()).anyTimes();

    replay(kind, repository);

    return repository;
  }

  protected StorageItem createItem(String version, String filename) {
    StorageItem item = createMock(StorageItem.class);

    expect(item.getPath()).andReturn("blalu/" + version + "/" + filename).anyTimes();
    expect(item.getParentPath()).andReturn("blalu/" + version).anyTimes();
    expect(item.getItemContext()).andReturn(new RequestContext()).anyTimes();

    replay(item);
    return item;
  }
}
