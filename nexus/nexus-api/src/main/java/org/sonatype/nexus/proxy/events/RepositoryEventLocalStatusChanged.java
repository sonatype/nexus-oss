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

import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The event fired when repository's local status is changed.
 * 
 * @author cstamas
 */
public class RepositoryEventLocalStatusChanged
    extends RepositoryEvent
{
    private final LocalStatus oldLocalStatus;

    private final LocalStatus newLocalStatus;

    /**
     * Instantiates a new repository event evict unused items.
     * 
     * @param repository the repository
     */
    public RepositoryEventLocalStatusChanged( final Repository repository, final LocalStatus oldLocalStatus,
        final LocalStatus newLocalStatus )
    {
        super( repository );

        this.oldLocalStatus = oldLocalStatus;

        this.newLocalStatus = newLocalStatus;
    }

    public LocalStatus getOldLocalStatus()
    {
        return oldLocalStatus;
    }

    public LocalStatus getNewLocalStatus()
    {
        return newLocalStatus;
    }
}
