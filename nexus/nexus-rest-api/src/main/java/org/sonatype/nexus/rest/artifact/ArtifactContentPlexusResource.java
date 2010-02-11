/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.artifact;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "ArtifactContentPlexusResource" )
@Path( "/artifact/maven/content" )
@Produces( "*/*" )
public class ArtifactContentPlexusResource
    extends AbstractArtifactPlexusResource
{
    public ArtifactContentPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public List<Variant> getVariants()
    {
        return Collections.singletonList( new Variant( MediaType.TEXT_HTML ) );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/artifact/maven/content";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:artifact]" );
    }

    @Override
    public boolean acceptsUpload()
    {
        return true;
    }

    /**
     * Retrieves the content of the requested artifact. The HTTP client accessing this resource has to obey the content
     * disposition headers in HTTP response, where the real name of the artifact (but not the path!) is set, if name of
     * the artifact file is needed.
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
        return getContent( variant, false, request, response );
    }
}
