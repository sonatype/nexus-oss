/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
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
