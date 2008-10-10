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
package org.sonatype.nexus.rest;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.NoSuchRepositoryGroupException;
import org.sonatype.nexus.proxy.NoSuchRepositoryRouterException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

@Component( role = ManagedPlexusResource.class, hint = "content" )
public class ContentPlexusResource
    extends AbstractResourceStoreContentPlexusResource
    implements ManagedPlexusResource
{
    @Requirement
    private Nexus nexus;

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/**", "contentAuthcBasic,contentTperms" );
    }

    public List<Variant> getVariants()
    {
        List<Variant> result = super.getVariants();

        // default this presentation to HTML to enable user browsing
        result.add( 0, new Variant( MediaType.TEXT_HTML ) );

        return result;
    }

    @Override
    protected ResourceStore getResourceStore( Request request )
        throws NoSuchRepositoryException,
            NoSuchRepositoryGroupException,
            NoSuchRepositoryRouterException,
            ResourceException
    {
        return nexus.getRootRouter();
    }
}
