package org.sonatype.nexus.scheduling;

import java.util.Date;

public interface ScheduleIterator
{
    Date peekNext();

    Date next();

    boolean isFinished();
}
