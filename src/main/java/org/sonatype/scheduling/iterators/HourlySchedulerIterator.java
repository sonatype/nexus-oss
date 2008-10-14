package org.sonatype.scheduling.iterators;

import java.util.Calendar;
import java.util.Date;

public class HourlySchedulerIterator
    extends AbstractCalendarBasedSchedulerIterator
{
    public HourlySchedulerIterator( Date startingDate )
    {
        super( startingDate );
    }

    public HourlySchedulerIterator( Date startingDate, Date endingDate )
    {
        super( startingDate, endingDate );
    }

    public void stepNext()
    {
        getCalendar().add( Calendar.HOUR, 1 );
    }
}
