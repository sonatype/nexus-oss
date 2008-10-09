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

/**
 * Resource handler for Repository resource.
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "RepositoryPlexusResource" )
public class RepositoryPlexusResource
    extends AbstractRepositoryPlexusResource
{

    public RepositoryPlexusResource()
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
        return "/repositories/{" + REPOSITORY_ID_KEY + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repositories/*", "authcBasic,perms[nexus:repositories]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return this.getRepositoryResourceResponse( this.getRepositoryId( request ), this.getNexusInstance( request ) );
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RepositoryResourceResponse repoRequest = (RepositoryResourceResponse) payload;

        String repoId = this.getRepositoryId( request );

        if ( repoRequest != null )
        {
            try
            {
                RepositoryBaseResource resource = repoRequest.getData();

                if ( REPO_TYPE_VIRTUAL.equals( resource.getRepoType() ) )
                {
                    try
                    {
                        CRepositoryShadow shadow = getNexusInstance( request ).readRepositoryShadow( repoId );

                        shadow = getRepositoryShadowAppModel( (RepositoryShadowResource) resource, shadow );

                        getNexusInstance( request ).updateRepositoryShadow( shadow );
                    }
                    catch ( NoSuchRepositoryException e )
                    {
                        getLogger().warn( "Virtual repository not found, id=" + repoId );

                        throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Virtual repository Not Found" );
                    }
                }
                else
                {
                    try
                    {
                        CRepository normal = getNexusInstance( request ).readRepository( repoId );

                        normal = getRepositoryAppModel( (RepositoryResource) resource, normal );

                        getNexusInstance( request ).updateRepository( normal );
                    }
                    catch ( NoSuchRepositoryException e )
                    {
                        getLogger().warn( "Repository not found, id=" + repoId );

                        throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository Not Found" );
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

        // return current repo
        return this.getRepositoryResourceResponse( this.getRepositoryId( request ), this.getNexusInstance( request ) );
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        String repoId = this.getRepositoryId( request );
        try
        {
            try
            {
                getNexusInstance( request ).deleteRepository( repoId );
            }
            catch ( NoSuchRepositoryException e )
            {
                getNexusInstance( request ).deleteRepositoryShadow( repoId );
            }

            response.setStatus( Status.SUCCESS_NO_CONTENT );
        }
        catch ( ConfigurationException e )
        {
            getLogger().warn( "Repository not deletable, it has dependants, id=" + repoId );

            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST,
                "Repository is not deletable, it has dependants." );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "Repository not found, id=" + repoId );

            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository Not Found" );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Got IO Exception!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }
    }

}
