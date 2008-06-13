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

import java.text.ParseException;
import java.util.Date;

import org.sonatype.scheduling.iterators.CronIterator;
import org.sonatype.scheduling.iterators.SchedulerIterator;
import org.sonatype.scheduling.iterators.cron.CronExpression;

public class CronSchedule
    extends AbstractSchedule
{
    private final String cronString;

    private final CronExpression cronExpression;

    public CronSchedule( String cronExpression )
        throws ParseException
    {
        super( new Date(), null );

        this.cronString = cronExpression;

        this.cronExpression = new CronExpression( cronString );
    }

    public String getCronString()
    {
        return cronString;
    }

    public SchedulerIterator getIterator()
    {
        return new CronIterator( cronExpression );
    }

}
