/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.tasks.api;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.SchedulerTask;

@Component( role = SchedulerTask.class, hint = "SleepRepositoryTask", instantiationStrategy = "per-lookup" )
public class SleepRepositoryTask
    extends AbstractNexusRepositoriesTask<Object>
{

    @Override
    protected Object doRun()
        throws Exception
    {
        getLogger().debug( getMessage() );

        final int time = getTime();
        sleep( time );
        getRepositoryRegistry().getRepository( getRepositoryId() );
        sleep( time );
        return null;
    }

    protected void sleep( final int time )
        throws InterruptedException
    {
        for ( int i = 0; i < time; i++ )
        {
            Thread.sleep( 1000 / 2 );
            checkInterruption();
        }
    }

    private int getTime()
    {
        String t = getParameter( "time" );

        if ( StringUtils.isEmpty( t ) )
        {
            return 5;
        }
        else
        {
            return new Integer( t );
        }
    }

    @Override
    protected String getAction()
    {
        return "Sleeping";
    }

    @Override
    protected String getMessage()
    {
        return "Sleeping for " + getTime() + " seconds!";
    }

}
