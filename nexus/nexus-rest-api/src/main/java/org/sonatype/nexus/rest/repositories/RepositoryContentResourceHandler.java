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
package org.sonatype.nexus.rest.repositories;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.rest.AbstractResourceStoreContentResource;
import org.sonatype.nexus.rest.ApplicationBridge;

/**
 * Resource handler for Repository content resource.
 * 
 * @author cstamas
 */
public class RepositoryContentResourceHandler
    extends AbstractResourceStoreContentResource
{

    public RepositoryContentResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * The RepositoryContentResourceHandler simply returns the repository by ID that is in request attributes. It is
     * actually coming from router path mapping.
     * 
     * @see ApplicationBridge
     */
    protected ResourceStore getResourceStore()
        throws NoSuchRepositoryException
    {
        return getNexus().getRepository(
            getRequest().getAttributes().get( RepositoryResourceHandler.REPOSITORY_ID_KEY ).toString() );
    }

}
