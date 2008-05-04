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
package org.sonatype.nexus.rest.groups;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.proxy.NoSuchRepositoryRouterException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.proxy.maven.M2GroupIdBasedRepositoryRouter;
import org.sonatype.nexus.proxy.router.RepositoryRouter;
import org.sonatype.nexus.rest.AbstractResourceStoreContentResource;

/**
 * Resource handler for Repository content resource.
 * 
 * @author cstamas
 */
public class RepositoryGroupContentResourceHandler
    extends AbstractResourceStoreContentResource
{

    private final String groupId;

    public RepositoryGroupContentResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );

        this.groupId = getRequest().getAttributes().get( RepositoryGroupResourceHandler.GROUP_ID_KEY ).toString();
    }

    /**
     * The RepositoryGroupContentResourceHandler simply returns the router that does "grouping", since Router also
     * implements the ResourceStore interface. But this is not enough, since the router prepends the "groupId" to path,
     * so we must mangle the path.
     */
    protected ResourceStore getResourceStore()
        throws NoSuchRepositoryRouterException
    {
        setResourceStorePath( "/" + groupId + getResourceStorePath() );

        // TODO: THIS IS BAD! Dynamic router needed!
        return (ResourceStore) lookup( RepositoryRouter.ROLE, M2GroupIdBasedRepositoryRouter.ID );
    }
}
