package de.is24.nexus.yum.service.impl;

import static java.util.Arrays.asList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.sonatype.scheduling.ProgressListener;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.SchedulerTask;
import org.sonatype.scheduling.TaskState;
import org.sonatype.scheduling.iterators.SchedulerIterator;
import org.sonatype.scheduling.schedules.Schedule;
import de.is24.nexus.yum.repository.YumRepository;
import de.is24.nexus.yum.repository.task.YumMetadataGenerationTask;


public class ScheduledTaskAdapter implements ScheduledTask<YumRepository> {
  private final YumMetadataGenerationTask task;
  private final Future<YumRepository> future;

  public ScheduledTaskAdapter(YumMetadataGenerationTask task, Future<YumRepository> future) {
    this.task = task;
    this.future = future;
  }

  @Override
  public SchedulerTask<YumRepository> getSchedulerTask() {
    return task;
  }

  @Override
  public ProgressListener getProgressListener() {
    return null;
  }

  @Override
  public Callable<YumRepository> getTask() {
    return task;
  }

  @Override
  public String getId() {
    return task.getId();
  }

  @Override
  public String getName() {
    return task.getName();
  }

  @Override
  public void setName(String name) {
    throw new IllegalStateException("Can't set name anymore");
  }

  @Override
  public String getType() {
    return YumMetadataGenerationTask.ID;
  }

  @Override
  public TaskState getTaskState() {
    throw new IllegalStateException("Unsupported method!");
  }

  @Override
  public Date getScheduledAt() {
    throw new IllegalStateException("Unsupported method!");
  }

  @Override
  public void runNow() {
    throw new IllegalStateException("Unsupported method!");
  }

  @Override
  public void cancelOnly() {
    throw new IllegalStateException("Unsupported method!");
  }

  @Override
  public void cancel() {
    throw new IllegalStateException("Unsupported method!");
  }

  @Override
  public void cancel(boolean interrupt) {
    throw new IllegalStateException("Unsupported method!");
  }

  @Override
  public void reset() {
    throw new IllegalStateException("Unsupported method!");
  }

  @Override
  public Throwable getBrokenCause() {
    throw new IllegalStateException("Unsupported method!");
  }

  @Override
  public YumRepository get() throws ExecutionException, InterruptedException {
    return future.get();
  }

  @Override
  public YumRepository getIfDone() {
    try {
      return future.get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Date getLastRun() {
    throw new IllegalStateException("Unsupported method!");
  }

  @Override
  public TaskState getLastStatus() {
    throw new IllegalStateException("Unsupported method!");
  }

  @Override
  public Long getDuration() {
    throw new IllegalStateException("Unsupported method!");
  }

  @Override
  public Date getNextRun() {
    throw new IllegalStateException("Unsupported method!");
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void setEnabled(boolean enabled) {
    throw new IllegalStateException("Unsupported method!");
  }

  @Override
  public List<YumRepository> getResults() {
    try {
      return asList(future.get());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public SchedulerIterator getScheduleIterator() {
    throw new IllegalStateException("Unsupported method!");
  }

  @Override
  public Schedule getSchedule() {
    throw new IllegalStateException("Unsupported method!");
  }

  @Override
  public void setSchedule(Schedule schedule) {
    throw new IllegalStateException("Unsupported method!");
  }

  @Override
  public Map<String, String> getTaskParams() {
    throw new IllegalStateException("Unsupported method!");
  }
}
