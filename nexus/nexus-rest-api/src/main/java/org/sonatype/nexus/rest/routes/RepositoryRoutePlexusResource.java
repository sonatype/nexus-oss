package org.sonatype.nexus.rest.routes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;
import org.sonatype.nexus.rest.model.RepositoryRouteResourceResponse;
import org.sonatype.plexus.rest.resource.PlexusResourceException;

/**
 * Handles GET, PUT, DELETE for Repository route resources.
 * 
 * @author cstamas
 * @author tstevens
 * @plexus.component role-hint="RepositoryRoutePlexusResource"
 */
public class RepositoryRoutePlexusResource
    extends AbstractRepositoryRoutePlexusResource
{

    public RepositoryRoutePlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryRouteResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return "/repo_routes/{" + ROUTE_ID_KEY + "}";
    }

    protected String getRouteId( Request request )
    {
        return request.getAttributes().get( ROUTE_ID_KEY ).toString();
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {

        RepositoryRouteResourceResponse result = null;

        try
        {
            CGroupsSettingPathMappingItem route = getNexusInstance( request ).readGroupsSettingPathMapping(
                getRouteId( request ) );

            if ( route == null )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "No such route" );
            }

            RepositoryRouteResource resource = new RepositoryRouteResource();

            resource.setId( getRouteId( request ) );

            resource.setGroupId( route.getGroupId() );

            resource.setRuleType( config2resourceType( route.getRouteType() ) );

            resource.setPattern( route.getRoutePattern() );

            resource.setRepositories( getRepositoryRouteMemberRepositoryList(
                request.getResourceRef().getParentRef(),
                route.getRepositories(),
                request ) );

            result = new RepositoryRouteResourceResponse();

            result.setData( resource );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "Cannot find a repository declared within a group!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }
        catch ( IndexOutOfBoundsException e )
        {
            getLogger().warn( "Repository route not found, id=" + getRouteId( request ) );

            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository Route Not Found" );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Got IO Exception!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }
        return result;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RepositoryRouteResourceResponse routeRequest = (RepositoryRouteResourceResponse) payload;
        RepositoryRouteResourceResponse result = null;

        if ( routeRequest != null )
        {
            RepositoryRouteResource resource = routeRequest.getData();

            if ( ( !RepositoryRouteResource.BLOCKING_RULE_TYPE.equals( resource.getRuleType() ) && ( resource
                .getRepositories() == null || resource.getRepositories().size() == 0 ) )
                || resource.getId() == null || !resource.getId().equals( getRouteId( request ) ) )
            {
                throw new PlexusResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "The route cannot have zero repository members!",
                    getNexusErrorResponse( "repositories", "The route cannot have zero repository members!" ) );
            }
            else if ( RepositoryRouteResource.BLOCKING_RULE_TYPE.equals( resource.getRuleType() ) )
            {
                resource.setRepositories( null );
            }

            try
            {
                CGroupsSettingPathMappingItem route = getNexusInstance( request ).readGroupsSettingPathMapping(
                    getRouteId( request ) );

                if ( route == null )
                {
                    throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Route not found!" );
                }

                route.setId( getRouteId( request ) );

                route.setGroupId( resource.getGroupId() );

                route.setRoutePattern( resource.getPattern() );

                route.setRouteType( resource2configType( resource.getRuleType() ) );

                List<String> repositories = new ArrayList<String>( resource.getRepositories().size() );

                for ( RepositoryRouteMemberRepository repo : (List<RepositoryRouteMemberRepository>) resource
                    .getRepositories() )
                {
                    repositories.add( repo.getId() );
                }

                route.setRepositories( repositories );

                getNexusInstance( request ).updateGroupsSettingPathMapping( route );
                
                response.setStatus( Status.SUCCESS_NO_CONTENT );
            }
            catch ( ConfigurationException e )
            {
                handleConfigurationException( e );
            }
            catch ( NoSuchRepositoryException e )
            {
                getLogger().warn( "Cannot find a repository referenced within a route!", e );

                throw new PlexusResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Cannot find a repository referenced within a route!",
                    getNexusErrorResponse( "repositories", "Cannot find a repository referenced within a route!" ) );
            }
            catch ( IOException e )
            {
                getLogger().warn( "Got IO Exception!", e );

                throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
            }
        }
        // TODO: this is null, because we return a 204
        return result;
    }

    @Override
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        try
        {
            CGroupsSettingPathMappingItem route = getNexusInstance( request ).readGroupsSettingPathMapping(
                getRouteId( request ) );

            if ( route == null )
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Route not found!" );
            }

            getNexusInstance( request ).deleteGroupsSettingPathMapping( getRouteId( request ) );

            response.setStatus( Status.SUCCESS_NO_CONTENT );
        }
        catch ( IOException e )
        {
            getLogger().warn( "Got IO Exception!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }
    }

}
