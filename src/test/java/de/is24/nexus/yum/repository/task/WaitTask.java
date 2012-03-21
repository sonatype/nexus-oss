package de.is24.nexus.yum.repository.task;

import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;

@Component(role = SchedulerTask.class, hint = "WaitTask", instantiationStrategy = "per-lookup")
public class WaitTask extends AbstractNexusTask<Object> {
  public static final Logger LOG = LoggerFactory.getLogger(WaitTask.class);

  @Override
  protected Object doRun() throws Exception {
    LOG.info("Go to sleep for a sec.");
    Thread.sleep(1000);
    return null;
  }

  @Override
  public boolean allowConcurrentExecution(Map<String, List<ScheduledTask<?>>> activeTasks) {
    return true;
  }

  @Override
  protected String getAction() {
    return "Wait";
  }

  @Override
  protected String getMessage() {
    return "Wait";
  }

}
