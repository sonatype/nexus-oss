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
