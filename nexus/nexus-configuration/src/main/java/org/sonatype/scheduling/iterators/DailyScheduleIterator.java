package org.sonatype.scheduling.iterators;

import java.util.Calendar;
import java.util.Date;

public class DailyScheduleIterator
    extends AbstractCalendarBasedScheduleIterator
{
    public DailyScheduleIterator( Date startingDate )
    {
        super( startingDate );
    }

    public DailyScheduleIterator( Date startingDate, Date endingDate )
    {
        super( startingDate, endingDate );
    }

    public void stepNext()
    {
        getCalendar().add( Calendar.DATE, 1 );
    }
}
