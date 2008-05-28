package org.sonatype.scheduling.iterators;

import java.util.Date;

public interface ScheduleIterator
{
    Date peekNext();

    Date next();

    boolean isFinished();
}
