package org.sonatype.scheduling.schedules;

import java.util.Date;

public abstract class AbstractSchedule
    implements Schedule
{
    private final Date startDate;

    private final Date endDate;

    public AbstractSchedule( Date startDate, Date endDate )
    {
        super();

        this.startDate = startDate;

        this.endDate = endDate;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }
}
