/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStore;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

@Component( role = ManagedPlexusResource.class, hint = "content" )
public class ContentPlexusResource
    extends AbstractResourceStoreContentPlexusResource
    implements ManagedPlexusResource
{
    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        // this is managed plexus resource, so path is not important
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

        // also support plain text content inside Nexus repositories
        result.add( new Variant( MediaType.TEXT_PLAIN ) );

        return result;
    }

    @Override
    protected ResourceStore getResourceStore( final Request request )
        throws NoSuchRepositoryException,
            ResourceException
    {
        return getNexus().getRootRouter();
    }
}
