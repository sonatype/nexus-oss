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

public class CronIterator
    extends AbstractSchedulerIterator
{
    private final String cronExpression;

    public CronIterator( String cronExpression )
    {
        super( new Date() );

        this.cronExpression = cronExpression;
    }

    @Override
    protected Date doPeekNext()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void stepNext()
    {
        // TODO Auto-generated method stub
    }

}
