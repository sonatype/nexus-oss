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
package org.sonatype.nexus.proxy.item;

import java.util.Collection;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchResourceStoreException;
import org.sonatype.nexus.proxy.RepositoryNotAvailableException;
import org.sonatype.nexus.proxy.RepositoryNotListableException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

/**
 * The Class DefaultStorageCollectionItem.
 */
public class DefaultStorageCollectionItem
    extends AbstractStorageItem
    implements StorageCollectionItem
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7329636330511885938L;

    /**
     * Instantiates a new default storage collection item.
     * 
     * @param repository the repository
     * @param path the path
     * @param canRead the can read
     * @param canWrite the can write
     */
    public DefaultStorageCollectionItem( Repository repository, String path, boolean canRead, boolean canWrite )
    {
        super( repository, path, canRead, canWrite );
    }

    /**
     * Instantiates a new default storage collection item.
     * 
     * @param router the router
     * @param path the path
     * @param virtual the virtual
     * @param canRead the can read
     * @param canWrite the can write
     */
    public DefaultStorageCollectionItem( RepositoryRouter router, String path, boolean canRead, boolean canWrite )
    {
        super( router, path, canRead, canWrite );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageCollectionItem#list()
     */
    public Collection<StorageItem> list()
        throws AccessDeniedException,
            NoSuchResourceStoreException,
            RepositoryNotAvailableException,
            RepositoryNotListableException,
            ItemNotFoundException,
            StorageException
    {
        if ( isVirtual() || !Repository.class.isAssignableFrom( getStore().getClass() ) )
        {
            ResourceStoreRequest req = new ResourceStoreRequest( getPath(), true );

            // let the call inherit the context, ie. auth info, etc.
            req.getRequestContext().putAll( getItemContext() );

            return getStore().list( req );
        }
        else
        {
            Collection<StorageItem> result = ( (Repository) getStore() )
                .list( getRepositoryItemUid(), getItemContext() );

            for ( StorageItem item : result )
            {
                if ( getPath().endsWith( RepositoryItemUid.PATH_SEPARATOR ) )
                {
                    ( (AbstractStorageItem) item ).setPath( getPath() + item.getName() );
                }
                else
                {
                    ( (AbstractStorageItem) item ).setPath( getPath() + RepositoryItemUid.PATH_SEPARATOR
                        + item.getName() );
                }
            }
            return result;
        }
    }

}
