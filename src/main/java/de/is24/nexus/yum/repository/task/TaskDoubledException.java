package de.is24.nexus.yum.repository.task;

import java.util.concurrent.RejectedExecutionException;

import org.sonatype.scheduling.ScheduledTask;

public class TaskDoubledException extends RejectedExecutionException {
  private static final long serialVersionUID = 1L;

  private final ScheduledTask<?> original;

  public TaskDoubledException(ScheduledTask<?> original, String message) {
    super(message);
    this.original = original;
  }

  public ScheduledTask<?> getOriginal() {
    return original;
  }
}
