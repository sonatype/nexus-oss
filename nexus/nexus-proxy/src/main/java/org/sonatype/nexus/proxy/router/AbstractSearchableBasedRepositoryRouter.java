/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.router;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.target.TargetSet;

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
     */
    @Requirement
    private NexusIndexer indexer;

    /**
     * The repository registry.
     */
    @Requirement
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

    // =====================================================================
    // AbstractPathBasedRepositoryRouter

    protected StorageItem doRetrieveItem( ResourceStoreRequest req )
        throws RepositoryNotAvailableException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        return super.retrieveItemPostprocessor( req, renderVirtualPath( req, false ) );
    }

    protected List<StorageItem> doListItems( ResourceStoreRequest req )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        return renderVirtualPath( req, true );
    }

    // =====================================================================
    // Unsupported ops

    protected void doCopyItem( ResourceStoreRequest f, ResourceStoreRequest t )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        throw new UnsupportedOperationException( "Operation COPY not supported on this RepositoryRouter!" );
    }

    protected void doMoveItem( ResourceStoreRequest f, ResourceStoreRequest t )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        throw new UnsupportedOperationException( "Operation MOVE not supported on this RepositoryRouter!" );
    }

    protected void doStoreItem( ResourceStoreRequest req, InputStream is, Map<String, String> userAttributes )
        throws IllegalOperationException,
            StorageException,
            AccessDeniedException
    {
        throw new UnsupportedOperationException( "Operation STORE not supported on this RepositoryRouter!" );
    }

    protected void doDeleteItem( ResourceStoreRequest request )
        throws IllegalOperationException,
            ItemNotFoundException,
            StorageException,
            AccessDeniedException
    {
        throw new UnsupportedOperationException( "Operation DELETE not supported on this RepositoryRouter!" );
    }

    protected TargetSet doGetTargetsForRequest( ResourceStoreRequest request )
    {
        // this imple simply returns empty
        return new TargetSet();
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
        throws ItemNotFoundException,
            StorageException;

}
