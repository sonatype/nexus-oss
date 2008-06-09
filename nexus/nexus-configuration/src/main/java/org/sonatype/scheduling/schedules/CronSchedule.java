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
package org.sonatype.scheduling.schedules;

import org.sonatype.scheduling.iterators.CronIterator;
import org.sonatype.scheduling.iterators.SchedulerIterator;

public class CronSchedule
    extends AbstractSchedule
{
    private final String cronExpression;

    public CronSchedule( String cronExpression )
    {
        super( null, null );

        this.cronExpression = cronExpression;
    }

    public String getCronExpression()
    {
        return cronExpression;
    }

    public SchedulerIterator getIterator()
    {
        return new CronIterator( cronExpression );
    }

}
