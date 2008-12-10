/**
 * Sonatype NexusTM [Open Source Version].
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

import java.io.IOException;
import java.util.Map;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.utils.StoreFileWalker;

public class RecreateAttributesWalker
    extends StoreFileWalker
{
    private Repository repository;

    private Map<String, String> initialData;

    public RecreateAttributesWalker( Repository repository, Logger logger, Map<String, String> initialData )
    {
        super( repository, logger );

        this.repository = repository;

        this.initialData = initialData;
    }

    @Override
    protected void processFileItem( StorageFileItem item )
    {
        try
        {
            if ( getInitialData() != null )
            {
                item.getAttributes().putAll( initialData );
            }

            getRepository().getLocalStorage().getAttributesHandler().storeAttributes(
                item,
                item.getInputStream() );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Could not recreate attributes for item " + item.getRepositoryItemUid().toString(), e );
        }
    }

    public Repository getRepository()
    {
        return repository;
    }

    public Map<String, String> getInitialData()
    {
        return initialData;
    }

}
