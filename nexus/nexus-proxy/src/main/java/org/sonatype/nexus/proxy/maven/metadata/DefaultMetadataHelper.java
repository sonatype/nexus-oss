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

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;

/**
 * Default MetadataHelper in Nexus, works based on a Repository.
 * 
 * @author Juven Xu
 */
public class DefaultMetadataHelper
    extends AbstractMetadataHelper
{
    private MavenRepository repository;

    public DefaultMetadataHelper( Logger logger, MavenRepository repository )
    {
        super( logger );

        this.repository = repository;
    }

    @Override
    public void store( String content, String path )
        throws Exception
    {
        ContentLocator contentLocator = new StringContentLocator( content );

        storeItem( path, contentLocator );
    }

    @Override
    public void remove( String path )
        throws StorageException,
            UnsupportedStorageOperationException,
            IllegalOperationException,
            ItemNotFoundException
    {
        repository.deleteItem( false, new ResourceStoreRequest( path, true ) );
    }

    @Override
    public boolean exists( String path )
        throws StorageException
    {
        return repository.getLocalStorage().containsItem( repository, new ResourceStoreRequest( path, true ) );
    }

    @Override
    public InputStream retrieveContent( String path )
        throws Exception
    {
        StorageItem item = repository.retrieveItem( false, new ResourceStoreRequest( path, false ) );

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
        return repository.getLocalStorage().retrieveItem( repository, new ResourceStoreRequest( path, true ) );
    }

    private void storeItem( String path, ContentLocator contentLocator )
        throws StorageException,
            UnsupportedStorageOperationException,
            IllegalOperationException
    {
        ResourceStoreRequest req = new ResourceStoreRequest( path );

        DefaultStorageFileItem mdFile = new DefaultStorageFileItem( repository, req, true, true, contentLocator );

        repository.storeItem( false, mdFile );

        repository.removeFromNotFoundCache( path );
    }

    @Override
    protected GavCalculator getGavCalculator()
    {
        return repository.getGavCalculator();
    }

}
