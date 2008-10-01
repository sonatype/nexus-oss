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

import java.util.Date;

import org.sonatype.scheduling.iterators.cron.CronExpression;

public class CronIterator
    extends AbstractSchedulerIterator
{
    private final CronExpression cronExpression;

    private Date nextDate;

    public CronIterator( CronExpression cronExpression )
    {
        super( new Date() );

        this.cronExpression = cronExpression;
    }

    @Override
    protected Date doPeekNext()
    {
        if ( nextDate == null )
        {
            nextDate = cronExpression.getNextValidTimeAfter( new Date() );
        }

        return nextDate;
    }

    @Override
    protected void stepNext()
    {
        if ( nextDate == null )
        {
            doPeekNext();
        }
        else
        {
            nextDate = cronExpression.getNextValidTimeAfter( doPeekNext() );
        }
    }

    public void resetFrom( Date from )
    {
        this.nextDate = cronExpression.getNextValidTimeAfter( from );
    }
}
