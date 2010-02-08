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

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.ArtifactStoreHelper;
import org.sonatype.nexus.proxy.maven.ArtifactStoreRequest;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.rest.model.ArtifactResolveResource;
import org.sonatype.nexus.rest.model.ArtifactResolveResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * POM Resource handler.
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "ArtifactResolvePlexusResource" )
@Path( ArtifactResolvePlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
public class ArtifactResolvePlexusResource
    extends AbstractArtifactPlexusResource
{
    public static final String RESOURCE_URI = "/artifact/maven/resolve";

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
     * Resolve an artifact and retrieve a set of details about that artifact.
     */
    @Override
    @GET
    @ResourceMethodSignature( queryParams = { @QueryParam( "g" ), @QueryParam( "a" ), @QueryParam( "v" ),
        @QueryParam( "p" ), @QueryParam( "c" ), @QueryParam( "r" ), @QueryParam( "e" ) }, 
        output = ArtifactResolveResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();

        String groupId = form.getFirstValue( "g" );

        String artifactId = form.getFirstValue( "a" );

        String version = form.getFirstValue( "v" );

        String packaging = form.getFirstValue( "p" );

        String classifier = form.getFirstValue( "c" );

        String repositoryId = form.getFirstValue( "r" );

        String extension = form.getFirstValue( "e" );

        if ( groupId == null || artifactId == null || version == null || repositoryId == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
                                         "At least following request parameters have to be given: gavr!" );
        }

        // default the packaging
        if ( StringUtils.isBlank( packaging ) )
        {
            packaging = "jar";
        }

        ArtifactStoreRequest gavRequest =
            getResourceStoreRequest( request, false, repositoryId, groupId, artifactId, version, packaging, classifier,
                                     extension );

        try
        {
            MavenRepository mavenRepository = getMavenRepository( repositoryId );

            ArtifactStoreHelper helper = mavenRepository.getArtifactStoreHelper();

            StorageFileItem file = helper.retrieveArtifact( gavRequest );

            Gav gav = mavenRepository.getGavCalculator().pathToGav( file.getPath() );

            if ( gav == null )
            {
                throw new ItemNotFoundException( "GAV: " + gavRequest.getGroupId() + " : " + gavRequest.getArtifactId()
                    + " : " + gavRequest.getVersion(), gavRequest, mavenRepository );
            }

            ArtifactResolveResource resource = new ArtifactResolveResource();

            resource.setSha1( file.getAttributes().get( DigestCalculatingInspector.DIGEST_SHA1_KEY ) );

            resource.setGroupId( gav.getGroupId() );

            resource.setArtifactId( gav.getArtifactId() );

            resource.setVersion( gav.getVersion() );

            resource.setClassifier( gav.getClassifier() );

            resource.setExtension( gav.getExtension() );

            resource.setFileName( gav.getName() );

            resource.setRepositoryPath( file.getPath() );

            resource.setSnapshot( gav.isSnapshot() );

            if ( resource.isSnapshot() )
            {
                resource.setBaseVersion( gav.getBaseVersion() );

                if ( gav.getSnapshotBuildNumber() != null )
                {
                    resource.setSnapshotBuildNumber( gav.getSnapshotBuildNumber() );

                    resource.setSnapshotTimeStamp( gav.getSnapshotTimeStamp() );
                }
            }

            ArtifactResolveResourceResponse result = new ArtifactResolveResourceResponse();

            result.setData( resource );

            return result;
        }
        catch ( Exception e )
        {
            handleException( request, response, e );

            return null;
        }
    }
}
