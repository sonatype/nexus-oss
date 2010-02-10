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
package org.sonatype.nexus.rest.repositories;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.ProxyMode;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.NoSuchRepositoryAccessException;
import org.sonatype.nexus.rest.model.RepositoryDependentStatusResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResourceResponse;
import org.sonatype.nexus.rest.util.EnumUtil;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RepositoryStatusPlexusResource" )
@Path( RepositoryStatusPlexusResource.RESOURCE_URI )
@Consumes( { "application/xml", "application/json" } )
@Produces( { "application/xml", "application/json" } )
public class RepositoryStatusPlexusResource
    extends AbstractRepositoryPlexusResource
{
    public static final String RESOURCE_URI = "/repositories/{" + REPOSITORY_ID_KEY + "}/status"; 

    public RepositoryStatusPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new RepositoryStatusResourceResponse();
    }

    @Override
    public String getResourceUri()
    {
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repositories/*/status", "authcBasic,perms[nexus:repostatus]" );
    }

    /**
     * Retrieve the local and remote status of the requested repository.
     * 
     * @param repositoryId The repository to access.
     */
    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY ) }, 
                              output = RepositoryStatusResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        RepositoryStatusResourceResponse result = null;

        String repoId = getRepositoryId( request );

        try
        {
            RepositoryStatusResource resource = new RepositoryStatusResource();

            Repository repo = getRepositoryRegistry().getRepository( repoId );

            resource.setId( repo.getId() );

            resource.setRepoType( getRestRepoType( repo ) );

            resource.setFormat( repo.getRepositoryContentClass().getId() );

            resource.setLocalStatus( repo.getLocalStatus().toString() );

            if ( repo.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
            {
                ProxyRepository prepo = repo.adaptToFacet( ProxyRepository.class );

                resource.setRemoteStatus( getRestRepoRemoteStatus( prepo, request, response ) );

                resource.setProxyMode( prepo.getProxyMode().toString() );
            }

            result = new RepositoryStatusResourceResponse();

            result.setData( resource );

        }
        catch ( NoSuchRepositoryAccessException e )
        {
            getLogger().warn( "Repository access denied, id=" + repoId );

            throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN, "Access Denied to Repository" );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().warn( "Repository not found, id=" + repoId );

            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository Not Found" );
        }

        return result;
    }

    /**
     * Update the local status of the requested repositories.
     * 
     * @param repositoryId The repository to access.
     */
    @Override
    @PUT
    @ResourceMethodSignature( pathParams = { @PathParam( AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY ) },
                              input = RepositoryStatusResourceResponse.class,
                              output = RepositoryStatusResourceResponse.class )
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        RepositoryStatusResourceResponse repoStatusRequest = (RepositoryStatusResourceResponse) payload;

        RepositoryStatusResourceResponse result = null;

        String repoId = getRepositoryId( request );

        if ( repoStatusRequest != null )
        {
            try
            {
                RepositoryStatusResource resource = repoStatusRequest.getData();

                if ( resource.getLocalStatus() == null )
                {
                    throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Local status must be defined" );
                }

                LocalStatus localStatus = EnumUtil.valueOf( resource.getLocalStatus(), LocalStatus.class );
                if ( REPO_TYPE_VIRTUAL.equals( resource.getRepoType() ) )
                {
                    ShadowRepository shadow =
                        getRepositoryRegistry().getRepositoryWithFacet( repoId, ShadowRepository.class );

                    shadow.setLocalStatus( localStatus );

                    getNexusConfiguration().saveConfiguration();

                    result = (RepositoryStatusResourceResponse) this.get( context, request, response, null );
                }
                else
                {
                    Repository repository = getRepositoryRegistry().getRepository( repoId );

                    repository.setLocalStatus( localStatus );

                    if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class )
                        && resource.getProxyMode() != null )
                    {
                        ProxyMode proxyMode = EnumUtil.valueOf( resource.getProxyMode(), ProxyMode.class );
                        repository.adaptToFacet( ProxyRepository.class ).setProxyMode( proxyMode );
                    }

                    // update dependant shadows too
                    for ( ShadowRepository shadow : getRepositoryRegistry().getRepositoriesWithFacet(
                                                                                                      ShadowRepository.class ) )
                    {
                        if ( repository.getId().equals( shadow.getMasterRepositoryId() ) )
                        {
                            shadow.setLocalStatus( localStatus );
                        }
                    }

                    getNexusConfiguration().saveConfiguration();

                    result = (RepositoryStatusResourceResponse) this.get( context, request, response, null );

                    for ( ShadowRepository shadow : getRepositoryRegistry().getRepositoriesWithFacet(
                                                                                                      ShadowRepository.class ) )
                    {
                        if ( repository.getId().equals( shadow.getMasterRepositoryId() ) )
                        {
                            RepositoryDependentStatusResource dependent = new RepositoryDependentStatusResource();

                            dependent.setId( shadow.getId() );

                            dependent.setRepoType( getRestRepoType( shadow ) );

                            dependent.setFormat( shadow.getRepositoryContentClass().getId() );

                            dependent.setLocalStatus( shadow.getLocalStatus().toString() );

                            result.getData().addDependentRepo( dependent );
                        }
                    }
                }

            }
            catch ( NoSuchRepositoryAccessException e )
            {
                getLogger().warn( "Repository access denied, id=" + repoId );

                throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN, "Access Denied to Repository" );
            }
            catch ( NoSuchRepositoryException e )
            {
                getLogger().warn( "Repository not found, id=" + repoId );

                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Repository Not Found" );
            }
            // catch ( ConfigurationException e )
            // {
            // getLogger().warn( "Configuration unacceptable, repoId=" + repoId, e );
            //
            // throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
            // "Configuration unacceptable, repoId=" + repoId + ": "
            // + e.getMessage(), getNexusErrorResponse( "*", e.getMessage() ) );
            // }
            catch ( IOException e )
            {
                getLogger().warn( "Got IO Exception!", e );

                throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
            }
        }
        return result;
    }

}
