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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.DefaultStorageCollectionItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * This router offers the "per-router view" by using paths in form "/(routerId)/...". Because Id's are unique across
 * Proximity Routers, this Router does not handle aggregations, since they are not needed.
 * 
 * @author cstamas
 */
@Component( role = RootRepositoryRouter.class )
public class ResourceStoreIdBasedRepositoryRouter
    extends AbstractRegistryDrivenRepositoryRouter
    implements RootRepositoryRouter, Initializable
{
    public static final String ROLE = "org.sonatype.nexus.proxy.router.RootRepositoryRouter";

    public static final String ID = "root";

    private ContentClass contentClass = new DefaultContentClass();

    /**
     * The map of routers by their role-hint.
     */
    @Requirement( role = RepositoryRouter.class )
    private List<RepositoryRouter> routers;

    /**
     * The known handlers for content classes.
     */
    private Map<String, List<RepositoryRouter>> contentClassHandlers;

    public void initialize()
    {
        super.initialize();

        contentClassHandlers = new HashMap<String, List<RepositoryRouter>>();

        for ( RepositoryRouter router : routers )
        {
            if ( !contentClassHandlers.containsKey( router.getId() ) )
            {
                contentClassHandlers.put( router.getId(), new ArrayList<RepositoryRouter>() );
            }

            if ( DefaultContentClass.ID.equals( router.getHandledContentClass().getId() ) )
            {
                contentClassHandlers.get( router.getId() ).add( 0, router );
            }
            else
            {
                contentClassHandlers.get( router.getId() ).add( router );
            }
        }
    }

    public String getId()
    {
        return ID;
    }

    public ContentClass getHandledContentClass()
    {
        return contentClass;
    }

    /**
     * We are rendering "other routers".
     */
    protected List<StorageItem> renderVirtualPath( ResourceStoreRequest request, boolean list )
    {
        List<StorageItem> result = new ArrayList<StorageItem>();

        if ( list )
        {
            // creating a list of collections. Each collection is actually the "root" of an available Router
            for ( String id : contentClassHandlers.keySet() )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Adding path " + id + " to virtual path " + request.getRequestPath() );
                }

                DefaultStorageCollectionItem coll = new DefaultStorageCollectionItem( this, RepositoryItemUid.PATH_ROOT
                    + id, true, false );

                result.add( coll );
            }
        }
        else if ( !list )
        {
            // creating a simple CollectionItem that is actually the "root" of this router.
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
     * This is RouterRouter, hence we are returning a Router identified by ID as first elem im path.
     */
    public List<ResourceStore> resolveResourceStore( ResourceStoreRequest request )
        throws NoSuchRepositoryGroupException
    {
        List<ResourceStore> result = null;

        if ( !RepositoryItemUid.PATH_ROOT.equals( request.getRequestPath() ) )
        {
            String path = request.getRequestPath().startsWith( RepositoryItemUid.PATH_ROOT ) ? request
                .getRequestPath().substring( 1 ) : request.getRequestPath();

            String[] explodedPath = path.split( RepositoryItemUid.PATH_SEPARATOR );

            if ( explodedPath.length >= 1 )
            {
                ResourceStore store = null;

                ResourceStore defaultHandler = null;

                if ( contentClassHandlers.containsKey( explodedPath[0] ) )
                {
                    // it is a known id, like: repositories, groups, etc.
                    List<RepositoryRouter> handlers = contentClassHandlers.get( explodedPath[0] );

                    if ( handlers.size() == 1 )
                    {
                        store = handlers.get( 0 );
                    }
                    else if ( handlers.size() > 1 )
                    {
                        // ALL_CONTENTS handler is 1st
                        defaultHandler = handlers.get( 0 );

                        // look for the best one if possible
                        if ( explodedPath.length > 1 )
                        {
                            if ( !StringUtils.isEmpty( explodedPath[1] ) )
                            {
                                ContentClass groupContentClass = getRepositoryRegistry()
                                    .getRepositoryGroupContentClass( explodedPath[1] );

                                for ( RepositoryRouter rr : handlers )
                                {
                                    if ( groupContentClass.getId().equals( rr.getHandledContentClass().getId() ) )
                                    {
                                        store = rr;
                                    }
                                }
                            }
                        }

                        if ( store == null )
                        {
                            store = defaultHandler;
                        }
                    }
                    result = new ArrayList<ResourceStore>( 1 );

                    result.add( store );
                }
            }
        }
        return result;
    }
}
