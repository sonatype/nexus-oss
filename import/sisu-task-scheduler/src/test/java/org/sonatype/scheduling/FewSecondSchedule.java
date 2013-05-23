/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.scheduling;

import java.util.Calendar;
import java.util.Date;

import org.sonatype.scheduling.iterators.AbstractCalendarBasedSchedulerIterator;
import org.sonatype.scheduling.iterators.SchedulerIterator;
import org.sonatype.scheduling.schedules.DailySchedule;

public class FewSecondSchedule
    extends DailySchedule
{

    private final int interval;

    public FewSecondSchedule()
    {
        this( new Date( System.currentTimeMillis() + 500 ), null, 5 );
    }

    public FewSecondSchedule( Date startDate, Date endDate, int interval )
    {
        super( startDate, endDate );
        this.interval = interval;
    }

    @Override
    protected SchedulerIterator createIterator()
    {
        return new FewSecondSchedulerIterator();
    }

    class FewSecondSchedulerIterator
        extends AbstractCalendarBasedSchedulerIterator
    {

        public FewSecondSchedulerIterator()
        {
            super( getStartDate(), getEndDate() );
        }

        @Override
        public void stepNext()
        {
            getCalendar().add( Calendar.SECOND, interval );
        }
    }

}
