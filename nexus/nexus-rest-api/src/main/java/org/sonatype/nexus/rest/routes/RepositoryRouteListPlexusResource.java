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
package org.sonatype.nexus.rest.routes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.mapping.RepositoryPathMapping;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.rest.NoSuchRepositoryAccessException;
import org.sonatype.nexus.rest.model.RepositoryRouteListResource;
import org.sonatype.nexus.rest.model.RepositoryRouteListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;
import org.sonatype.nexus.rest.model.RepositoryRouteResourceResponse;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;

/**
 * A resource list for Repository route list.
 * 
 * @author cstamas
 * @author tstevens
 */
@Component( role = PlexusResource.class, hint = "RepositoryRouteListPlexusResource" )
public class RepositoryRouteListPlexusResource
    extends AbstractRepositoryRoutePlexusResource
{

    public RepositoryRouteListPlexusResource()
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
        return "/repo_routes";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:routes]" );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryRouteListResourceResponse result = new RepositoryRouteListResourceResponse();

        Map<String, RepositoryPathMapping> mappings = getRepositoryMapper().getMappings();

        RepositoryRouteListResource resource = null;

        try
        {
            for ( RepositoryPathMapping item : mappings.values() )
            {
                try
                {
                    resource = new RepositoryRouteListResource();

                    if ( !item.getGroupId().equals( "*" ) )
                    {
                        // XXX: added to check access to group
                        this.getRepositoryRegistry().getRepositoryWithFacet( item.getGroupId(), GroupRepository.class );
                    }
                    resource.setGroupId( item.getGroupId() );

                    resource.setResourceURI( createChildReference( request, this, item.getId() ).toString() );

                    resource.setRuleType( config2resourceType( item.getMappingType() ) );

                    // XXX: cstamas -- a hack!
                    resource.setPattern( item.getPatterns().get( 0 ).toString() );

                    resource.setRepositories( getRepositoryRouteMemberRepositoryList( request.getResourceRef(), item
                        .getMappedRepositories(), request ) );

                    result.addData( resource );
                }
                catch ( NoSuchRepositoryAccessException e )
                {
                    // we are listing the routes, we do not need to fail the list because only one entry is not
                    // available to the user
                    getLogger().debug( "Access Denied to Repository contained within route.", e );
                }
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "Cannot find a repository declared within a mapping!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }

        return result;
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RepositoryRouteResourceResponse routeRequest = (RepositoryRouteResourceResponse) payload;

        RepositoryRouteResourceResponse result = null;

        if ( routeRequest != null )
        {
            RepositoryRouteResource resource = routeRequest.getData();

            if ( !RepositoryRouteResource.BLOCKING_RULE_TYPE.equals( resource.getRuleType() )
                && ( resource.getRepositories() == null || resource.getRepositories().size() == 0 ) )
            {
                throw new PlexusResourceException(
                                                   Status.CLIENT_ERROR_BAD_REQUEST,
                                                   "The route cannot have zero repository members!",
                                                   getNexusErrorResponse( "repositories",
                                                                          "The route cannot have zero repository members!" ) );
            }
            else if ( RepositoryRouteResource.BLOCKING_RULE_TYPE.equals( resource.getRuleType() ) )
            {
                resource.setRepositories( null );
            }

            resource.setId( Long.toHexString( System.nanoTime() ) );

            try
            {
                ArrayList<String> mappedReposes = new ArrayList<String>( resource.getRepositories().size() );

                for ( RepositoryRouteMemberRepository member : resource.getRepositories() )
                {
                    mappedReposes.add( member.getId() );
                }

                RepositoryPathMapping route =
                    new RepositoryPathMapping( resource.getId(), resource2configType( resource.getRuleType() ),
                                               resource.getGroupId(), Arrays.asList( new String[] { resource
                                                   .getPattern() } ), mappedReposes );

                getRepositoryMapper().addMapping( route );

                getNexusConfiguration().saveConfiguration();

                resource.setGroupId( route.getGroupId() );

                result = new RepositoryRouteResourceResponse();

                result.setData( resource );
            }
            catch ( ConfigurationException e )
            {
                if ( e.getCause() != null && e.getCause() instanceof PatternSyntaxException )
                {
                    throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error.",
                                                       getNexusErrorResponse( "pattern", e.getMessage() ) );
                }
                else
                {
                    handleConfigurationException( e );
                }
            }
            catch ( PatternSyntaxException e )
            {
                // TODO: fix because this happens before we validate, we need to fix the validation.
                ErrorResponse errorResponse = getNexusErrorResponse( "*", e.getMessage() );
                throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error.", errorResponse );
            }
/*            catch ( NoSuchRepositoryException e )
            {
                getLogger().warn( "Cannot find a repository referenced within a route!", e );

                throw new PlexusResourceException(
                                                   Status.CLIENT_ERROR_BAD_REQUEST,
                                                   "Cannot find a repository referenced within a route!",
                                                   getNexusErrorResponse( "repositories",
                                                                          "Cannot find a repository referenced within a route!" ) );
            }*/
            catch ( IOException e )
            {
                getLogger().warn( "Got IO Exception!", e );

                throw new ResourceException( Status.SERVER_ERROR_INTERNAL );

            }
        }
        return result;
    }

}
