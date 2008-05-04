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
package org.sonatype.nexus.rest.identify;

import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.model.NexusArtifact;

/**
 * Resource that is able to fetch the identified Nexus Artifact. The used hash algorithm and hash key are coming from
 * request attributes, and are posibly mapped from URL. Recognized algorithms: "sha1" and "md5".
 * 
 * @author cstamas
 */
public class IdentifyHashResourceHandler
    extends AbstractNexusResourceHandler
{

    public static final String ALGORITHM_KEY = "algorithm";

    public static final String HASH_KEY = "hash";

    public IdentifyHashResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    public Representation getRepresentationHandler( Variant variant )
        throws IOException
    {
        String alg = getRequest().getAttributes().get( ALGORITHM_KEY ).toString();

        String checksum = getRequest().getAttributes().get( HASH_KEY ).toString();

        NexusArtifact na = null;

        if ( "md5".equalsIgnoreCase( alg ) )
        {
            na = ai2Na( getNexus().identifyArtifact( ArtifactInfo.MD5, checksum ) );
        }
        else if ( "sha1".equalsIgnoreCase( alg ) )
        {
            na = ai2Na( getNexus().identifyArtifact( ArtifactInfo.SHA1, checksum ) );
        }

        if ( na != null )
        {
            return serialize( variant, na );
        }
        else
        {
            return null;
        }
    }

}
