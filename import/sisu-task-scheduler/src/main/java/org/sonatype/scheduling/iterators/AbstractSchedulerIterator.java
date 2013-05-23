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
        
        Date now = new Date();
        
        // Blow through all iterations up until we reach some point in the future (even a single millisecond will do)
        while ( result != null && result.before( now ))
        {
            stepNext();
            
            result = peekNext();
        }

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
