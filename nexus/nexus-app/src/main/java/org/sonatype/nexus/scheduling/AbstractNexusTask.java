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
package org.sonatype.nexus.scheduling;

import java.util.List;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.feeds.SystemProcess;
import org.sonatype.scheduling.SubmittedTask;

public abstract class AbstractNexusTask
    implements NexusTask
{
    private final Nexus nexus;

    private SystemProcess prc;

    private Logger logger;

    public AbstractNexusTask( Nexus nexus )
    {
        super();

        this.nexus = nexus;
    }

    public Logger getLogger()
    {
        return logger;
    }

    public void setLogger( Logger logger )
    {
        this.logger = logger;
    }

    protected Nexus getNexus()
    {
        return nexus;
    }

    public boolean allowConcurrentExecution( List<SubmittedTask> existingTasks )
    {
        // override if needed
        return false;
    }

    public final void run()
    {
        prc = getNexus().getFeedRecorder().systemProcessStarted( getAction(), getMessage() );

        beforeRun();

        try
        {
            doRun();

            getNexus().getFeedRecorder().systemProcessFinished( prc );

            afterRun();
        }
        catch ( Exception e )
        {
            getNexus().getFeedRecorder().systemProcessBroken( prc, e );
        }
    }

    protected void beforeRun()
    {
        // override if needed
    }

    protected abstract void doRun()
        throws Exception;

    protected void afterRun()
    {
        // override if needed
    }

    protected abstract String getAction();

    protected abstract String getMessage();

}
