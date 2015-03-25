/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.yum.internal.task;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;
import org.sonatype.nexus.proxy.maven.MavenHostedRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.maven.routing.Manager;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.scheduling.TaskConfiguration;
import org.sonatype.nexus.scheduling.TaskInfo;
import org.sonatype.nexus.scheduling.TaskScheduler;
import org.sonatype.nexus.yum.YumHosted;
import org.sonatype.nexus.yum.YumRegistry;
import org.sonatype.nexus.yum.YumRepository;
import org.sonatype.nexus.yum.internal.RpmScanner;
import org.sonatype.nexus.yum.internal.support.YumNexusTestSupport;

import com.google.code.tempusfugit.temporal.Condition;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.io.File.pathSeparator;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenerateMetadataTaskConcurrencyIT
    extends YumNexusTestSupport
{

  private static final String RPM_NAME_2 = "hallomommy";

  private static final String RPM_NAME_1 = "hallodaddy";

  public static final int PARALLEL_THREAD_COUNT = 5;

  public static final Logger LOG = LoggerFactory.getLogger(GenerateMetadataTaskConcurrencyIT.class);

  private static final int MAX_PARALLEL_SCHEDULER_THREADS = 20;

  @Inject
  private TaskScheduler nexusScheduler;

  @Inject
  private RepositoryRegistry repositoryRegistry;

  @Inject
  private YumRegistry yumRegistry;

  private final Set<String> threadNames = new HashSet<String>();

  @After
  public void waitForAllTasks()
      throws TimeoutException, InterruptedException
  {
    waitFor(new Condition()
    {
      @Override
      public boolean isSatisfied() {
        return nexusScheduler.listsTasks().isEmpty();
      }
    });
  }

  @Test
  @Ignore(
      "One cannot override Task execute as done in createYumRepositoryTask anymore, as Quartz is re-instantiating the task instance")
  public void shouldExecuteSeveralThreadInParallel()
      throws Exception
  {
    List<TaskInfo> futures = Lists.newArrayList();

    for (int repositoryId = 0; repositoryId < PARALLEL_THREAD_COUNT; repositoryId++) {
      futures.add(nexusScheduler.submit(createYumRepositoryTask(repositoryId).taskConfiguration()));
    }

    waitFor(futures);
  }

  @Test
  public void shouldReuseQueuedTaskOfTheSameType()
      throws Exception
  {
    final File tmpDir = copyToTempDir(rpmsDir());

    final MavenHostedRepository repository = mock(MavenHostedRepository.class);
    when(repository.getId()).thenReturn("REPO");
    when(repository.getLocalUrl()).thenReturn(osIndependentUri(tmpDir));
    when(repository.getProviderRole()).thenReturn(Repository.class.getName());
    when(repository.getProviderHint()).thenReturn("maven2");
    when(repository.adaptToFacet(HostedRepository.class)).thenReturn(repository);
    when(repository.adaptToFacet(MavenRepository.class)).thenReturn(repository);
    when(repository.adaptToFacet(MavenHostedRepository.class)).thenReturn(repository);
    final RepositoryItemUid uid = mock(RepositoryItemUid.class);
    when(uid.getLock()).thenReturn(mock(RepositoryItemUidLock.class));
    when(repository.createUid(anyString())).thenReturn(uid);
    when(repository.getRepositoryContentClass()).thenReturn(new Maven2ContentClass());
    when(repository.isExposed()).thenReturn(true);
    final RepositoryKind repositoryKind = mock(RepositoryKind.class);
    Mockito.<Class<?>>when(repositoryKind.getMainFacet()).thenReturn(MavenHostedRepository.class);
    when(repositoryKind.isFacetAvailable(HostedRepository.class)).thenReturn(true);
    when(repositoryKind.isFacetAvailable(MavenRepository.class)).thenReturn(true);
    when(repositoryKind.isFacetAvailable(MavenHostedRepository.class)).thenReturn(true);
    when(repository.getRepositoryKind()).thenReturn(repositoryKind);

    final File rpm1 = createDummyRpm(RPM_NAME_1, "1", new File(tmpDir, "rpm1"));
    final File rpm2 = createDummyRpm(RPM_NAME_2, "2", new File(tmpDir, "rpm2"));

    // given executions blocking all thread of the scheduler
    final List<TaskInfo> futures = Lists.newArrayList();
    for (int index = 0; index < MAX_PARALLEL_SCHEDULER_THREADS; index++) {
      futures.add(nexusScheduler.submit(nexusScheduler.createTaskConfigurationInstance(WaitTask.class)));
    }
    repositoryRegistry.addRepository(repository);
    final YumHosted yum = (YumHosted) yumRegistry.register(repository);

    // when
    final String file1 = "rpm1/" + rpm1.getName();
    final String file2 = "rpm2/" + rpm2.getName();

    final TaskInfo first = yum.addRpmAndRegenerate(file1);
    final TaskInfo second = yum.addRpmAndRegenerate(file2);
    futures.add(first);
    futures.add(second);

    waitFor(futures);
    // then

    // instance not anymore, rescheduled task's taskInfo is recreated
    // assertThat(second, is(first));
    // but is same task
    assertThat(second.getId(), is(first.getId()));
    // but second one has the "actual" config! first shows state at the moment it was scheduled
    assertThat(second.getConfiguration().getString(GenerateMetadataTask.PARAM_ADDED_FILES),
        is(file1 + pathSeparator + file2));
  }

  private void waitFor(List<TaskInfo> futures)
      throws ExecutionException, InterruptedException
  {
    for (TaskInfo future : futures) {
      Future<?> f = future.getCurrentState().getFuture();
      if (f != null) {
        f.get();
      }
    }
  }

  /**
   * This method is broken: you cannot override task like this anymore.
   */
  private GenerateMetadataTask createYumRepositoryTask(final int repositoryId)
      throws Exception
  {
    final GenerateMetadataTask task = new GenerateMetadataTask(
        yumRegistry,
        mock(RpmScanner.class),
        mock(Manager.class)
    )
    {
      @Override
      protected YumRepository execute()
          throws Exception
      {
        String threadName = Thread.currentThread().getName();
        LOG.debug("Thread name : {}", threadName);
        if (!threadNames.add(threadName)) {
          Assert.fail("Uses the same thread : " + threadName);
        }
        Thread.sleep(100);
        return null;
      }
    };
    final TaskConfiguration taskCfg = nexusScheduler.createTaskConfigurationInstance(GenerateMetadataTask.class);
    taskCfg.setId("foo");
    taskCfg.setTypeId(GenerateMetadataTask.class.getSimpleName());
    task.configure(taskCfg);
    task.setRepositoryId("REPO_" + repositoryId);
    return task;
  }

}
