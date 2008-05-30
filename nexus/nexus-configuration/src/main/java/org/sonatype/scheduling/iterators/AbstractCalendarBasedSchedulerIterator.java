package org.sonatype.scheduling.iterators;

import java.util.Calendar;
import java.util.Date;

public abstract class AbstractCalendarBasedSchedulerIterator
    extends AbstractSchedulerIterator
{
    private final Calendar calendar;

    public AbstractCalendarBasedSchedulerIterator( Date startingDate )
    {
        this( startingDate, null );
    }

    public AbstractCalendarBasedSchedulerIterator( Date startingDate, Date endingDate )
    {
        super( startingDate, endingDate );

        this.calendar = Calendar.getInstance();

        calendar.setTime( startingDate );
    }

    protected Calendar getCalendar()
    {
        return calendar;
    }

    public final Date doPeekNext()
    {
        return getCalendar().getTime();
    }
}
