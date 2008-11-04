package org.sonatype.nexus.rest.repositories;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

/**
 * A resource list for Repository list.
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "RepositoryListPlexusResource" )
public class RepositoryListPlexusResource
    extends AbstractRepositoryPlexusResource
{

    public RepositoryListPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return "/repositories";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:repositories]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return listRepositories( request, false );
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RepositoryResourceResponse repoRequest = (RepositoryResourceResponse) payload;
        String repoId = null;

        if ( repoRequest != null )
        {
            RepositoryBaseResource resource = repoRequest.getData();
            repoId = resource.getId();

            try
            {
                if ( REPO_TYPE_VIRTUAL.equals( resource.getRepoType() ) )
                {
                    try
                    {
                        CRepositoryShadow shadow = getNexus().readRepositoryShadow( resource.getId() );

                        if ( shadow != null )
                        {
                            getLogger().info( "Virtual repository with ID=" + resource.getId() + " already exists!" );

                            throw new PlexusResourceException(
                                Status.CLIENT_ERROR_BAD_REQUEST,
                                "Virtual repository with id=" + resource.getId() + " already exists!",
                                getNexusErrorResponse( "id", "Virtual repository with id=" + resource.getId()
                                    + " already exists!" ) );
                        }
                    }
                    catch ( NoSuchRepositoryException e )
                    {
                        CRepositoryShadow shadow = getRepositoryShadowAppModel(
                            (RepositoryShadowResource) resource,
                            null );

                        getNexus().createRepositoryShadow( shadow );
                    }
                }
                else
                {
                    try
                    {
                        CRepository normal = getNexus().readRepository( resource.getId() );

                        if ( normal != null )
                        {
                            getLogger().info( "Repository with ID=" + resource.getId() + " already exists!" );

                            throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Repository with id="
                                + resource.getId() + " already exists!", getNexusErrorResponse(
                                "id",
                                "Repository with id=" + resource.getId() + " already exists!" ) );
                        }
                    }
                    catch ( NoSuchRepositoryException e )
                    {
                        CRepository normal = getRepositoryAppModel( (RepositoryResource) resource, null );

                        getNexus().createRepository( normal );
                    }
                }
            }
            catch ( ConfigurationException e )
            {
                handleConfigurationException( e );
            }
            catch ( IOException e )
            {
                getLogger().warn( "Got IO Exception!", e );

                throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
            }
        }

        return this.getRepositoryResourceResponse( repoId, this.getNexus() );
    }
}
