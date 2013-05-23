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
package org.sonatype.scheduling.iterators;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

public class WeeklySchedulerIterator
    extends AbstractCalendarBasedSchedulerIterator
{
    private final Set<Integer> weekdaysToRun;

    public WeeklySchedulerIterator( Date startingDate )
    {
        super( startingDate );

        this.weekdaysToRun = null;
    }

    public WeeklySchedulerIterator( Date startingDate, Date endingDate )
    {
        super( startingDate, endingDate );

        this.weekdaysToRun = null;
    }

    public WeeklySchedulerIterator( Date startingDate, Date endingDate, Set<Integer> weekdaysToRun )
    {
        super( startingDate, endingDate );

        this.weekdaysToRun = weekdaysToRun;
    }

    public void stepNext()
    {
        if ( weekdaysToRun == null || weekdaysToRun.isEmpty() )
        {
            getCalendar().add( Calendar.WEEK_OF_YEAR, 1 );
        }
        else
        {
            getCalendar().add( Calendar.DAY_OF_WEEK, 1 );

            while ( !weekdaysToRun.contains( getCalendar().get( Calendar.DAY_OF_WEEK ) ) )
            {
                getCalendar().add( Calendar.DAY_OF_WEEK, 1 );
            }
        }
    }
}
