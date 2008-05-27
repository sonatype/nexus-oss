package org.sonatype.nexus.scheduling;

import java.util.Date;

public interface ScheduledTask
    extends SubmittedTask
{
    Date lastRun();

    Date nextRun();

    boolean isPaused();

    void setPaused( boolean paused );
}
