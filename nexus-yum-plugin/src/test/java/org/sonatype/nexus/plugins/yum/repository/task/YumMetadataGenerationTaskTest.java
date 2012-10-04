/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
 package org.sonatype.nexus.plugins.yum.repository.task;

import static org.sonatype.nexus.plugins.yum.repository.task.YumMetadataGenerationTask.ID;
import static org.sonatype.nexus.test.reflection.ReflectionTestUtils.setField;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.scheduling.TaskState.RUNNING;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.nexus.plugins.yum.repository.task.YumMetadataGenerationTask;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.scheduling.DefaultScheduledTask;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.TaskState;
import org.sonatype.scheduling.schedules.OnceSchedule;
import org.sonatype.scheduling.schedules.RunNowSchedule;

import org.sonatype.nexus.plugins.yum.config.YumConfiguration;
import org.sonatype.nexus.plugins.yum.repository.YumRepository;

@SuppressWarnings("unchecked")
public class YumMetadataGenerationTaskTest {
	private static final String ANOTHER_REPO = "repo2";
	private static final String ANOTHER_VERSION = "version2";
	private static final String VERSION = "version";
	private static final String NO_VERSION = null;
	private static final String REPO = "REPO1";
  private static final String BASE_URL = "http://foo.bla";
  private static final String RPM_URL = BASE_URL + "/content/repositories/" + REPO;
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

  @Test
  public void shouldSetDefaultsForRepoParams() throws Exception {
    // given
    YumMetadataGenerationTask task = new YumMetadataGenerationTask();
    setField(task, "repositoryRegistry", repoRegistry());
    task.setRpmDir(RPM_DIR.getAbsolutePath());
    task.setRpmUrl(RPM_URL);
    // when
    task.setDefaults();
    // then
    assertThat(task.getRepoDir(), is(RPM_DIR.getAbsoluteFile()));
    assertThat(task.getRepoUrl(), is(RPM_URL));
  }

  @Test
  public void shouldSetDefaultsIfOnlyRepoWasSet() throws Exception {
    // given
    YumMetadataGenerationTask task = new YumMetadataGenerationTask();
    task.setRepositoryId(REPO);
    setField(task, "repositoryRegistry", repoRegistry());
    setField(task, "restApiSettings", restApiSettings());
    // when
    task.setDefaults();
    // then
    assertThat(task.getRpmDir(), is(RPM_DIR.getAbsolutePath()));
    assertThat(task.getRpmUrl(), is(RPM_URL));
    assertThat(task.getRepoDir(), is(RPM_DIR.getAbsoluteFile()));
    assertThat(task.getRepoUrl(), is(RPM_URL));
  }

  private GlobalRestApiSettings restApiSettings() {
    GlobalRestApiSettings settings = mock(GlobalRestApiSettings.class);
    when(settings.getBaseUrl()).thenReturn(BASE_URL);
    return settings;
  }

  private RepositoryRegistry repoRegistry() throws Exception {
    final Repository repo = mock(Repository.class);
    when(repo.getId()).thenReturn(REPO);
    when(repo.getLocalUrl()).thenReturn(RPM_DIR.getAbsolutePath());
    final RepositoryRegistry repoRegistry = mock(RepositoryRegistry.class);
    when(repoRegistry.getRepository(anyString())).thenReturn(repo);
    return repoRegistry;
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
    task.setRpmDir(RPM_DIR.getAbsolutePath());
    task.setRpmUrl(RPM_URL);
    task.setRepoDir(RPM_DIR);
    task.setRepoUrl(RPM_URL);
    task.setRepositoryId(repo);
    task.setVersion(version);
    task.setAddedFiles(null);
    task.setSingleRpmPerDirectory(true);

    final YumConfiguration yumConfig = createMock(YumConfiguration.class);
    expect(yumConfig.getMaxParallelThreadCount()).andReturn(10).anyTimes();
    replay(yumConfig);
    setField(task, "yumConfig", yumConfig);
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
