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

    public void resetFrom( Date from )
    {
        calendar.setTime( from );
    }
}
