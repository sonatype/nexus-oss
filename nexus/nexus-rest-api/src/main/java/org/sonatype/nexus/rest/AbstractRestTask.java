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
package org.sonatype.nexus.rest;

import java.util.concurrent.Callable;

import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.feeds.SystemProcess;

public abstract class AbstractRestTask<T>
    implements Callable<T>
{
    private final Nexus nexus;

    private SystemProcess prc;

    public AbstractRestTask( Nexus nexus )
    {
        super();

        this.nexus = nexus;
    }

    protected Nexus getNexus()
    {
        return nexus;
    }

    public final T call()
        throws Exception
    {
        prc = getNexus().getFeedRecorder().systemProcessStarted( getAction(), getMessage() );

        T result = null;

        beforeRun();

        try
        {
            result = doRun();

            getNexus().getFeedRecorder().systemProcessFinished( prc );
        }
        catch ( Exception e )
        {
            getNexus().getFeedRecorder().systemProcessBroken( prc, e );

            throw e;
        }

        afterRun();

        return result;
    }

    protected void beforeRun()
    {
    }

    protected abstract T doRun() throws Exception;

    protected void afterRun()
    {
    }

    protected abstract String getAction();

    protected abstract String getMessage();

}
