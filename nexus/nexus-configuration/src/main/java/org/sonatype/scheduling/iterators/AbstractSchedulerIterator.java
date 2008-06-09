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

public abstract class AbstractSchedulerIterator
    implements SchedulerIterator
{
    private final Date startingDate;

    private final Date endingDate;

    public AbstractSchedulerIterator( Date startingDate )
    {
        this( startingDate, null );
    }

    public AbstractSchedulerIterator( Date startingDate, Date endingDate )
    {
        super();

        if ( startingDate == null )
        {
            throw new NullPointerException( "Starting Date of " + this.getClass().getName() + " cannot be null!" );
        }

        this.startingDate = startingDate;

        this.endingDate = endingDate;
    }

    public Date getStartingDate()
    {
        return startingDate;
    }

    public Date getEndingDate()
    {
        return endingDate;
    }

    public final Date peekNext()
    {
        Date current = doPeekNext();

        if ( current == null || ( getEndingDate() != null && current.after( getEndingDate() ) ) )
        {
            return null;
        }
        else
        {
            return current;
        }
    }

    public final Date next()
    {
        Date result = peekNext();

        stepNext();

        return result;
    }

    public boolean isFinished()
    {
        return peekNext() == null;
    }

    protected abstract Date doPeekNext();

    protected abstract void stepNext();

}
