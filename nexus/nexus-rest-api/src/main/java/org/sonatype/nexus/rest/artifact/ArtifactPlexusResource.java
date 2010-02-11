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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.maven.model.Model;
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
@Component( role = PlexusResource.class, hint = "ArtifactPlexusResource" )
@Path( ArtifactPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class ArtifactPlexusResource
    extends AbstractArtifactPlexusResource
{
    public static final String RESOURCE_URI = "/artifact/maven";

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
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:artifact]" );
    }

    /**
     * Returns POM model in a serialized form (it is NOT consumable by Maven, the returned content is not XML
     * representation of Maven POM!) for provided GAV coordinates.
     * 
     * @param g Group id of the pom (Required).
     * @param a Artifact id of the pom (Required).
     * @param v Version of the artifact (Required) Supports resolving of "LATEST", "RELEASE" and snapshot versions
     *            ("1.0-SNAPSHOT") too.
     * @param r Repository to retrieve the pom from (Required).
     */
    @Override
    @GET
    @ResourceMethodSignature( queryParams = { @QueryParam( "g" ), @QueryParam( "a" ), @QueryParam( "v" ),
        @QueryParam( "r" ) }, output = Model.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return getPom( variant, request, response );
    }

}
