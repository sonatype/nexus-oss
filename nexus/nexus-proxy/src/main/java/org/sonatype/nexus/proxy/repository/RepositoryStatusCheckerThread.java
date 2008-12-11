/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.repository;

public class RepositoryStatusCheckerThread
    extends Thread
{
    private final Repository repository;

    public RepositoryStatusCheckerThread( Repository repository )
    {
        super();

        this.repository = repository;
    }

    public Repository getRepository()
    {
        return repository;
    }

    public void run()
    {
        try
        {
            while ( !isInterrupted() && getRepository().getProxyMode() != null )
            {
                if ( RepositoryStatusCheckMode.ALWAYS.equals( getRepository().getRepositoryStatusCheckMode() ) )
                {
                    if ( getRepository().getLocalStatus().shouldServiceRequest() )
                    {
                        getRepository().getRemoteStatus( false );
                    }
                }
                else if ( RepositoryStatusCheckMode.AUTO_BLOCKED_ONLY.equals( getRepository()
                    .getRepositoryStatusCheckMode() ) )
                {
                    if ( getRepository().getProxyMode().shouldAutoUnblock() )
                    {
                        getRepository().getRemoteStatus( false );
                    }
                }
                else if ( RepositoryStatusCheckMode.NEVER.equals( getRepository().getRepositoryStatusCheckMode() ) )
                {
                    // nothing
                }

                Thread.sleep( AbstractRepository.REMOTE_STATUS_RETAIN_TIME );
            }
        }
        catch ( InterruptedException e )
        {

        }
    }
}
