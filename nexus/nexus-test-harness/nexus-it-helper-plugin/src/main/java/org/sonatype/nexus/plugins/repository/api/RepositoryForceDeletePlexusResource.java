package org.sonatype.nexus.plugins.repository.api;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RepositoryForceDeletePlexusResource" )
public class RepositoryForceDeletePlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String REPOSITORY_ID_KEY = "repositoryId";

    public static final String RESOURCE_URI = "/repository_force_delete/{" + REPOSITORY_ID_KEY + "}";

    public RepositoryForceDeletePlexusResource()
    {
        this.setModifiable( true );
        this.setReadable( false );
    }

    @Override
    public Object getPayloadInstance()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repository_force_delete/*", "anon" );
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        String repoId = this.getRepositoryId( request );
        try
        {
            getNexus().deleteRepository( repoId, true );

            response.setStatus( Status.SUCCESS_NO_CONTENT );
        }
        catch ( Exception e )
        {
            getLogger().warn( "Unable to delete repository, id=" + repoId );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to delete repository, id=" + repoId );
        }
    }

    protected String getRepositoryId( Request request )
    {
        return request.getAttributes().get( REPOSITORY_ID_KEY ).toString();
    }
}
