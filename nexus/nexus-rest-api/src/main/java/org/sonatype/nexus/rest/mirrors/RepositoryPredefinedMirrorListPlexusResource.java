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
package org.sonatype.nexus.rest.mirrors;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.repositories.metadata.NexusRepositoryMetadataHandler;
import org.sonatype.nexus.repository.metadata.MetadataHandlerException;
import org.sonatype.nexus.repository.metadata.model.RepositoryMetadata;
import org.sonatype.nexus.repository.metadata.model.RepositoryMirrorMetadata;
import org.sonatype.nexus.rest.model.MirrorResource;
import org.sonatype.nexus.rest.model.MirrorResourceListResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RepositoryPredefinedMirrorListPlexusResource" )
@Path( RepositoryPredefinedMirrorListPlexusResource.RESOURCE_URI )
@Consumes( { "application/xml", "application/json" } )
public class RepositoryPredefinedMirrorListPlexusResource
    extends AbstractRepositoryMirrorPlexusResource
{
    public static final String RESOURCE_URI = "/repository_predefined_mirrors/{" + REPOSITORY_ID_KEY + "}"; 
    @Requirement
    private NexusRepositoryMetadataHandler repoMetadata;

    public RepositoryPredefinedMirrorListPlexusResource()
    {
        setModifiable( false );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repository_predefined_mirrors/*",
                                             "authcBasic,perms[nexus:repositorypredefinedmirrors]" );
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    /**
     * Get the predefined list of mirrors for a repository (as defined in the repository metadata).  These
     * mirrors may NOT be enabled, simply listed as available.
     * 
     * @param repositoryId The repository to retrieve the predefined mirrors for.
     */
    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( AbstractRepositoryMirrorPlexusResource.REPOSITORY_ID_KEY ) },
                              output = MirrorResourceListResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        MirrorResourceListResponse dto = new MirrorResourceListResponse();

        String repositoryId = this.getRepositoryId( request );

        // get remote metadata
        RepositoryMetadata metadata = this.getMetadata( repositoryId );

        if ( metadata != null )
        {
            for ( RepositoryMirrorMetadata mirror : (List<RepositoryMirrorMetadata>) metadata.getMirrors() )
            {
                MirrorResource resource = new MirrorResource();
                resource.setId( mirror.getId() );
                resource.setUrl( mirror.getUrl() );
                dto.addData( resource );
            }
        }

        return dto;
    }

    private RepositoryMetadata getMetadata( String repositoryId )
        throws ResourceException
    {
        RepositoryMetadata metadata = null;
        try
        {
            metadata = repoMetadata.readRepositoryMetadata( repositoryId );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().error( "Unable to retrieve metadata", e );
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Invalid repository ID", e );
        }
        catch ( MetadataHandlerException e )
        {
            getLogger().info( "Unable to retrieve metadata, returning no items: " + e.getMessage() );
        }
        catch ( IOException e )
        {
            getLogger().error( "Unable to retrieve metadata", e );
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Metadata handling error", e );
        }

        return metadata;
    }

}
