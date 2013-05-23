/*
 * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
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
