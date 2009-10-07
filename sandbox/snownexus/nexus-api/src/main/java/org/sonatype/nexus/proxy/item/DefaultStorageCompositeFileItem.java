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

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

/**
 * The Class DefaultStorageCompositeFileItem.
 */
public class DefaultStorageCompositeFileItem
    extends DefaultStorageFileItem
    implements StorageCompositeFileItem
{
    private List<StorageItem> sources;

    public DefaultStorageCompositeFileItem( Repository repository, ResourceStoreRequest request, boolean canRead,
                                            boolean canWrite, ContentLocator contentLocator, List<StorageItem> sources )
    {
        super( repository, request, canRead, canWrite, contentLocator );

        if ( sources != null )
        {
            getSources().addAll( sources );
        }
    }

    public DefaultStorageCompositeFileItem( RepositoryRouter router, ResourceStoreRequest request, boolean canRead,
                                            boolean canWrite, ContentLocator contentLocator, List<StorageItem> sources )
    {
        super( router, request, canRead, canWrite, contentLocator );

        if ( sources != null )
        {
            getSources().addAll( sources );
        }
    }

    public List<StorageItem> getSources()
    {
        if ( sources == null )
        {
            sources = new ArrayList<StorageItem>();
        }

        return sources;
    }
}
