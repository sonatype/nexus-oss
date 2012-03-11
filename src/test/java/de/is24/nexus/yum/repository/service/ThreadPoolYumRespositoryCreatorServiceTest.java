package de.is24.nexus.yum.repository.service;

import static de.is24.nexus.yum.repository.task.YumMetadataGenerationTask.ID;
import static de.is24.test.reflection.ReflectionTestUtils.findMethod;
import static de.is24.test.reflection.ReflectionTestUtils.setField;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.codehaus.plexus.component.annotations.Requirement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.scheduling.ScheduledTask;

import com.google.code.tempusfugit.temporal.Condition;

import de.is24.nexus.yum.repository.AbstractSchedulerTest;
import de.is24.nexus.yum.repository.YumRepository;
import de.is24.nexus.yum.repository.config.YumGeneratorConfiguration;
import de.is24.nexus.yum.repository.task.YumMetadataGenerationTask;


public class ThreadPoolYumRespositoryCreatorServiceTest extends AbstractSchedulerTest {
  public static final int PARALLEL_THREAD_COUNT = 5;
  public static final Logger LOG = LoggerFactory.getLogger(ThreadPoolYumRespositoryCreatorServiceTest.class);

  @Requirement
  private NexusScheduler nexusScheduler;

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
    List<ScheduledTask<YumRepository>> futures = new ArrayList<ScheduledTask<YumRepository>>();

    for (int repositoryId = 0; repositoryId < PARALLEL_THREAD_COUNT; repositoryId++) {
			futures.add(nexusScheduler.submit(ID, createYumRepositoryTask(repositoryId)));
    }

    for (ScheduledTask<YumRepository> future : futures) {
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
        setField(task, "logger", LoggerFactory.getLogger( YumMetadataGenerationTask.class ));
  	task.setConfiguration(mockConfig("REPO_" + repositoryId));
  	return task;
  }

	private YumGeneratorConfiguration mockConfig(String repositoryId) {
    final YumGeneratorConfiguration config = createNiceMock(YumGeneratorConfiguration.class);
    expect(config.getId()).andReturn(repositoryId).anyTimes();
    replay(config);
    return config;
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
