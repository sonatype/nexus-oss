package org.sonatype.nexus.scheduling;

import java.util.Date;

public interface ScheduledTask
    extends SubmittedTask
{
    Date lastRun();

    Date nextRun();

    boolean isEnabled();

    void setEnabled( boolean enabled );
}
