/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.artifact;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * POM Resource handler.
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "ArtifactRedirectPlexusResource" )
@Path( "/artifact/maven/redirect" )
public class ArtifactRedirectPlexusResource
    extends AbstractArtifactPlexusResource
{

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/artifact/maven/redirect";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:artifact]" );
    }

    /**
     * Emits HTTP redirects (or Not found if not found) to the location of the requested artifact. This resource never
     * returns any content, only 301 Moved permanently or 404 Not found status codes. The HTTP client accessing this
     * resource has to follow the redirection to get to the content of the artifact.
     * 
     * @param g Group id of the artifact (Required).
     * @param a Artifact id of the artifact (Required).
     * @param v Version of the artifact (Required) Supports resolving of "LATEST", "RELEASE" and snapshot versions
     *            ("1.0-SNAPSHOT") too.
     * @param r Repository that the artifact is contained in (Required).
     * @param p Packaging type of the artifact (Optional).
     * @param c Classifier of the artifact (Optional).
     * @param e Extension of the artifact (Optional).
     */
    @Override
    @GET
    @ResourceMethodSignature( queryParams = { @QueryParam( "g" ), @QueryParam( "a" ), @QueryParam( "v" ),
        @QueryParam( "r" ), @QueryParam( "p" ), @QueryParam( "c" ), @QueryParam( "e" ) }, output = String.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return getContent( variant, true, request, response );
    }

}
