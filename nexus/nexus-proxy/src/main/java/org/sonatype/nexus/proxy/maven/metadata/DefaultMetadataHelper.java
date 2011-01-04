/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.maven.metadata;

import java.io.InputStream;

import org.apache.maven.index.artifact.GavCalculator;
import org.codehaus.plexus.logging.Logger;
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

        repository.removeFromNotFoundCache( req );
    }

    @Override
    protected GavCalculator getGavCalculator()
    {
        return repository.getGavCalculator();
    }

}
