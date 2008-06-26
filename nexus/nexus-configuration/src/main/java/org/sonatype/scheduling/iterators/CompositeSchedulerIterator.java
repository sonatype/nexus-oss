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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class CompositeSchedulerIterator
    extends AbstractSchedulerIterator
{
    private final List<SchedulerIterator> iterators;

    public CompositeSchedulerIterator( Collection<SchedulerIterator> its )
    {
        super( new Date(), null );

        this.iterators = new ArrayList<SchedulerIterator>( its.size() );

        this.iterators.addAll( its );
    }

    @Override
    protected Date doPeekNext()
    {
        // get the "smallest" date and return it's peekNext();
        return getNextIterator().peekNext();
    }

    @Override
    public void stepNext()
    {
        // get the "smallest" date and return it's next();
        getNextIterator().next();
    }

    @Override
    public boolean isFinished()
    {
        // it is finished if all iterators are finished
        boolean result = false;

        for ( SchedulerIterator i : iterators )
        {
            result = result || i.isFinished();
        }

        return result;
    }

    protected SchedulerIterator getNextIterator()
    {
        Date currDate = null;

        Date nextDate = null;

        SchedulerIterator currIterator = null;

        SchedulerIterator nextIterator = null;

        for ( Iterator<SchedulerIterator> i = iterators.iterator(); i.hasNext(); )
        {
            currIterator = i.next();

            currDate = currIterator.peekNext();

            if ( currDate == null )
            {
                i.remove();
            }
            else
            {
                if ( nextDate == null || currDate.before( nextDate ) )
                {
                    nextDate = currDate;

                    nextIterator = currIterator;
                }
            }
        }
        return nextIterator;
    }

    public void resetFrom( Date from )
    {
        for ( SchedulerIterator iter : iterators )
        {
            iter.resetFrom( from );
        }
    }
}
