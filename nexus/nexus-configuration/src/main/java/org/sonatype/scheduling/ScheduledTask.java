package org.sonatype.scheduling;

import java.util.Date;

public interface ScheduledTask
    extends SubmittedTask
{
    Date getLastRun();

    Date getNextRun();

    boolean isEnabled();

    void setEnabled( boolean enabled );
    
    ScheduleIterator getScheduleIterator();
}
