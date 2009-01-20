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
package org.sonatype.nexus.proxy.item;

import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

/**
 * The Class DefaultStorageLinkItem.
 */
public class DefaultStorageLinkItem
    extends AbstractStorageItem
    implements StorageLinkItem
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 4494595788515460394L;

    /** The target. */
    private transient RepositoryItemUid targetUid;

    /**
     * Instantiates a new default storage link item.
     * 
     * @param repository the repository
     * @param path the path
     * @param canRead the can read
     * @param canWrite the can write
     * @param targetUid the target uid
     */
    public DefaultStorageLinkItem( Repository repository, String path, boolean canRead, boolean canWrite,
        RepositoryItemUid targetUid )
    {
        super( repository, path, canRead, canWrite );

        setTarget( targetUid );
    }

    /**
     * Instantiates a new default storage link item.
     * 
     * @param router the router
     * @param path the path
     * @param canRead the can read
     * @param canWrite the can write
     * @param targetUid the target uid
     */
    public DefaultStorageLinkItem( RepositoryRouter router, String path, boolean canRead, boolean canWrite,
        RepositoryItemUid targetUid )
    {
        super( router, path, canRead, canWrite );

        setTarget( targetUid );
    }

    public RepositoryItemUid getTarget()
    {
        return targetUid;
    }

    public void setTarget( RepositoryItemUid target )
    {
        this.targetUid = target;
    }

    public void overlay( StorageItem item )
        throws IllegalArgumentException
    {
        super.overlay( item );

        StorageLinkItem otherLink = (StorageLinkItem) item;

        if ( otherLink.getTarget() != null )
        {
            setTarget( otherLink.getTarget() );
        }
    }

}
