package org.sonatype.nexus.plugins.maven.api;

import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.project.ProjectBuildingException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.plugins.maven.MavenGate;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.maven.ArtifactStoreHelper;
import org.sonatype.nexus.proxy.maven.ArtifactStoreRequest;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.rest.artifact.AbstractArtifactPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "InterpolatedPomPlexusResource" )
public class InterpolatedPomPlexusResource
    extends AbstractArtifactPlexusResource
{
    @Requirement
    private MavenGate mavenGate;

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/maven/interpolatedPom";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:status]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        try
        {
            // for now, we use "nexus all"
            Model result = mavenGate.getEffectiveModel( getPom( request ), null, null, null, null );

            return result;
        }
        catch ( ProjectBuildingException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage(), e );
        }
    }

    // ==

    protected StorageFileItem getPom( Request request )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();

        String groupId = form.getFirstValue( "g" );

        String artifactId = form.getFirstValue( "a" );

        String version = form.getFirstValue( "v" );

        String repositoryId = form.getFirstValue( "r" );

        if ( groupId == null || artifactId == null || version == null || repositoryId == null )
        {
            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST );
        }

        ArtifactStoreRequest gavRequest =
            getResourceStoreRequest( request, false, repositoryId, groupId, artifactId, version, null, null, "pom" );

        try
        {
            MavenRepository mavenRepository = getMavenRepository( repositoryId );

            ArtifactStoreHelper helper = mavenRepository.getArtifactStoreHelper();

            StorageFileItem file = helper.retrieveArtifactPom( gavRequest );

            return file;
        }
        catch ( Exception e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Ouch!", e );
        }
    }

    protected MavenRepository getMavenRepository( String id )
        throws ResourceException
    {
        try
        {
            Repository repository = getUnprotectedRepositoryRegistry().getRepository( id );

            if ( !repository.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
            {
                throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "This is not a Maven repository!" );
            }

            return repository.adaptToFacet( MavenRepository.class );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage() );
        }
    }

}
