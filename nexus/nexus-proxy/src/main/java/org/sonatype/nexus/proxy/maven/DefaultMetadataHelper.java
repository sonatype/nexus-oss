/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */

package org.sonatype.nexus.proxy.maven;

import java.io.InputStream;

import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
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
    public void remove( String path ) throws StorageException, UnsupportedStorageOperationException, RepositoryNotAvailableException, ItemNotFoundException
    {
        repository.deleteItem( repository.createUid( path ), null );
    }
    
    @Override
    public boolean exists( String path )
        throws StorageException
    {
        return repository.getLocalStorage().containsItem( repository.createUid( path ) );
    }

    @Override
    public InputStream retrieveContent( String path )
        throws Exception
    {
        RepositoryItemUid uid = repository.createUid( path );

        return repository.retrieveItemContent( uid );
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
        return repository.getLocalStorage().retrieveItem( repository.createUid( path ) );
    }

    private void storeItem( RepositoryItemUid uid, ContentLocator contentLocator )
        throws StorageException,
            UnsupportedStorageOperationException,
            RepositoryNotAvailableException
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
