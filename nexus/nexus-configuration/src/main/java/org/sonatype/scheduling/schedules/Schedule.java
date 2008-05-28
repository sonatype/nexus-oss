package org.sonatype.scheduling.schedules;

import org.sonatype.scheduling.iterators.ScheduleIterator;

public interface Schedule
{
    ScheduleIterator getIterator();
}
