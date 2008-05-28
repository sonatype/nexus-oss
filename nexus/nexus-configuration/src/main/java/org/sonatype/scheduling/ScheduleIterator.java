package org.sonatype.scheduling;

import java.util.Date;

public interface ScheduleIterator
{
    Date peekNext();

    Date next();

    boolean isFinished();
}
