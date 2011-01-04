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
package org.sonatype.nexus.rest.identify;

import java.io.IOException;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.maven.index.MAVEN;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.rest.AbstractIndexerNexusPlexusResource;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * Resource that is able to fetch the identified Nexus Artifact. The used hash algorithm and hash key are coming from
 * request attributes, and are posibly mapped from URL. Recognized algorithms: "sha1" and "md5".
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "IdentifyHashPlexusResource" )
@Path( IdentifyHashPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class IdentifyHashPlexusResource
    extends AbstractIndexerNexusPlexusResource
{
    public static final String ALGORITHM_KEY = "algorithm";

    public static final String HASH_KEY = "hash";

    public static final String RESOURCE_URI = "/identify/{" + ALGORITHM_KEY + "}/{" + HASH_KEY + "}";

    @Requirement
    private IndexerManager indexerManager;

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/identify/*/*", "authcBasic,perms[nexus:identify]" );
    }

    /**
     * Retrieve artifact details using a hash value.
     * 
     * @param algorithm The hash algorithm (i.e. md5 or sha1).
     * @param hash The hash string to compare.
     */
    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( IdentifyHashPlexusResource.ALGORITHM_KEY ),
        @PathParam( IdentifyHashPlexusResource.HASH_KEY ) }, output = NexusArtifact.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String alg = request.getAttributes().get( ALGORITHM_KEY ).toString();

        String checksum = request.getAttributes().get( HASH_KEY ).toString();

        NexusArtifact na = null;

        try
        {
            if ( "sha1".equalsIgnoreCase( alg ) )
            {
                Collection<NexusArtifact> nas =
                    ai2NaColl( request, indexerManager.identifyArtifact( MAVEN.SHA1, checksum ) );

                if ( nas != null && nas.size() > 0 )
                {
                    na = nas.iterator().next();
                }
            }
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "IOException during configuration retrieval!", e );
        }

        return na;
    }
}
