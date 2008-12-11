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
package org.sonatype.nexus.proxy.eclipse;

import java.util.List;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.router.DefaultGroupIdBasedRepositoryRouter;

/**
 * A router for Eclipse Update Sites.
 * 
 * @author cstamas
 * OFF plexus.component role-hint="groups-eclipse-update-site"
 */
public class EclipseUpdateSiteGroupIdBasedRepositoryRouter
    extends DefaultGroupIdBasedRepositoryRouter
{
    private ContentClass contentClass = new EclipseUpdateSiteContentClass();

    public ContentClass getHandledContentClass()
    {
        return contentClass;
    }

    protected boolean isSiteXml( String path )
    {
        return path.endsWith( "site.xml" );
    }

    protected boolean shouldStopItemSearchOnFirstFoundFile( StorageItem item )
    {
        if ( isSiteXml( item.getPath() ) )
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "The item " + item.getPath() + " is site.xml. Continuing." );
            }
            return false;
        }
        else
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "The item " + item.getPath() + " is not site.xml. Stopping." );
            }
            return super.shouldStopItemSearchOnFirstFoundFile( item );
        }
    }

    protected StorageItem retrieveItemPostprocessor( ResourceStoreRequest request, List<StorageItem> listOfStorageItems )
        throws StorageException
    {
        if ( !isSiteXml( request.getRequestPath() ) || listOfStorageItems.size() == 1 )
        {
            // there is no need for Metadata aggregation, the result list contains only one item
            // or it is not metadata
            return super.retrieveItemPostprocessor( request, listOfStorageItems );
        }
        else
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Applying 'eclipse' postprocessing for site.xml." );
            }

            // the listOfStorageItemss are actually a list of FileItems containing site.xml-s from various
            // reposes.. simply merge them, nothing else (see EclipseUpdateSiteRepository!)!
            return super.retrieveItemPostprocessor( request, listOfStorageItems );
        }

    }
}
