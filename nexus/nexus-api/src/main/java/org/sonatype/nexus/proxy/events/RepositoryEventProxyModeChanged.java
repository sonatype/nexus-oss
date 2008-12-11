/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.events;

import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The event fired when a repository's proxy mode changed.
 */
public class RepositoryEventProxyModeChanged
    extends RepositoryEvent
{
    private final ProxyMode oldProxyMode;

    private final ProxyMode newProxyMode;

    private final Throwable cause;

    public RepositoryEventProxyModeChanged( final Repository repository, final ProxyMode oldProxyMode,
        final ProxyMode newProxyMode, final Throwable cause )
    {
        super( repository );

        this.oldProxyMode = oldProxyMode;

        this.newProxyMode = newProxyMode;

        this.cause = cause;
    }

    public ProxyMode getOldProxyMode()
    {
        return oldProxyMode;
    }

    public ProxyMode getNewProxyMode()
    {
        return newProxyMode;
    }

    public Throwable getCause()
    {
        return cause;
    }
}
