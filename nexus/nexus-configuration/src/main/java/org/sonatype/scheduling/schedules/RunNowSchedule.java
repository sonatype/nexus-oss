package org.sonatype.scheduling.schedules;

import java.util.Date;

public class RunNowSchedule
    extends OnceSchedule
{
    public RunNowSchedule()
    {
        super( new Date( System.currentTimeMillis() + 500 ) );
    }
}
