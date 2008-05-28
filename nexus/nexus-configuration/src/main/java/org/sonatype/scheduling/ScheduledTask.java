package org.sonatype.scheduling;

import org.sonatype.scheduling.schedules.Schedule;

public interface ScheduledTask
    extends IteratingTask
{
    Schedule getSchedule();
}
