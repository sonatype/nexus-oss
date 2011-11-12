package de.is24.nexus.yum.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.scheduling.ScheduledTask;
import de.is24.nexus.yum.repository.AbstractSchedulerTest;
import de.is24.nexus.yum.repository.YumMetadataGenerationTask;
import de.is24.nexus.yum.repository.YumRepository;
import de.is24.nexus.yum.repository.YumRepositoryGeneratorJob;


public class ThreadPoolYumRespositoryCreatorServiceTest extends AbstractSchedulerTest {
  public static final int PARALLEL_THREAD_COUNT = 5;
  public static final Logger LOG = LoggerFactory.getLogger(ThreadPoolYumRespositoryCreatorServiceTest.class);
  private final ThreadPoolYumRepositoryCreatorService service = new ThreadPoolYumRepositoryCreatorService();
  private final Set<String> threadNames = new HashSet<String>();
  private int currentOrder = 0;

  @Test
  public void shouldExecuteSeveralThreadInParallel() throws Exception {
    List<ScheduledTask<YumRepository>> futures = new ArrayList<ScheduledTask<YumRepository>>();

    for (int repositoryId = 0; repositoryId < PARALLEL_THREAD_COUNT; repositoryId++) {
      futures.add(service.submit(taskFromJob(createYumRepositoryTask(repositoryId))));
    }

    for (ScheduledTask<YumRepository> future : futures) {
      future.get();
    }
  }

  @Test
  public void shouldExecuteTasksForTheSameRepositoryInCreationOrder() throws Exception {
    List<ScheduledTask<YumRepository>> futures = new ArrayList<ScheduledTask<YumRepository>>();

    for (int order = 1; order < PARALLEL_THREAD_COUNT; order++) {
      futures.add(service.submit(taskFromJob(createOrderedYumRepositoryJob(order))));
      Thread.sleep(500);
    }

    for (ScheduledTask<YumRepository> future : futures) {
      future.get();
    }
    Assert.assertEquals(PARALLEL_THREAD_COUNT - 1, currentOrder);
  }

  private YumRepositoryGeneratorJob createOrderedYumRepositoryJob(final int order) {
    return new YumRepositoryGeneratorJob(null) {
      @Override
      public String getRepositoryId() {
        return "CONSTANT_REPO";
      }

      @Override
      public YumRepository call() throws Exception {
        checkOrder(order);
        Thread.sleep(500);
        checkOrderEqual(order);
        return null;
      }
    };
  }

  public synchronized void checkOrder(int order) {
    if (order != (currentOrder + 1)) {
      Assert.fail("Execution is not in correct order. Current order is " + currentOrder +
        " and now tried to execute order " +
        order);
    }
    currentOrder++;
  }

  public synchronized void checkOrderEqual(final int order) {
    if (currentOrder != order) {
      Assert.fail("CurrentOrder (" + currentOrder + ") not equal to " + order);
    }
  }

  private YumRepositoryGeneratorJob createYumRepositoryTask(final int repositoryId) {
    return new YumRepositoryGeneratorJob(null) {
      @Override
      public String getRepositoryId() {
        return "REPO_" + repositoryId;
      }

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

  private YumMetadataGenerationTask taskFromJob(YumRepositoryGeneratorJob job) {
    YumMetadataGenerationTask task = scheduler.createTaskInstance(YumMetadataGenerationTask.class);
    task.setJob(job);
    return task;
  }
}
