package de.is24.nexus.yum.service.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import javax.inject.Inject;
import javax.inject.Named;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.plugin.Managed;
import org.sonatype.scheduling.ScheduledTask;
import com.google.inject.Singleton;
import de.is24.nexus.yum.repository.YumMetadataGenerationTask;
import de.is24.nexus.yum.repository.YumRepository;
import de.is24.nexus.yum.repository.YumRepositoryGeneratorJob;


/**
 * Created by IntelliJ IDEA.
 * User: BVoss
 * Date: 28.07.11
 * Time: 18:16
 * To change this template use File | Settings | File Templates.
 */
@Managed
@Named(YumRepositoryCreatorService.DEFAULT_BEAN_NAME)
@Singleton
public class ThreadPoolYumRepositoryCreatorService implements YumRepositoryCreatorService {
  private static final int THREAD_POOL_SIZE = 10;

  private volatile boolean isShutdown = false;

  private Thread dispatcherThread;

  private static final Set<String> CURRENT_RUNNING_REPOS = Collections.synchronizedSet(new HashSet<String>());

  private final PriorityBlockingQueue<YumRepositoryGeneratorJobFutureTask> workQueue =
    new PriorityBlockingQueue<YumRepositoryGeneratorJobFutureTask>();

  private ThreadPoolExecutor executorService;

  @Inject
  private NexusScheduler nexusScheduler;

  public ThreadPoolYumRepositoryCreatorService() {
    activate();
  }

  @Override
  public <T> T createTaskInstance(Class<T> taskClass) {
    return nexusScheduler.createTaskInstance(taskClass);
  }

  @Override
  public boolean isShutdown() {
    return isShutdown;
  }

  @Override
  public int size() {
    return workQueue.size();
  }

  @Override
  public ScheduledTask<YumRepository> submit(final YumMetadataGenerationTask yumMetadataGenerationTask) {
    if (isShutdown) {
      throw new IllegalStateException("don't accept new jobs when shutdown");
    }

    final YumRepositoryGeneratorJobFutureTask result = new YumRepositoryGeneratorJobFutureTask(
      yumMetadataGenerationTask.getJob());
    workQueue.put(result);
    return new ScheduledTaskAdapter(yumMetadataGenerationTask, result);
  }

  @Override
  public synchronized void shutdown() {
    isShutdown = true;
    dispatcherThread.interrupt();
    executorService.shutdownNow();
    workQueue.clear();
    CURRENT_RUNNING_REPOS.clear();
    dispatcherThread = null;
    executorService = null;
  }

  @Override
  public synchronized void activate() {
    executorService = newThreadPool();
    isShutdown = false;
    dispatcherThread = new Thread(new DispatcherRunnable());
    dispatcherThread.start();
  }

  private static ThreadPoolExecutor newThreadPool() {
    return (ThreadPoolExecutor) Executors.newFixedThreadPool(THREAD_POOL_SIZE);
  }

  @Override
  public int getActiveWorkerCount() {
    return (executorService != null) ? executorService.getActiveCount() : 0;
  }

  private final class DispatcherRunnable implements Runnable {
    final List<YumRepositoryGeneratorJobFutureTask> othersOfSameRepoAreRunning =
      new LinkedList<YumRepositoryGeneratorJobFutureTask>();

    @Override
    public void run() {
      YumRepositoryGeneratorJobFutureTask current;
      while (!isShutdown) {
        current = workQueue.poll();
        if (current == null) {
          pushBackTemporallyPolledTasks();
          try {
            //give the executor time to do his work in case of queue contains only elements waiting for others with same repositoryId
            Thread.sleep(50);

            //use take in case of an empty queue and wait for an element to dispatch
            current = workQueue.take();
          } catch (InterruptedException ie) {
            //may occur on shutdown
          }
        }
        if (!isShutdown && (current != null)) {
          if (noTaskForRepositoryPresent(current.getRepoId())) {
            executorService.execute(current);
            pushBackTemporallyPolledTasks();
          } else {
            othersOfSameRepoAreRunning.add(current);
          }
        }
      }
    }

    private boolean noTaskForRepositoryPresent(final String repoId) {
      final boolean b = CURRENT_RUNNING_REPOS.add(repoId);
      return b;
    }

    private void pushBackTemporallyPolledTasks() {
      workQueue.addAll(othersOfSameRepoAreRunning);
      othersOfSameRepoAreRunning.clear();
    }
  }


  private static final class YumRepositoryGeneratorJobFutureTask extends FutureTask<YumRepository>
    implements Comparable<YumRepositoryGeneratorJobFutureTask> {
    private final String repoId;
    private final Long creationTime = System.currentTimeMillis();

    private YumRepositoryGeneratorJobFutureTask(YumRepositoryGeneratorJob yumRepositoryGeneratorJob) {
      super(yumRepositoryGeneratorJob);
      repoId = yumRepositoryGeneratorJob.getRepositoryId();
    }

    String getRepoId() {
      return repoId;
    }

    @Override
    protected void done() {
      CURRENT_RUNNING_REPOS.remove(repoId);
    }

    @Override
    public int compareTo(YumRepositoryGeneratorJobFutureTask o) {
      return o.creationTime.compareTo(this.creationTime);
    }
  }


}
