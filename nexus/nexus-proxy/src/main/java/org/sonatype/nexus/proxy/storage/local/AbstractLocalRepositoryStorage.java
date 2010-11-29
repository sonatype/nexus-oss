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
package org.sonatype.nexus.proxy.storage.local;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.mime.MimeUtil;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreIteratorRequest;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.LinkPersister;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.wastebasket.Wastebasket;

/**
 * Abstract Storage class. It have ID and defines logger. Predefines all write methods to be able to "decorate"
 * StorageItems with attributes if supported.
 * 
 * @author cstamas
 */
public abstract class AbstractLocalRepositoryStorage
    implements LocalRepositoryStorage
{
    @Requirement
    private Logger logger;

    /**
     * The wastebasket.
     */
    @Requirement
    private Wastebasket wastebasket;

    /**
     * The default Link persister.
     */
    @Requirement
    private LinkPersister linkPersister;

    /**
     * The MIME util.
     */
    @Requirement
    private MimeUtil mimeUtil;

    protected Logger getLogger()
    {
        return logger;
    }

    protected Wastebasket getWastebasket()
    {
        return wastebasket;
    }
    
    protected LinkPersister getLinkPersister()
    {
        return linkPersister;
    }

    protected MimeUtil getMimeUtil()
    {
        return mimeUtil;
    }

    /**
     * Gets the absolute url from base.
     * 
     * @param uid the uid
     * @return the absolute url from base
     */
    @Deprecated
    public URL getAbsoluteUrlFromBase( Repository repository, ResourceStoreRequest request )
        throws LocalStorageException
    {
        StringBuffer urlStr = new StringBuffer( repository.getLocalUrl() );

        if ( request.getRequestPath().startsWith( RepositoryItemUid.PATH_SEPARATOR ) )
        {
            urlStr.append( request.getRequestPath() );
        }
        else
        {
            urlStr.append( RepositoryItemUid.PATH_SEPARATOR ).append( request.getRequestPath() );
        }
        try
        {
            return new URL( urlStr.toString() );
        }
        catch ( MalformedURLException e )
        {
            try
            {
                return new File( urlStr.toString() ).toURI().toURL();
            }
            catch ( MalformedURLException e1 )
            {
                throw new LocalStorageException( "The local storage has a malformed URL as baseUrl!", e );
            }
        }
    }

    public final void deleteItem( Repository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, UnsupportedStorageOperationException, LocalStorageException
    {
        getWastebasket().delete( this, repository, request );
    }

    // ==

    public Iterator<StorageItem> iterateItems( Repository repository, ResourceStoreIteratorRequest request )
        throws ItemNotFoundException, LocalStorageException
    {
        throw new UnsupportedOperationException( "Iteration not supported!" );
    }

}
