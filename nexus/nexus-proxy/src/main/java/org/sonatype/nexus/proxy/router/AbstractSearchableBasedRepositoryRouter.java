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
import java.util.List;
import java.util.Map;

import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.StorageLinkItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;

/**
 * The Class AbstractSearchableBasedRepositoryRouter is the base class for all routers that needs Searchable to produce
 * a view and handles real (non-virtual) and non-real (virtual) items. Repositories based on this are all read-only.
 * 
 * @author cstamas
 */
public abstract class AbstractSearchableBasedRepositoryRouter
    extends AbstractRepositoryRouter
{

    /**
     * The searchable.
     * 
     * @plexus.requirement
     */
    private NexusIndexer indexer;

    /**
     * The repository registry.
     * 
     * @plexus.requirement
     */
    private RepositoryRegistry repositoryRegistry;

    /**
     * Gets the searchable.
     * 
     * @return the searchable
     */
    public NexusIndexer getIndexer()
    {
        return indexer;
    }

    /**
     * Sets the searchable.
     * 
     * @param searchable the new searchable
     */
    public void setIndexer( NexusIndexer indexer )
    {
        this.indexer = indexer;
    }

    /**
     * Gets the repository registry.
     * 
     * @return the repository registry
     */
    public RepositoryRegistry getRepositoryRegistry()
    {
        return repositoryRegistry;
    }

    /**
     * Sets the repository registry.
     * 
     * @param repositoryRegistry the new repository registry
     */
    public void setRepositoryRegistry( RepositoryRegistry repositoryRegistry )
    {
        this.repositoryRegistry = repositoryRegistry;
    }

    /**
     * A better alternative, since we now have RepositoryRegistry, it allows us to implement more performant way to
     * retrieve non-virtual items.
     * 
     * @param link the link
     * @return the storage item
     * @throws NoSuchRepositoryException the no such repository exception
     * @throws NoSuchRepositoryGroupException the no such repository group exception
     * @throws AccessDeniedException the access denied exception
     * @throws ItemNotFoundException the item not found exception
     * @throws RepositoryNotAvailableException the repository not available exception
     * @see org.sonatype.nexus.proxy.proxy.router.AbstractRepositoryRouter#dereferenceLink(org.sonatype.nexus.proxy.proxy.item.StorageLinkItem)
     */
    protected StorageItem dereferenceLink( StorageLinkItem link )
        throws NoSuchResourceStoreException,
            AccessDeniedException,
            ItemNotFoundException,
            RepositoryNotAvailableException,
            StorageException
    {
        if ( link.isVirtual() )
        {
            return super.dereferenceLink( link );
        }
        else
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Dereferencing link " + link.getTarget() );
            }
            RepositoryItemUid uid = new RepositoryItemUid( getRepositoryRegistry(), link.getTarget() );
            return uid.getRepository().retrieveItem( false, uid );
        }
    }

    // =====================================================================
    // AbstractPathBasedRepositoryRouter

    protected StorageItem doRetrieveItem( ResourceStoreRequest req )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        return super.retrieveItemPostprocessor( req, renderVirtualPath( req, false ) );
    }

    protected List<StorageItem> doListItems( ResourceStoreRequest req )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        return renderVirtualPath( req, true );
    }

    // =====================================================================
    // Unsupported ops

    protected void doCopyItem( ResourceStoreRequest f, ResourceStoreRequest t )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        throw new UnsupportedOperationException( "Operation COPY not supported on this RepositoryRouter!" );
    }

    protected void doMoveItem( ResourceStoreRequest f, ResourceStoreRequest t )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        throw new UnsupportedOperationException( "Operation MOVE not supported on this RepositoryRouter!" );
    }

    protected void doStoreItem( ResourceStoreRequest req, InputStream is, Map<String, String> userAttributes )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            StorageException,
            AccessDeniedException
    {
        throw new UnsupportedOperationException( "Operation STORE not supported on this RepositoryRouter!" );
    }

    protected void doDeleteItem( ResourceStoreRequest request )
        throws NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        throw new UnsupportedOperationException( "Operation DELETE not supported on this RepositoryRouter!" );
    }

    // =====================================================================
    // Customization stuff No1

    /**
     * Render virtual path.
     * 
     * @param request the request
     * @param list the list
     * @return the list< storage item>
     * @throws NoSuchRepositoryException the no such repository exception
     * @throws NoSuchRepositoryGroupException the no such repository group exception
     */
    protected abstract List<StorageItem> renderVirtualPath( ResourceStoreRequest request, boolean list )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            StorageException;

}
