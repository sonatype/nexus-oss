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
package org.sonatype.nexus.proxy.item;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.AccessManager;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.attributes.AttributesHandler;
import org.sonatype.nexus.proxy.cache.PathCache;
import org.sonatype.nexus.proxy.item.uid.RepositoryItemUidAttributeManager;
import org.sonatype.nexus.proxy.mirror.PublishedMirrors;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.repository.RequestProcessor;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.local.LocalStorageContext;
import org.sonatype.nexus.proxy.target.TargetSet;
import org.sonatype.nexus.scheduling.RepositoryTaskFilter;

public class DummyRepository
    implements Repository
{
    private final String id;

    public DummyRepository( String id )
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public StorageItem retrieveItem( ResourceStoreRequest request )
        throws ItemNotFoundException, IllegalOperationException, StorageException, AccessDeniedException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void copyItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException,
        StorageException, AccessDeniedException
    {
        // TODO Auto-generated method stub

    }

    public void moveItem( ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException,
        StorageException, AccessDeniedException
    {
        // TODO Auto-generated method stub

    }

    public void deleteItem( ResourceStoreRequest request )
        throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException,
        StorageException, AccessDeniedException
    {
        // TODO Auto-generated method stub

    }

    public void storeItem( ResourceStoreRequest request, InputStream is, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException,
        StorageException, AccessDeniedException
    {
        // TODO Auto-generated method stub

    }

    public void createCollection( ResourceStoreRequest request, Map<String, String> userAttributes )
        throws UnsupportedStorageOperationException, ItemNotFoundException, IllegalOperationException,
        StorageException, AccessDeniedException
    {
        // TODO Auto-generated method stub

    }

    public Collection<StorageItem> list( ResourceStoreRequest request )
        throws ItemNotFoundException, IllegalOperationException, StorageException, AccessDeniedException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public CoreConfiguration getCurrentCoreConfiguration()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void configure( Object config )
        throws ConfigurationException
    {
        // TODO Auto-generated method stub

    }

    public boolean isDirty()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean commitChanges()
        throws ConfigurationException
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean rollbackChanges()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public String getProviderRole()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getProviderHint()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setId( String id )
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

    public String getPathPrefix()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setPathPrefix( String prefix )
    {
        // TODO Auto-generated method stub

    }

    public RepositoryKind getRepositoryKind()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ContentClass getRepositoryContentClass()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public RepositoryTaskFilter getRepositoryTaskFilter()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public TargetSet getTargetsForRequest( ResourceStoreRequest request )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean hasAnyTargetsForRequest( ResourceStoreRequest request )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public RepositoryItemUid createUid( String path )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Action getResultingActionOnWrite( ResourceStoreRequest rsr )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isCompatible( Repository repository )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public <F> F adaptToFacet( Class<F> t )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public int getNotFoundCacheTimeToLive()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public void setNotFoundCacheTimeToLive( int notFoundCacheTimeToLive )
    {
        // TODO Auto-generated method stub

    }

    public PathCache getNotFoundCache()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setNotFoundCache( PathCache notFoundcache )
    {
        // TODO Auto-generated method stub

    }

    public void maintainNotFoundCache( ResourceStoreRequest request )
        throws ItemNotFoundException
    {
        // TODO Auto-generated method stub

    }

    public void addToNotFoundCache( String path )
    {
        // TODO Auto-generated method stub

    }

    public void removeFromNotFoundCache( String path )
    {
        // TODO Auto-generated method stub

    }

    public void addToNotFoundCache( ResourceStoreRequest request )
    {
        // TODO Auto-generated method stub

    }

    public void removeFromNotFoundCache( ResourceStoreRequest request )
    {
        // TODO Auto-generated method stub

    }

    public boolean isNotFoundCacheActive()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void setNotFoundCacheActive( boolean notFoundCacheActive )
    {
        // TODO Auto-generated method stub

    }

    public String getLocalUrl()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setLocalUrl( String url )
        throws StorageException
    {
        // TODO Auto-generated method stub

    }

    public LocalStatus getLocalStatus()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setLocalStatus( LocalStatus val )
    {
        // TODO Auto-generated method stub

    }

    public LocalRepositoryStorage getLocalStorage()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setLocalStorage( LocalRepositoryStorage storage )
    {
        // TODO Auto-generated method stub

    }

    public PublishedMirrors getPublishedMirrors()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, RequestProcessor> getRequestProcessors()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isUserManaged()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void setUserManaged( boolean val )
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

    public boolean isBrowseable()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void setBrowseable( boolean val )
    {
        // TODO Auto-generated method stub

    }

    public RepositoryWritePolicy getWritePolicy()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setWritePolicy( RepositoryWritePolicy writePolicy )
    {
        // TODO Auto-generated method stub

    }

    public boolean isIndexable()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void setIndexable( boolean val )
    {
        // TODO Auto-generated method stub

    }

    public boolean isSearchable()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void setSearchable( boolean val )
    {
        // TODO Auto-generated method stub

    }

    public void expireCaches( ResourceStoreRequest request )
    {
        // TODO Auto-generated method stub

    }

    public void expireNotFoundCaches( ResourceStoreRequest request )
    {
        // TODO Auto-generated method stub

    }

    public Collection<String> evictUnusedItems( ResourceStoreRequest request, long timestamp )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean recreateAttributes( ResourceStoreRequest request, Map<String, String> initialData )
    {
        // TODO Auto-generated method stub
        return false;
    }

    public AccessManager getAccessManager()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setAccessManager( AccessManager accessManager )
    {
        // TODO Auto-generated method stub

    }

    public StorageItem retrieveItem( boolean fromTask, ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void copyItem( boolean fromTask, ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
    {
        // TODO Auto-generated method stub

    }

    public void moveItem( boolean fromTask, ResourceStoreRequest from, ResourceStoreRequest to )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
    {
        // TODO Auto-generated method stub

    }

    public void deleteItem( boolean fromTask, ResourceStoreRequest request )
        throws UnsupportedStorageOperationException, IllegalOperationException, ItemNotFoundException, StorageException
    {
        // TODO Auto-generated method stub

    }

    public Collection<StorageItem> list( boolean fromTask, ResourceStoreRequest request )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void storeItem( boolean fromTask, StorageItem item )
        throws UnsupportedStorageOperationException, IllegalOperationException, StorageException
    {
        // TODO Auto-generated method stub

    }

    public Collection<StorageItem> list( boolean fromTask, StorageCollectionItem item )
        throws IllegalOperationException, ItemNotFoundException, StorageException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public AttributesHandler getAttributesHandler()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setAttributesHandler( AttributesHandler attributesHandler )
    {
        // TODO Auto-generated method stub
        
    }

    public RepositoryItemUidAttributeManager getRepositoryItemUidAttributeManager()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public LocalStorageContext getLocalStorageContext()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
