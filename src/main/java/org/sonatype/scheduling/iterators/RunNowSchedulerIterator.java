package org.sonatype.scheduling.iterators;

import java.util.Date;

public class RunNowSchedulerIterator
    extends OnceSchedulerIterator
{
    public RunNowSchedulerIterator()
    {
        super( new Date( System.currentTimeMillis() + 500 ) );
    }
}
