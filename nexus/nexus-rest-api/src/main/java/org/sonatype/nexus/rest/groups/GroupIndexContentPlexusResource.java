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

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.rest.AbstractIndexContentPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * Group index content resource.
 * 
 * @author dip
 */
// Component( role = PlexusResource.class, hint = "groupIndexResource" )
public class GroupIndexContentPlexusResource extends
    AbstractIndexContentPlexusResource 
{
    public static final String GROUP_ID_KEY = "groupId";

    @Override
    public String getResourceUri()
    {
        return "/repo_groups/{" + GROUP_ID_KEY + "}/index_content";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repo_groups/*/index_content**", "authcBasic,tgiperms" );
    }
    
    protected IndexingContext getIndexingContext( Request request )
        throws ResourceException
    {
        try
        {
            String groupId = String.valueOf( request.getAttributes().get( GROUP_ID_KEY ) );
            return indexerManager.getRepositoryGroupContext( groupId );
        }
        catch ( NoSuchRepositoryGroupException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e );
        }
    }

}
