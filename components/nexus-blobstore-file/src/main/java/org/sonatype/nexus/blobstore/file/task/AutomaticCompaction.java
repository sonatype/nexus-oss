package org.sonatype.nexus.blobstore.file.task;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.scheduling.ScheduledTask;
import org.sonatype.scheduling.schedules.CronSchedule;
import org.sonatype.scheduling.schedules.Schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows enabling and disabling of automatic compaction of blob stores on an arbitrary cron schedule.
 *
 * If/when we develop a BlobStoreManager, consider having a single task that compacts all blob stores.
 *
 * @since 3.0
 */
@Named
public class AutomaticCompaction
{
  private static final Logger log = LoggerFactory.getLogger(AutomaticCompaction.class);

  private NexusScheduler scheduler;

  @Inject
  public AutomaticCompaction(final NexusScheduler scheduler) {
    this.scheduler = scheduler;
  }

  public void activateAutomaticCompaction(BlobStore blobStore, String cronSettings) throws ParseException {
    final ScheduledTask<?> existingTask = getTask(blobStore);
    if (existingTask != null) {
      existingTask.setSchedule(new CronSchedule(cronSettings));
      existingTask.setEnabled(true);
      scheduler.updateSchedule(existingTask);
    }
    else {
      final CompactTask compactTask = new CompactTask(blobStore);
      Schedule schedule = new CronSchedule(cronSettings);
      final ScheduledTask<Void> schedule1 = scheduler
          .schedule("Automatically submit analytics events", compactTask, schedule);
      log.debug("Scheduled blob compaction for {} with task {}", blobStore.getName(), schedule1);
    }
  }

  public void deactivateAutomaticCompaction(BlobStore blobStore) {
    final ScheduledTask<?> task = getTask(blobStore);
    if (task == null) {
      log.warn("Deactivating automatic compaction for blob store {}, but no scheduled task existed.");
      return;
    }
    log.debug("Disabling blob store compaction for blob store {}.", blobStore.getName());
    task.setEnabled(false);
    scheduler.updateSchedule(task);
  }

  private ScheduledTask<?> getTask(BlobStore blobStore) {
    final List<ScheduledTask<?>> tasks = findScheduledTasks(getTaskIdentifier(blobStore));
    if (tasks.isEmpty()) {
      return null;
    }
    if (tasks.size() > 1) {
      log.warn("More than one scheduled compaction exists for blob store {}.", blobStore.getName());
    }
    return tasks.get(0);
  }

  /**
   * Helper to get all tasks for a given scheduled task type-id.
   */
  private List<ScheduledTask<?>> findScheduledTasks(final String taskTypeId) {
    final List<ScheduledTask<?>> scheduledTasks = scheduler.getActiveTasks().get(taskTypeId);
    if (scheduledTasks == null) {
      return Collections.emptyList();
    }
    return scheduledTasks;
  }

  private String getTaskIdentifier(BlobStore blobStore) {
    return "blobstore.compact." + blobStore.getName();
  }
}
