package org.sonatype.scheduling;

import java.util.Date;

import org.sonatype.scheduling.iterators.ScheduleIterator;

public interface IteratingTask
    extends SubmittedTask
{
    Date getLastRun();

    Date getNextRun();

    boolean isEnabled();

    void setEnabled( boolean enabled );

    ScheduleIterator getScheduleIterator();
}
