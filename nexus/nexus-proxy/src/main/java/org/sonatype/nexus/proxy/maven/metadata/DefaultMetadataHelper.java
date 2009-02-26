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
package org.sonatype.nexus.proxy.maven.metadata;

import java.io.InputStream;

import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * Default MetadataHelper in Nexus, works based on a Repository.
 * 
 * @author Juven Xu
 */
public class DefaultMetadataHelper
    extends AbstractMetadataHelper
{
    private Repository repository;

    public DefaultMetadataHelper( Repository repository )
    {
        this.repository = repository;
    }

    @Override
    public void store( String content, String path )
        throws Exception
    {
        // UIDs are like URIs! The separator is _always_ "/"!!!
        RepositoryItemUid mdUid = repository.createUid( path );

        ContentLocator contentLocator = new StringContentLocator( content );

        storeItem( mdUid, contentLocator );
    }

    @Override
    public void remove( String path )
        throws StorageException,
            UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException
    {
        repository.deleteItem( repository.createUid( path ), null );
    }

    @Override
    public boolean exists( String path )
        throws StorageException
    {
        return repository.getLocalStorage().containsItem( repository, null, path );
    }

    @Override
    public InputStream retrieveContent( String path )
        throws Exception
    {
        StorageItem item = repository.retrieveItem( repository.createUid( path ), null );

        if ( item instanceof StorageFileItem )
        {
            return ( (StorageFileItem) item ).getInputStream();
        }
        else
        {
            return null;
        }
    }

    @Override
    protected boolean shouldBuildChecksum( String path )
    {
        if ( !super.shouldBuildChecksum( path ) )
        {
            return false;
        }

        try
        {
            if ( getStorageItem( path ).isVirtual() )
            {
                return false;
            }
        }
        catch ( Exception e )
        {
            return false;
        }

        return true;
    }

    @Override
    public String buildMd5( String path )
        throws StorageException,
            ItemNotFoundException
    {
        return getStorageItem( path ).getAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY );
    }

    @Override
    public String buildSh1( String path )
        throws StorageException,
            ItemNotFoundException
    {
        return getStorageItem( path ).getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY );
    }

    private AbstractStorageItem getStorageItem( String path )
        throws StorageException,
            ItemNotFoundException
    {
        return repository.getLocalStorage().retrieveItem( repository, null, path );
    }

    private void storeItem( RepositoryItemUid uid, ContentLocator contentLocator )
        throws StorageException,
            UnsupportedStorageOperationException,
            IllegalOperationException
    {
        DefaultStorageFileItem mdFile = new DefaultStorageFileItem(
            repository,
            uid.getPath(),
            true,
            true,
            contentLocator );

        repository.storeItem( mdFile );

        repository.removeFromNotFoundCache( uid.getPath() );
    }

}
