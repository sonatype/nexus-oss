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

import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.nexus.rest.AbstractResourceStoreContentPlexusResource;
import org.sonatype.nexus.rest.ApplicationBridge;

/**
 * A simple testing resource. Will "publish" itself on passed in token URI
 * (/token) and will emit "token" for GETs and respond with HTTP 200 to all
 * other HTTP methods (PUT, POST) only if the token equals to entity passed in.
 * 
 * @author dip
 * @plexus.component role-hint="repoIndexBrowser"
 */
public class RepositoryIndexContentPlexusResource extends AbstractResourceStoreContentPlexusResource {

    public static final String REPOSITORY_ID_KEY = "repositoryId";
    
    @Override
    public Object getPayloadInstance() {
        return null;
    }

    @Override
    public String getResourceUri() {
        return "/repositories/{" + REPOSITORY_ID_KEY + "}/index_content";
    }
    
    /**
     * The RepositoryContentResourceHandler simply returns the repository by ID that is in request attributes. It is
     * actually coming from router path mapping.
     * 
     * @see ApplicationBridge
     */
    protected ResourceStore getResourceStore( Request request )
        throws ResourceException, NoSuchRepositoryException
    {
        return getNexusInstance(request).getRepository(
            request.getAttributes().get( REPOSITORY_ID_KEY ).toString() );
    }
}
