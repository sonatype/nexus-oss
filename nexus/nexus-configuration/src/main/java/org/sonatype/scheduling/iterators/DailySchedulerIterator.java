package org.sonatype.scheduling.iterators;

import java.util.Calendar;
import java.util.Date;

public class DailySchedulerIterator
    extends AbstractCalendarBasedSchedulerIterator
{
    public DailySchedulerIterator( Date startingDate )
    {
        super( startingDate );
    }

    public DailySchedulerIterator( Date startingDate, Date endingDate )
    {
        super( startingDate, endingDate );
    }

    public void stepNext()
    {
        getCalendar().add( Calendar.DATE, 1 );
    }
}
