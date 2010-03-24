/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;

public class RepositoryStatusCheckerThread
    extends Thread
{
    private final ProxyRepository repository;

    public RepositoryStatusCheckerThread( ProxyRepository repository )
    {
        super();

        this.repository = repository;
    }

    public ProxyRepository getRepository()
    {
        return repository;
    }

    public void run()
    {
        try
        {
            while ( !isInterrupted() && getRepository().getProxyMode() != null )
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
                        getRepository()
                            .getRemoteStatus( new ResourceStoreRequest( RepositoryItemUid.PATH_ROOT ), true );
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
                
                // sleep the time that repository says (repository is driving how much should be sleep)
                Thread.sleep( getRepository().getRepositoryStatusCheckPeriod() );
            }
        }
        catch ( InterruptedException e )
        {
        }
    }
}
