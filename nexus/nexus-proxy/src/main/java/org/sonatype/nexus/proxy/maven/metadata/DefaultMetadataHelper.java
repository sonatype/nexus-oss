/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StringContentLocator;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
import org.sonatype.nexus.proxy.wastebasket.DeleteOperation;

/**
 * Default MetadataHelper in Nexus, works based on a Repository.
 * 
 * @author Juven Xu
 */
public class DefaultMetadataHelper
    extends AbstractMetadataHelper
{
    private final MavenRepository repository;

    private final DeleteOperation operation;

    public DefaultMetadataHelper( Logger logger, MavenRepository repository )
    {
        this( logger, repository, DeleteOperation.MOVE_TO_TRASH );
    }

    public DefaultMetadataHelper( Logger logger, MavenRepository repository, DeleteOperation operation )
    {
        super( logger );

        this.repository = repository;
        this.operation = operation;
    }


    @Override
    public void store( String content, String path )
        throws IOException
    {
        ContentLocator contentLocator = new StringContentLocator( content );

        putStorageItem( path, contentLocator );
    }

    @Override
    public void remove( String path )
        throws IOException
    {
        deleteStorageItem( path );
    }

    @Override
    public boolean exists( String path )
        throws IOException
    {
        return repository.getLocalStorage().containsItem( repository, new ResourceStoreRequest( path, true ) );
    }

    @Override
    public InputStream retrieveContent( String path )
        throws IOException
    {
        StorageItem item = getStorageItem( path, false );

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
            if ( getStorageItem( path, true ).isVirtual() )
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
        throws IOException
    {
        return getStorageItem( path, true ).getRepositoryItemAttributes().get( DigestCalculatingInspector.DIGEST_MD5_KEY );
    }

    @Override
    public String buildSh1( String path )
        throws IOException
    {
        return getStorageItem( path, true ).getRepositoryItemAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY );
    }

    private AbstractStorageItem getStorageItem( final String path, final boolean localOnly )
        throws IOException
    {
        try
        {
            return repository.getLocalStorage().retrieveItem( repository, new ResourceStoreRequest( path, localOnly ) );
        }
        catch ( ItemNotFoundException e )
        {
            throw new IOException( "Item not found!", e );
        }
    }

    private void putStorageItem( final String path, final ContentLocator contentLocator )
        throws IOException
    {
        try
        {
            ResourceStoreRequest req = new ResourceStoreRequest( path );

            DefaultStorageFileItem mdFile = new DefaultStorageFileItem( repository, req, true, true, contentLocator );

            repository.storeItem( false, mdFile );

            // TODO: why? storeItem() already does this!!!
            repository.removeFromNotFoundCache( req );
        }
        catch ( Exception e )
        {
            throw new IOException( e );
        }
    }

    private void deleteStorageItem( final String path )
        throws IOException
    {
        try
        {
            ResourceStoreRequest request = new ResourceStoreRequest( path, true );
            request.getRequestContext().put( DeleteOperation.DELETE_OPERATION_CTX_KEY, operation );
            repository.deleteItem( false, request );
        }
        catch ( Exception e )
        {
            throw new IOException( e );
        }
    }

    @Override
    protected GavCalculator getGavCalculator()
    {
        return repository.getGavCalculator();
    }

}
