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

public class MonthlySchedulerIterator
    extends AbstractCalendarBasedSchedulerIterator
{
    private final Set<Integer> monthdaysToRun;

    public MonthlySchedulerIterator( Date startingDate )
    {
        super( startingDate );

        this.monthdaysToRun = null;
    }

    public MonthlySchedulerIterator( Date startingDate, Date endingDate )
    {
        super( startingDate, endingDate );

        this.monthdaysToRun = null;
    }

    public MonthlySchedulerIterator( Date startingDate, Date endingDate, Set<Integer> monthdaysToRun )
    {
        super( startingDate, endingDate );

        this.monthdaysToRun = monthdaysToRun;
    }

    public void stepNext()
    {
        if ( monthdaysToRun == null || monthdaysToRun.isEmpty() )
        {
            getCalendar().add( Calendar.MONTH, 1 );
        }
        else
        {
            getCalendar().add( Calendar.DAY_OF_MONTH, 1 );

            // step over the days not in when to run
            while ( !monthdaysToRun.contains( getCalendar().get( Calendar.DAY_OF_MONTH ) ) )
            {
                getCalendar().add( Calendar.DAY_OF_MONTH, 1 );
            }
        }
    }
}
