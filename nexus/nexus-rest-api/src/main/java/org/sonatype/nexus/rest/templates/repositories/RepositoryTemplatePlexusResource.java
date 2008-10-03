package org.sonatype.nexus.rest.templates.repositories;

import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.nexus.rest.repositories.AbstractRepositoryPlexusResource;

/**
 * @author tstevens
 * @plexus.component role-hint="RepositoryTemplatePlexusResource"
 */
public class RepositoryTemplatePlexusResource
    extends AbstractRepositoryPlexusResource
{
    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return "/templates/repositories/{" + REPOSITORY_ID_KEY + "}";
    }

    protected String getRepositoryId( Request request )
    {
        return request.getAttributes().get( REPOSITORY_ID_KEY ).toString();
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryResourceResponse result = new RepositoryResourceResponse();;
        try
        {
            CRepository model = getNexusInstance( request ).readRepositoryTemplate( getRepositoryId( request ) );

            if ( model == null )
            {
                CRepositoryShadow shadowModel = getNexusInstance( request ).readRepositoryShadowTemplate(
                    getRepositoryId( request ) );

                if ( shadowModel == null )
                {
                    throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository template not found" );
                }

                result.setData( getRepositoryShadowRestModel( shadowModel ) );
            }
            else
            {
                result.setData( getRepositoryRestModel( model ) );
            }
        }
        catch ( IOException e )
        {
            getLogger().warn( "Got IO Exception", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }
        return result;
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RepositoryResourceResponse repoRequest = (RepositoryResourceResponse) payload;
        RepositoryResourceResponse result = null;

        if ( repoRequest != null )
        {
            try
            {
                RepositoryBaseResource resource = repoRequest.getData();

                if ( REPO_TYPE_VIRTUAL.equals( resource.getRepoType() ) )
                {
                    CRepositoryShadow shadow = getNexusInstance( request ).readRepositoryShadowTemplate(
                        getRepositoryId( request ) );

                    if ( shadow == null )
                    {
                        throw new ResourceException(
                            Status.CLIENT_ERROR_NOT_FOUND,
                            "Virtual repository template with ID=" + resource.getId() + " not found" );
                    }
                    else
                    {
                        shadow = getRepositoryShadowAppModel( (RepositoryShadowResource) resource, shadow );

                        getNexusInstance( request ).updateRepositoryShadowTemplate( shadow );

                        result = new RepositoryResourceResponse();
                        result.setData( getRepositoryShadowRestModel( shadow ) );
                    }
                }
                else
                {
                    CRepository normal = getNexusInstance( request )
                        .readRepositoryTemplate( getRepositoryId( request ) );

                    if ( normal == null )
                    {
                        throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository template with ID="
                            + resource.getId() + " not found" );
                    }
                    else
                    {
                        normal = getRepositoryAppModel( (RepositoryResource) resource, normal );

                        getNexusInstance( request ).updateRepositoryTemplate( normal );

                        result = new RepositoryResourceResponse();
                        result.setData( getRepositoryRestModel( normal ) );
                    }
                }
            }
            catch ( IOException e )
            {
                getLogger().warn( "Got IO Exception!", e );

                throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
            }
        }
        
        return result;
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        try
        {
            CRepository model = getNexusInstance( request ).readRepositoryTemplate( getRepositoryId( request ) );

            if ( model == null )
            {
                CRepositoryShadow shadowModel = getNexusInstance( request ).readRepositoryShadowTemplate(
                    getRepositoryId( request ) );

                if ( shadowModel == null )
                {
                    throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository template not found" );
                }

                getNexusInstance( request ).deleteRepositoryShadowTemplate( getRepositoryId( request ) );
            }

            getNexusInstance( request ).deleteRepositoryTemplate( getRepositoryId( request ) );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Got IO Exception!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }
    }
}
