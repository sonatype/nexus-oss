package org.sonatype.scheduling;

import java.util.concurrent.Callable;

import org.sonatype.scheduling.schedules.Schedule;

public class DefaultScheduledTask<T>
    extends DefaultIteratingTask<T>
    implements ScheduledTask<T>
{
    private final Schedule schedule;

    public DefaultScheduledTask( String clazz, DefaultScheduler scheduler, Callable<T> callable, Schedule schedule )
    {
        super( clazz, scheduler, callable, schedule.getIterator() );

        this.schedule = schedule;
    }

    // ScheduledTask

    public Schedule getSchedule()
    {
        return schedule;
    }

}
