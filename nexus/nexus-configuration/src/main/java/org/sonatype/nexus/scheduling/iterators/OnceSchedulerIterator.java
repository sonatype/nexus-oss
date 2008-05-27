package org.sonatype.nexus.scheduling.iterators;

import java.util.Date;

public class OnceSchedulerIterator
    extends DailyScheduleIterator
{

    public OnceSchedulerIterator( Date startingDate )
    {
        super( startingDate, startingDate );
    }

}
