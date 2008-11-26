package org.sonatype.nexus.proxy.dummy;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.RepositoryNotListableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.cache.PathCache;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryStatusCheckMode;
import org.sonatype.nexus.proxy.repository.RepositoryType;
import org.sonatype.nexus.proxy.repository.RequestProcessor;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.target.TargetSet;
import org.sonatype.nexus.scheduling.RepositoryTaskFilter;

public class DummyRepository
    implements Repository
{

    public void addToNotFoundCache( String path )
    {
        // TODO Auto-generated method stub

    }

    public void clearCaches( String path )
    {
        // TODO Auto-generated method stub

    }

    public void copyItem( RepositoryItemUid from, RepositoryItemUid to, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        // TODO Auto-generated method stub

    }

    public RepositoryItemUid createUid( String path )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void deleteItem( RepositoryItemUid uid, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        // TODO Auto-generated method stub

    }

    public Collection<String> evictUnusedItems( long timestamp )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public AccessManager getAccessManager()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public int getItemMaxAge()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public LocalStatus getLocalStatus()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public LocalRepositoryStorage getLocalStorage()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getLocalUrl()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getName()
    {
        // TODO Auto-generated method stub
        return null;
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

    public ProxyMode getProxyMode()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoteStatus getRemoteStatus( boolean forceCheck )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoteRepositoryStorage getRemoteStorage()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public RemoteStorageContext getRemoteStorageContext()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getRemoteUrl()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ContentClass getRepositoryContentClass()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public RepositoryStatusCheckMode getRepositoryStatusCheckMode()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public RepositoryType getRepositoryType()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Action getResultingActionOnWrite( ResourceStoreRequest rsr )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public TargetSet getTargetsForRequest( RepositoryItemUid uid, Map<String, Object> context )
    {
        // TODO Auto-generated method stub
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

    public boolean isIndexable()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isUserManaged()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public Collection<StorageItem> list( RepositoryItemUid uid, Map<String, Object> context )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<StorageItem> list( StorageCollectionItem item )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void moveItem( RepositoryItemUid from, RepositoryItemUid to, Map<String, Object> context )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        // TODO Auto-generated method stub

    }

    public boolean recreateAttributes( String fromPath, Map<String, String> initialData )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean recreateMavenMetadata( String fromPath )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void removeFromNotFoundCache( String path )
    {
        // TODO Auto-generated method stub

    }

    public StorageItem retrieveItem( boolean localOnly, RepositoryItemUid uid, Map<String, Object> context )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public InputStream retrieveItemContent( RepositoryItemUid uid )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setAccessManager( AccessManager accessManager )
    {
        // TODO Auto-generated method stub

    }

    public void setAllowWrite( boolean val )
    {
        // TODO Auto-generated method stub

    }

    public void setBrowseable( boolean val )
    {
        // TODO Auto-generated method stub

    }

    public void setId( String id )
    {
        // TODO Auto-generated method stub

    }

    public void setIndexable( boolean val )
    {
        // TODO Auto-generated method stub

    }

    public void setItemMaxAge( int itemMaxAge )
    {
        // TODO Auto-generated method stub

    }

    public void setLocalStatus( LocalStatus val )
    {
        // TODO Auto-generated method stub

    }

    public void setLocalStorage( LocalRepositoryStorage storage )
    {
        // TODO Auto-generated method stub

    }

    public void setLocalUrl( String url )
    {
        // TODO Auto-generated method stub

    }

    public void setName( String name )
    {
        // TODO Auto-generated method stub

    }

    public void setNotFoundCache( PathCache notFoundcache )
    {
        // TODO Auto-generated method stub

    }

    public void setNotFoundCacheTimeToLive( int notFoundCacheTimeToLive )
    {
        // TODO Auto-generated method stub

    }

    public void setProxyMode( ProxyMode val )
    {
        // TODO Auto-generated method stub

    }

    public void setRemoteStorage( RemoteRepositoryStorage storage )
    {
        // TODO Auto-generated method stub

    }

    public void setRemoteStorageContext( RemoteStorageContext ctx )
    {
        // TODO Auto-generated method stub

    }

    public void setRemoteUrl( String url )
    {
        // TODO Auto-generated method stub

    }

    public void setRepositoryStatusCheckMode( RepositoryStatusCheckMode mode )
    {
        // TODO Auto-generated method stub

    }

    public void setUserManaged( boolean val )
    {
        // TODO Auto-generated method stub

    }

    public void storeItem( StorageItem item )
        throws UnsupportedStorageOperationException,
            RepositoryNotAvailableException,
            StorageException
    {
        // TODO Auto-generated method stub

    }

    public void addProximityEventListener( EventListener listener )
    {
        // TODO Auto-generated method stub

    }

    public void notifyProximityEventListeners( AbstractEvent evt )
    {
        // TODO Auto-generated method stub

    }

    public void removeProximityEventListener( EventListener listener )
    {
        // TODO Auto-generated method stub

    }

    public void copyItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        // TODO Auto-generated method stub

    }

    public void createCollection( ResourceStoreRequest request, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        // TODO Auto-generated method stub

    }

    public void deleteItem( ResourceStoreRequest request )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        // TODO Auto-generated method stub

    }

    public String getId()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public TargetSet getTargetsForRequest( ResourceStoreRequest request )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<StorageItem> list( ResourceStoreRequest request )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            RepositoryNotListableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void moveItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        // TODO Auto-generated method stub

    }

    public StorageItem retrieveItem( ResourceStoreRequest request )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void storeItem( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        // TODO Auto-generated method stub

    }

    public void onProximityEvent( AbstractEvent evt )
    {
        // TODO Auto-generated method stub

    }

    public boolean isExposed()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void setExposed( boolean val )
    {
        // TODO Auto-generated method stub

    }

    public List<RequestProcessor> getRequestProcessors()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public RepositoryTaskFilter getRepositoryTaskFilter()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public boolean isCompatible( Repository repository )
    {
        // TODO Auto-generated method stub
        return false;
    }

}
