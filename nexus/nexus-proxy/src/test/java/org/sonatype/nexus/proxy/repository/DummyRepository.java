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
package org.sonatype.nexus.proxy.repository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.cache.PathCache;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.DefaultStorageLinkItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.router.DefaultContentClass;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public class DummyRepository
    implements Repository
{

    private String id = "dummy";

    private String groupId;

    public DummyRepository()
    {
        super();
    }

    public DummyRepository( String id )
    {
        this();
        this.id = id;
    }

    public String getBaseUrl()
    {
        return "http://dummy.com/something";
    }

    public String getId()
    {
        return id;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public int getRank()
    {
        return 50;
    }

    public boolean isAvailable()
    {
        return true;
    }

    public boolean isIndexable()
    {
        return true;
    }

    public boolean isListable()
    {
        return true;
    }

    public boolean isOffline()
    {
        return false;
    }

    public boolean isReadOnly()
    {
        return false;
    }

    public Collection<StorageItem> list( RepositoryItemUid uid )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        if ( uid.getPath().endsWith( "coll" ) )
        {
            List<StorageItem> result = new ArrayList<StorageItem>( 3 );
            result.add( new DefaultStorageFileItem( this, FilenameUtils.separatorsToUnix( FilenameUtils.concat( uid
                .getPath(), "a.txt" ) ), true, true ) );
            result.add( new DefaultStorageFileItem( this, FilenameUtils.separatorsToUnix( FilenameUtils.concat( uid
                .getPath(), "b.txt" ) ), true, true ) );
            result.add( new DefaultStorageFileItem( this, FilenameUtils.separatorsToUnix( FilenameUtils.concat( uid
                .getPath(), "c.txt" ) ), true, true ) );
            return result;
        }
        else
        {
            throw new IllegalArgumentException( "Not a coll!" );
        }
    }

    public StorageItem retrieveItem( boolean localOnly, RepositoryItemUid uid )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        if ( uid.getPath().endsWith( "coll" ) )
        {
            DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( this, "", true, true );
            return coll;
        }
        else if ( uid.getPath().endsWith( "file" ) )
        {
            DefaultStorageFileItem file = new DefaultStorageFileItem( this, "", true, true );
            return file;
        }
        else if ( uid.getPath().endsWith( "link" ) )
        {
            DefaultStorageLinkItem link = new DefaultStorageLinkItem( this, "", true, true, new RepositoryItemUid(
                this,
                "/a/link" ).toString() );
            return link;
        }
        else
        {
            throw new ItemNotFoundException( uid );
        }
    }

    public StorageItem lookupItem( RepositoryItemUid uid )
        throws RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        if ( uid.getPath().endsWith( "coll" ) )
        {
            DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( this, "", true, true );
            return coll;
        }
        else if ( uid.getPath().endsWith( "file" ) )
        {
            DefaultStorageFileItem file = new DefaultStorageFileItem( this, "", true, true );
            return file;
        }
        else if ( uid.getPath().endsWith( "link" ) )
        {
            DefaultStorageLinkItem link = new DefaultStorageLinkItem( this, "", true, true, new RepositoryItemUid(
                this,
                "/a/link" ).toString() );
            return link;
        }
        else
        {
            return null;
        }
    }

    public InputStream retrieveItemContent( RepositoryItemUid uid )
        throws UnsupportedOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        return new ByteArrayInputStream( uid.getPath().getBytes() );
    }

    // ===============================================================
    // Dummy, unsupported stuff

    public Collection<StorageItem> list( ResourceStoreRequest request )
        throws NoSuchRepositoryException,
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

    public void addProximityEventListener( EventListener listener )
    {
    }

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

    public void deleteItem( RepositoryItemUid uid )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
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

    public AccessManager getAccessManager()
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public LocalRepositoryStorage getLocalStorage()
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public RemoteRepositoryStorage getRemoteStorage()
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

    public void notifyProximityEventListeners( AbstractEvent evt )
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public boolean recreateAttributes( Map<String, String> initialData )
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void removeProximityEventListener( EventListener listener )
    {
    }

    public void setAccessManager( AccessManager accessManager )
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void setAvailable( boolean val )
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public void setIndexable( boolean val )
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void setListable( boolean val )
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void setLocalStorage( LocalRepositoryStorage storage )
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void setOffline( boolean val )
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void setRank( int rank )
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void setReadOnly( boolean val )
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void setRemoteStorage( RemoteRepositoryStorage storage )
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void storeItem( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            StorageException
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

    public void setBaseUrl( String url )
    {
        throw new UnsupportedOperationException( "This is dummy!" );
    }

    public void purgeNotFoundCache()
    {
        // TODO Auto-generated method stub

    }

    public boolean isRemoteStorageReachable()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public String getLocalUrl()
    {
        return "file://somewhere/on/the/disk/";
    }

    public String getRemoteUrl()
    {
        return "http://dummy.com/something";
    }

    public void setLocalUrl( String url )
    {
        // TODO Auto-generated method stub

    }

    public void setRemoteUrl( String url )
    {
        // TODO Auto-generated method stub

    }

    public RemoteStorageContext getRemoteStorageContext()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setRemoteStorageContext( RemoteStorageContext ctx )
    {
        // TODO Auto-generated method stub

    }

    public String getName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setName( String name )
    {
        // TODO Auto-generated method stub

    }

    public void clearCaches( String path )
    {
        // TODO Auto-generated method stub

    }

    public PathCache getNotFoundCache()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public int getNotFoundCacheTimeToLive()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public void setNotFoundCache( PathCache notFoundcache )
    {
        // TODO Auto-generated method stub

    }

    public void setNotFoundCacheTimeToLive( int notFoundCacheTimeToLive )
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

    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        // TODO Auto-generated method stub

    }

    public LocalStatus getLocalStatus()
    {
        return LocalStatus.IN_SERVICE;
    }

    public ProxyMode getProxyMode()
    {
        return null;
    }

    public RemoteStatus getRemoteStatus( boolean forceCheck )
    {
        return null;
    }

    public boolean isAllowWrite()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isBrowseable()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void setAllowWrite( boolean val )
    {
        // TODO Auto-generated method stub

    }

    public void setBrowseable( boolean val )
    {
        // TODO Auto-generated method stub

    }

    public void setLocalStatus( LocalStatus val )
    {
        // TODO Auto-generated method stub

    }

    public void setProxyMode( ProxyMode val )
    {
        // TODO Auto-generated method stub

    }

    public RepositoryType getRepositoryType()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setRepositoryType( RepositoryType repositoryType )
    {
        // TODO Auto-generated method stub

    }

    public ContentClass getRepositoryContentClass()
    {
        // TODO Auto-generated method stub
        return new DefaultContentClass();
    }

    public void addToNotFoundCache( String path )
    {
        // TODO Auto-generated method stub

    }

    public void removeFromNotFoundCache( String path )
    {
        // TODO Auto-generated method stub

    }

    public void copyItem( RepositoryItemUid from, RepositoryItemUid to )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        // TODO Auto-generated method stub
        
    }

    public void moveItem( RepositoryItemUid from, RepositoryItemUid to )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        // TODO Auto-generated method stub
        
    }

    public int getItemMaxAge()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public void setItemMaxAge( int itemMaxAge )
    {
        // TODO Auto-generated method stub
        
    }

}
