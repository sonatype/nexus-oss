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
package org.sonatype.scheduling;

import java.util.Date;

import org.sonatype.scheduling.iterators.SchedulerIterator;
import org.sonatype.scheduling.schedules.AbstractSchedule;
import org.sonatype.scheduling.schedules.Schedule;

public class SecondSchedule
    extends AbstractSchedule
    implements Schedule
{
    public SecondSchedule( Date startDate, Date endDate )
    {
        super( startDate, endDate );
    }
    public SchedulerIterator getIterator()
    {
        return new SecondScheduleIterator( getStartDate(), getEndDate() );
    }

}
