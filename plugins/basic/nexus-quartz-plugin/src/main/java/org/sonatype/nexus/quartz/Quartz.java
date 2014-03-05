package org.sonatype.nexus.quartz;

import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;

/**
 * Component managing Quartz {@link Scheduler}.
 *
 * @since 2.8
 */
public interface Quartz
{
  /**
   * Returns the Quartz {@link Scheduler} instance.
   */
  Scheduler getScheduler();

  /**
   * Helper method to ease "one time" execution of a {@link Job}.
   */
  <T extends Job> JobKey execute(final Class<T> clazz);

  /**
   * Helper method to ease scheduling of a {@link Job}.
   */
  <T extends Job> JobKey scheduleJob(final Class<T> clazz, final Trigger trigger);
}
