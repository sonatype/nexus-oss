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
package org.sonatype.nexus.proxy.router;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.sonatype.nexus.configuration.ApplicationConfiguration;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;

public class DummyRepositoryRouter
    implements RepositoryRouter
{

    public String getId()
    {
        return "dummyRouter";
    }

    public Collection<StorageItem> list( ResourceStoreRequest request )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        if ( request.getRequestPath().endsWith( "coll" ) )
        {
            List<StorageItem> result = new ArrayList<StorageItem>( 3 );
            result.add( new DefaultStorageCollectionItem( this, FilenameUtils.separatorsToUnix( FilenameUtils.concat(
                request.getRequestPath(),
                "virtual" ) ), true, true ) );
            result.add( new DefaultStorageCollectionItem( this, FilenameUtils.separatorsToUnix( FilenameUtils.concat(
                request.getRequestPath(),
                "non-virtual" ) ), true, true ) );
            return result;
        }
        else
        {
            throw new IllegalArgumentException( "Not a coll!" );
        }
    }

    // ============================================
    // Unsupported dummy stuff

    public void copyItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws IllegalArgumentException,
            NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void deleteItem( ResourceStoreRequest request )
        throws IllegalArgumentException,
            NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public boolean isFollowLinks()
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void moveItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws IllegalArgumentException,
            NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public StorageItem retrieveItem( ResourceStoreRequest request )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void setFollowLinks( boolean followLinks )
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void setId( String id )
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void storeItem( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws IllegalArgumentException,
            NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void purgeNotFoundCache()
    {
        // TODO Auto-generated method stub

    }

    public int getNotFoundCacheTTL()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public void setNotFoundCacheTTL( int notFoundCacheTTLSeconds )
    {
        // TODO Auto-generated method stub

    }

    public void createCollection( ResourceStoreRequest request, Map<String, String> userAttributes )
        throws IllegalArgumentException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        // TODO Auto-generated method stub

    }

    public void configure( ApplicationConfiguration configuration )
    {
        // TODO Auto-generated method stub

    }

    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        // TODO Auto-generated method stub

    }

    public Repository getRouterStorage()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setRouterStorage( Repository repository )
    {
        // TODO Auto-generated method stub
        
    }

    public ContentClass getHandledContentClass()
    {
        return new DefaultContentClass();
    }

}
