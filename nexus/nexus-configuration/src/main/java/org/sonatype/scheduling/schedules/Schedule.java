package org.sonatype.scheduling.schedules;

import org.sonatype.scheduling.iterators.SchedulerIterator;

public interface Schedule
{
    SchedulerIterator getIterator();
}
