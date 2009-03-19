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

import java.util.Collection;
import java.util.List;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * <p>
 * A common base for Proximity "web site" repository. It simply serves up static stuff, and detects "index.html" within
 * the collections.
 * 
 * @author cstamas
 */
public abstract class AbstractWebSiteRepository
    extends AbstractRepository
    implements WebSiteRepository
{
    protected AbstractWebSiteRepositoryConfiguration getExternalConfiguration()
    {
        return (AbstractWebSiteRepositoryConfiguration) super.getExternalConfiguration();
    }

    public List<String> getWelcomeFiles()
    {
        return getExternalConfiguration().getWelcomeFiles();
    }

    public void setWelcomeFiles( List<String> vals )
    {
        getExternalConfiguration().setWelcomeFiles( vals );
    }

    @Override
    protected StorageItem doRetrieveItem( ResourceStoreRequest request )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException
    {
        StorageItem result = super.doRetrieveItem( request );

        List<String> wf = getWelcomeFiles();

        if ( result instanceof StorageCollectionItem && wf.size() > 0 )
        {
            // it is a collection, check for one of the "welcome" files
            Collection<StorageItem> collItems = list( false, (StorageCollectionItem) result );

            for ( StorageItem item : collItems )
            {
                if ( item instanceof StorageFileItem && wf.contains( item.getName() ) )
                {
                    // it is a file, it's name is in welcomeFiles list, so return it instead parent collection
                    return item;
                }
            }
        }

        return result;
    }

}
