package org.sonatype.scheduling.iterators;

import java.util.Date;

public interface SchedulerIterator
{
    Date peekNext();

    Date next();

    boolean isFinished();
}
