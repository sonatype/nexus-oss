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

import java.util.ArrayList;
import java.util.List;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * This router offers the "per-repository view" by using paths in form "/(repositoryId)/(repositoryPath)". Because
 * repositoryId's are unique across Proximity, this Router does not handle aggregations, since there are not needed.
 * 
 * @author cstamas
 */
public abstract class RepoIdBasedRepositoryRouter
    extends AbstractRegistryDrivenRepositoryRouter
    implements RepositoryRouter
{

    /**
     * We are rendering registered reposes.
     */
    protected List<StorageItem> renderVirtualPath( ResourceStoreRequest request, boolean list )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException
    {
        List<StorageItem> result = new ArrayList<StorageItem>();

        if ( list )
        {
            // we are doing "dir"
            if ( request.getRequestRepositoryId() != null )
            {
                // we have a targeted request at repos
                Repository repository = getRepositoryRegistry().getRepository( request.getRequestRepositoryId() );

                if ( repository.getLocalStatus().shouldServiceRequest() )
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug(
                            "Adding repository " + repository.getId() + " to virtual path " + request.getRequestPath() );
                    }

                    DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem(
                        this,
                        RepositoryItemUid.PATH_ROOT + repository.getId(),
                        true,
                        false );

                    coll.setRepositoryId( repository.getId() );

                    result.add( coll );
                }
            }
            else
            {
                // list all reposes as "root"
                for ( Repository repository : getRepositoryRegistry().getRepositories() )
                {
                    if ( repository.getLocalStatus().shouldServiceRequest() )
                    {
                        if ( getLogger().isDebugEnabled() )
                        {
                            getLogger().debug(
                                "Adding repository " + repository.getId() + " to virtual path "
                                    + request.getRequestPath() );
                        }
                        DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem(
                            this,
                            RepositoryItemUid.PATH_ROOT + repository.getId(),
                            true,
                            false );

                        coll.setRepositoryId( repository.getId() );

                        result.add( coll );
                    }
                }
            }
        }
        else
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Adding ROOT to virtual path " + request.getRequestPath() );
            }
            DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem(
                this,
                RepositoryItemUid.PATH_ROOT,
                true,
                false );

            result.add( coll );
        }

        return result;
    }

    /**
     * The first elem in path is used to indetify a repo by it's ID.
     */
    protected List<ResourceStore> resolveResourceStore( ResourceStoreRequest request )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException
    {
        List<ResourceStore> result = null;

        if ( !RepositoryItemUid.PATH_ROOT.equals( request.getRequestPath() ) )
        {
            String path = request.getRequestPath().startsWith( RepositoryItemUid.PATH_ROOT ) ? request
                .getRequestPath().substring( 1 ) : request.getRequestPath();

            String[] explodedPath = path.split( RepositoryItemUid.PATH_SEPARATOR );

            if ( explodedPath.length >= 1 )
            {
                result = new ArrayList<ResourceStore>( 1 );

                result.add( getRepositoryRegistry().getRepository( explodedPath[0] ) );
            }
        }
        return result;
    }

}
