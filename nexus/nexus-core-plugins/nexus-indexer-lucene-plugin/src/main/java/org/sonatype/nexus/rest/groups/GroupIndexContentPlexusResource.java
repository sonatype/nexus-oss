/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.rest.groups;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.indextreeview.AbstractIndexContentPlexusResource;
import org.sonatype.nexus.rest.indextreeview.IndexBrowserTreeViewResponseDTO;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * Group index content resource.
 * 
 * @author dip
 */
@Component( role = PlexusResource.class, hint = "groupIndexResource" )
@Path( GroupIndexContentPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class GroupIndexContentPlexusResource
    extends AbstractIndexContentPlexusResource
{
    public static final String GROUP_ID_KEY = "groupId";
    
    public static final String RESOURCE_URI = "/repo_groups/{" + GROUP_ID_KEY + "}/index_content"; 

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI; 
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repo_groups/*/index_content/**", "authcBasic,tgiperms" );
    }

    @Override
    protected String getRepositoryId( Request request )
    {
        return String.valueOf( request.getAttributes().get( GROUP_ID_KEY ) );
    }
    
    /**
     * Get the index content from the specified group at the specified path.
     * Note that appended to the end of the url should be the path that you want to retrieve index content for.
     * i.e. /content/org/blah will retrieve the content of the index at that node.
     * 
     * @param groupId The group id to retrieve index content from.
     */
    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( GroupIndexContentPlexusResource.GROUP_ID_KEY ) }, 
                              output = IndexBrowserTreeViewResponseDTO.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return super.get( context, request, response, variant );
    }
}
