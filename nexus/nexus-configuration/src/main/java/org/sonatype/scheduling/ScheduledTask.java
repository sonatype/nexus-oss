package org.sonatype.scheduling;

import org.sonatype.scheduling.schedules.Schedule;

public interface ScheduledTask<T>
    extends IteratingTask<T>
{
    Schedule getSchedule();
}
