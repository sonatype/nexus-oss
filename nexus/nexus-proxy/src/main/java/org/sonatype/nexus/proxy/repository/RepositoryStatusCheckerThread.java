/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.proxy.repository;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;

public class RepositoryStatusCheckerThread
    extends Thread
{
    private final Logger logger;

    private final ProxyRepository repository;

    private boolean running;

    public RepositoryStatusCheckerThread( final Logger logger, final ProxyRepository repository )
    {
        super( "RepositoryStatusChecker-" + repository.getId() );

        this.logger = logger;

        this.repository = repository;

        this.running = true;
    }

    protected Logger getLogger()
    {
        return logger;
    }

    public ProxyRepository getRepository()
    {
        return repository;
    }

    public boolean isRunning()
    {
        return running;
    }

    public void setRunning( boolean val )
    {
        this.running = val;
    }

    public void run()
    {
        boolean interrupted = false;

        while ( isRunning() && getRepository().getProxyMode() != null )
        {
            // if interrupted from sleep, since autoBlock happened, do NOT try to unblock it immediately
            // it has to sleep the 1st amount of time repo says, and THEN try to unblock it
            if ( !interrupted )
            {
                LocalStatus repositoryLocalStatus = getRepository().getLocalStatus();

                // check only if repository is in service
                if ( repositoryLocalStatus.shouldServiceRequest() )
                {
                    // get status check mode
                    RepositoryStatusCheckMode repositoryStatusCheckMode =
                        getRepository().getRepositoryStatusCheckMode();

                    if ( RepositoryStatusCheckMode.ALWAYS.equals( repositoryStatusCheckMode ) )
                    {
                        // just do it, don't care for proxyMode
                        getRepository().getRemoteStatus( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT ), true );
                    }
                    else if ( RepositoryStatusCheckMode.AUTO_BLOCKED_ONLY.equals( repositoryStatusCheckMode ) )
                    {
                        // do it only if proxyMode , don't care for proxyMode
                        ProxyMode repositoryProxyMode = getRepository().getProxyMode();

                        if ( repositoryProxyMode.shouldAutoUnblock() )
                        {
                            getRepository().getRemoteStatus( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT ),
                                true );
                        }
                    }
                    else if ( RepositoryStatusCheckMode.NEVER.equals( repositoryStatusCheckMode ) )
                    {
                        // nothing
                    }
                }
            }

            try
            {
                long sleepTime = getRepository().getNextRemoteStatusRetainTime();

                // say this message only if repository is auto-blocked, regardless of repositoryStatusCheckMode
                if ( getRepository().getProxyMode().shouldAutoUnblock() )
                {
                    getLogger().info(
                        "Next attempt to auto-unblock the \"" + getRepository().getName() + "\" (id="
                            + getRepository().getId()
                            + ") repository by checking its remote peer health will occur in "
                            + DurationFormatUtils.formatDurationWords( sleepTime, true, true ) + "." );
                }

                Thread.sleep( sleepTime );

                interrupted = false;
            }
            catch ( InterruptedException e )
            {
                // just ignore it, isRunning() will take care.
                interrupted = true;
            }
        }
    }
}
