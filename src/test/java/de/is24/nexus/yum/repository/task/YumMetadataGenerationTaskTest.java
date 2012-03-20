package de.is24.nexus.yum.repository.task;

import static de.is24.nexus.yum.repository.task.YumMetadataGenerationTask.ID;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.sonatype.scheduling.TaskState.RUNNING;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.sonatype.scheduling.DefaultScheduledTask;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskState;
import org.sonatype.scheduling.schedules.OnceSchedule;
import org.sonatype.scheduling.schedules.RunNowSchedule;

import de.is24.nexus.yum.repository.YumRepository;

@SuppressWarnings("unchecked")
public class YumMetadataGenerationTaskTest {
	private static final String ANOTHER_REPO = "repo2";
	private static final String ANOTHER_VERSION = "version2";
	private static final String VERSION = "version";
	private static final String NO_VERSION = null;
	private static final String REPO = "REPO1";
	private static final String RPM_URL = "http://url";
	private static final File RPM_DIR = new File(".");

	@Test
	public void shouldNotExecuteIfOperateOnSameRepository() throws Exception {
		YumMetadataGenerationTask task = task(REPO, NO_VERSION);
		assertFalse(task.allowConcurrentExecution(createMap(scheduledTask(task), scheduledTask(REPO, NO_VERSION, RUNNING))));
	}

	@Test
	public void shouldNotExecuteIfOperateOnSameRepositoryAndSameVersion() throws Exception {
		YumMetadataGenerationTask task = task(REPO, VERSION);
		assertFalse(task.allowConcurrentExecution(createMap(scheduledTask(task), scheduledTask(REPO, VERSION, RUNNING))));
	}

	@Test
	public void shouldExecuteIfOperateOnSameRepositoryAndAnotherVersion() throws Exception {
		YumMetadataGenerationTask task = task(REPO, VERSION);
		assertTrue(task.allowConcurrentExecution(createMap(scheduledTask(task), scheduledTask(REPO, ANOTHER_VERSION, RUNNING))));
	}

	@Test
	public void shouldExecuteIfOperateOnAnotherRepository() throws Exception {
		YumMetadataGenerationTask task = task(REPO, NO_VERSION);
		assertTrue(task.allowConcurrentExecution(createMap(scheduledTask(task), scheduledTask(ANOTHER_REPO, NO_VERSION, RUNNING))));
	}

	private ScheduledTask<YumRepository> scheduledTask(String repo, String version, TaskState state, Date scheduledAt) {
		MockScheduledTask<YumRepository> scheduledTask = scheduledTask(task(repo, version));
		scheduledTask.setTaskState(state);
		scheduledTask.setSchedule(new OnceSchedule(new Date(scheduledAt.getTime() + 400)));
		return scheduledTask;
	}

	private ScheduledTask<YumRepository> scheduledTask(String repo, String version, TaskState state) {
		return scheduledTask(repo, version, state, new Date());
	}

	private MockScheduledTask<YumRepository> scheduledTask(YumMetadataGenerationTask task) {
		return new MockScheduledTask<YumRepository>(task);
	}

	private YumMetadataGenerationTask task(String repo, String version) {
		YumMetadataGenerationTask task = new YumMetadataGenerationTask() {

			@Override
			protected YumRepository doRun() throws Exception {
				return null;
			}

		};
    task.setBaseRpmDir(RPM_DIR.getAbsolutePath());
    task.setBaseRpmUrl(RPM_URL);
    task.setBaseRepoDir(RPM_DIR);
    task.setBaseRepoUrl(RPM_URL);
    task.setRepositoryId(repo);
    task.setVersion(version);
    task.setBaseCacheDir(RPM_DIR.getAbsolutePath());
    task.setAddedFiles(null);
    task.setSingleRpmPerDirectory(true);
		return task;
	}


	private Map<String, List<ScheduledTask<?>>> createMap(ScheduledTask<YumRepository>... scheduledTasks) {
		List<ScheduledTask<?>> list = new ArrayList<ScheduledTask<?>>();
		for (ScheduledTask<YumRepository> task : scheduledTasks) {
			list.add(task);
		}
		return createMap(list);
	}

	private Map<String, List<ScheduledTask<?>>> createMap(List<ScheduledTask<?>> yumTaskList) {
		Map<String, List<ScheduledTask<?>>> activeTasks = new HashMap<String, List<ScheduledTask<?>>>();
		activeTasks.put(ID, yumTaskList);
		return activeTasks;
	}

	private static class MockScheduledTask<T> extends DefaultScheduledTask<T> {

		public MockScheduledTask(Callable<T> callable) {
			super(ID, "", "", null, callable, new RunNowSchedule());
		}

		@Override
		public void setTaskState(TaskState state) {
			super.setTaskState(state);
		}

	}
}
