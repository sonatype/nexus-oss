package org.sonatype.nexus.plugins.yum.repository.task;

import static java.io.File.pathSeparator;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.plugins.yum.repository.task.YumMetadataGenerationTask.ID;
import static org.sonatype.nexus.test.reflection.ReflectionTestUtils.findMethod;
import static org.sonatype.nexus.test.reflection.ReflectionTestUtils.setField;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.component.annotations.Requirement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.plugins.yum.repository.AbstractSchedulerTest;
import org.sonatype.nexus.plugins.yum.repository.YumRepository;
import org.sonatype.nexus.plugins.yum.repository.service.YumService;
import org.sonatype.nexus.plugins.yum.repository.utils.RepositoryTestUtils;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.scheduling.ScheduledTask;

import com.google.code.tempusfugit.temporal.Condition;

public class YumMetadataGenerationTaskConcurrencyTest extends AbstractSchedulerTest {
  private static final String RPM_NAME_2 = "hallomommy";
  private static final String RPM_NAME_1 = "hallodaddy";
  public static final int PARALLEL_THREAD_COUNT = 5;
  public static final Logger LOG = LoggerFactory.getLogger(YumMetadataGenerationTaskConcurrencyTest.class);
  private static final int MAX_PARALLEL_SCHEDULER_THREADS = 20;

  @Requirement
  private NexusScheduler nexusScheduler;

  @Requirement
  private YumService yumService;

  private final Set<String> threadNames = new HashSet<String>();

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
    List<ScheduledTask<?>> futures = new ArrayList<ScheduledTask<?>>();

    for (int repositoryId = 0; repositoryId < PARALLEL_THREAD_COUNT; repositoryId++) {
      futures.add(nexusScheduler.submit(ID, createYumRepositoryTask(repositoryId)));
    }

    waitFor(futures);
  }

  @Test
  public void shouldReuseQueuedTaskOfTheSameType() throws Exception {
    final File tmpDir = RepositoryTestUtils.copyToTempDir(RepositoryTestUtils.RPM_BASE_FILE);
    final Repository repository = mock(Repository.class);
    when(repository.getId()).thenReturn("REPO");
    when(repository.getLocalUrl()).thenReturn(tmpDir.getAbsolutePath());
    final File rpm1 = RepositoryTestUtils.createDummyRpm(RPM_NAME_1, "1", new File(tmpDir, "rpm1"));
    final File rpm2 = RepositoryTestUtils.createDummyRpm(RPM_NAME_2, "2", new File(tmpDir, "rpm2"));
    // given executions blocking all thread of the scheduler
    final List<ScheduledTask<?>> futures = new ArrayList<ScheduledTask<?>>();
    for (int index = 0; index < MAX_PARALLEL_SCHEDULER_THREADS; index++) {
      futures.add(nexusScheduler.submit("WaitTask", nexusScheduler.createTaskInstance(WaitTask.class)));
    }
    // when
    final String file1 = "rpm1/" + rpm1.getName();
    final String file2 = "rpm2/" + rpm2.getName();
    final ScheduledTask<YumRepository> first = yumService.addToYumRepository(repository, file1);
    final ScheduledTask<YumRepository> second = yumService.addToYumRepository(repository, file2);
    futures.add(first);
    futures.add(second);

    waitFor(futures);
    // then
    assertThat(second, is(first));
    assertThat(((YumMetadataGenerationTask) first.getTask()).getAddedFiles(), is(file1 + pathSeparator + file2));
    final String content = IOUtils.toString(new GZIPInputStream(new FileInputStream(new File(tmpDir, "repodata/primary.xml.gz"))));
    assertThat(content, containsString(RPM_NAME_1));
    assertThat(content, containsString(RPM_NAME_2));
  }

  private void waitFor(List<ScheduledTask<?>> futures) throws ExecutionException, InterruptedException {
    for (ScheduledTask<?> future : futures) {
      future.get();
    }
  }

  private YumMetadataGenerationTask createYumRepositoryTask(final int repositoryId) throws Exception {
    YumTaskInterceptor interceptor = new YumTaskInterceptor(nexusScheduler.createTaskInstance(YumMetadataGenerationTask.class), threadNames);
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(YumMetadataGenerationTask.class);
    enhancer.setCallback(interceptor);
    YumMetadataGenerationTask task = (YumMetadataGenerationTask) enhancer.create();
    setField(task, "applicationEventMulticaster", lookup(ApplicationEventMulticaster.class));
    setField(task, "logger", LoggerFactory.getLogger(YumMetadataGenerationTask.class));
    task.setRepositoryId("REPO_" + repositoryId);
    return task;
  }

  private static class YumTaskInterceptor implements MethodInterceptor {

    private final YumMetadataGenerationTask task;
    private final Set<String> threadNames;

    public YumTaskInterceptor(YumMetadataGenerationTask task, Set<String> threadNames) {
      this.task = task;
      this.threadNames = threadNames;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
      if ("doRun".equals(method.getName())) {
        String threadName = Thread.currentThread().getName();
        LOG.info("Thread name : {}", threadName);
        if (!threadNames.add(threadName)) {
          Assert.fail("Uses the same thread : " + threadName);
        }
        Thread.sleep(100);
        return null;
      }

      Method origMethod = findMethod(YumMetadataGenerationTask.class, method.getName(), method.getParameterTypes());
      return origMethod.invoke(task, args);
    }

  }
}
