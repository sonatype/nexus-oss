package de.is24.nexus.yum.service.impl;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.codehaus.plexus.component.annotations.Requirement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.scheduling.ScheduledTask;

import com.google.code.tempusfugit.temporal.Condition;

import de.is24.nexus.yum.repository.AbstractSchedulerTest;
import de.is24.nexus.yum.repository.YumGeneratorConfiguration;
import de.is24.nexus.yum.repository.YumMetadataGenerationTask;
import de.is24.nexus.yum.repository.YumRepository;
import de.is24.nexus.yum.repository.YumRepositoryGeneratorJob;


public class ThreadPoolYumRespositoryCreatorServiceTest extends AbstractSchedulerTest {
  public static final int PARALLEL_THREAD_COUNT = 5;
  public static final Logger LOG = LoggerFactory.getLogger(ThreadPoolYumRespositoryCreatorServiceTest.class);

  @Requirement
  private YumRepositoryCreatorService service;

  private final Set<String> threadNames = new HashSet<String>();

  private final int currentOrder = 0;

  @After
  public void waitForAllTasks() throws TimeoutException, InterruptedException {
    waitFor(new Condition() {
        @Override
        public boolean isSatisfied() {
          return nexusScheduler.getActiveTasks().isEmpty();
        }
      });
  }

  @Test
  public void shouldExecuteSeveralThreadInParallel() throws Exception {
    List<ScheduledTask<YumRepository>> futures = new ArrayList<ScheduledTask<YumRepository>>();

    for (int repositoryId = 0; repositoryId < PARALLEL_THREAD_COUNT; repositoryId++) {
      futures.add(service.submit(taskFromJob(createYumRepositoryJob(repositoryId))));
    }

    for (ScheduledTask<YumRepository> future : futures) {
      future.get();
    }
  }

  private YumRepositoryGeneratorJob createYumRepositoryJob(final int repositoryId) {
    return new YumRepositoryGeneratorJob(mockConfig("REPO_" + repositoryId)) {
      @Override
      public YumRepository call() throws Exception {
        String threadName = Thread.currentThread().getName();
        LOG.info("Thread name : {}", threadName);
        if (!threadNames.add(threadName)) {
          Assert.fail("Uses the same thread : " + threadName);
        }
        Thread.sleep(100);
        return null;
      }
    };
  }

  private YumGeneratorConfiguration mockConfig(String repositoryId) {
    final YumGeneratorConfiguration config = createNiceMock(YumGeneratorConfiguration.class);
    expect(config.getId()).andReturn(repositoryId).anyTimes();
    replay(config);
    return config;
  }

  private YumMetadataGenerationTask taskFromJob(YumRepositoryGeneratorJob job) {
    YumMetadataGenerationTask task = nexusScheduler.createTaskInstance(YumMetadataGenerationTask.class);
    task.setJob(job);
    return task;
  }
}
