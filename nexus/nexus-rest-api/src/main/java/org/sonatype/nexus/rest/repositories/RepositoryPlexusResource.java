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
import java.lang.reflect.Method;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.rest.NoSuchRepositoryAccessException;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;
import org.sonatype.nexus.rest.util.EnumUtil;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;

/**
 * Resource handler for Repository resource.
 * 
 * @author cstamas
 */
@Component( role = PlexusResource.class, hint = "RepositoryPlexusResource" )
@Path( RepositoryPlexusResource.RESOURCE_URI )
@Produces( { "application/xml", "application/json" } )
@Consumes( { "application/xml", "application/json" } )
public class RepositoryPlexusResource
    extends AbstractRepositoryPlexusResource
{
    public static final String RESOURCE_URI = "/repositories/{" + REPOSITORY_ID_KEY + "}";

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
        return RESOURCE_URI;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/repositories/*", "authcBasic,perms[nexus:repositories]" );
    }

    /**
     * Get the configuration of an existing repository.
     * 
     * @param repositoryId The repository to access.
     */
    @Override
    @GET
    @ResourceMethodSignature( pathParams = { @PathParam( AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY ) }, output = RepositoryResourceResponse.class )
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return this.getRepositoryResourceResponse( request, getRepositoryId( request ) );
    }

    /**
     * Update an existing repository in nexus with new configuration.
     * 
     * @param repositoryId The repository to access.
     */
    @Override
    @PUT
    @ResourceMethodSignature( pathParams = { @PathParam( AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY ) }, input = RepositoryResourceResponse.class, output = RepositoryResourceResponse.class )
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
                    RepositoryShadowResource model = (RepositoryShadowResource) resource;

                    try
                    {
                        ShadowRepository shadow =
                            getRepositoryRegistry().getRepositoryWithFacet( repoId, ShadowRepository.class );

                        shadow.setName( model.getName() );

                        shadow.setExposed( resource.isExposed() );

                        shadow.setMasterRepositoryId( model.getShadowOf() );

                        shadow.setSynchronizeAtStartup( model.isSyncAtStartup() );

                        getNexusConfiguration().saveConfiguration();
                    }
                    catch ( NoSuchRepositoryAccessException e )
                    {
                        getLogger().warn( "Repository access denied, id=" + repoId );

                        throw new ResourceException( Status.CLIENT_ERROR_FORBIDDEN, "Access Denied to Repository" );
                    }
                    catch ( NoSuchRepositoryException e )
                    {
                        getLogger().warn( "Virtual repository not found, id=" + repoId );

                        throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Virtual repository Not Found" );
                    }
                }
                else
                {
                    RepositoryResource model = (RepositoryResource) resource;

                    try
                    {
                        Repository repository = getRepositoryRegistry().getRepository( repoId );

                        repository.setName( model.getName() );

                        repository.setExposed( resource.isExposed() );

                        // set null to read only
                        RepositoryWritePolicy writePolicy =
                            ( model.getWritePolicy() != null ) ? RepositoryWritePolicy.valueOf( model.getWritePolicy() )
                                            : RepositoryWritePolicy.READ_ONLY;

                        repository.setWritePolicy( writePolicy );

                        repository.setBrowseable( model.isBrowseable() );

                        repository.setIndexable( model.isIndexable() );
                        repository.setSearchable( model.isIndexable() );

                        repository.setNotFoundCacheTimeToLive( model.getNotFoundCacheTTL() );

                        if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
                        {
                            ProxyRepository proxyRepo = repository.adaptToFacet( ProxyRepository.class );

                            proxyRepo.setRemoteUrl( model.getRemoteStorage().getRemoteStorageUrl() );
                            String oldPasswordForRemoteStorage = null;
                            if ( proxyRepo.getRemoteAuthenticationSettings() != null
                                && UsernamePasswordRemoteAuthenticationSettings.class.isInstance( proxyRepo
                                    .getRemoteAuthenticationSettings() ) )
                            {
                                oldPasswordForRemoteStorage =
                                    ( (UsernamePasswordRemoteAuthenticationSettings) proxyRepo
                                        .getRemoteAuthenticationSettings() ).getPassword();
                            }

                            String oldPasswordForProxy = null;
                            if ( proxyRepo.getRemoteProxySettings() != null
                                && proxyRepo.getRemoteProxySettings().isEnabled()
                                && proxyRepo.getRemoteProxySettings().getProxyAuthentication() != null
                                && UsernamePasswordRemoteAuthenticationSettings.class.isInstance( proxyRepo
                                    .getRemoteAuthenticationSettings() ) )
                            {
                                oldPasswordForProxy =
                                    ( (UsernamePasswordRemoteAuthenticationSettings) proxyRepo.getRemoteProxySettings()
                                        .getProxyAuthentication() ).getPassword();
                            }

                            RemoteAuthenticationSettings remoteAuth =
                                getAuthenticationInfoConverter().convertAndValidateFromModel(
                                    this.convertAuthentication( model.getRemoteStorage().getAuthentication(),
                                        oldPasswordForRemoteStorage ) );
                            RemoteConnectionSettings remoteConnSettings =
                                getGlobalRemoteConnectionSettings().convertAndValidateFromModel(
                                    this.convertRemoteConnectionSettings( model.getRemoteStorage()
                                        .getConnectionSettings() ) );
                            RemoteProxySettings httpProxySettings =
                                getGlobalHttpProxySettings().convertAndValidateFromModel(
                                    this.convertHttpProxySettings( model.getRemoteStorage().getHttpProxySettings(),
                                        oldPasswordForProxy ) );

                            if ( remoteAuth != null )
                            {
                                proxyRepo.setRemoteAuthenticationSettings( remoteAuth );
                            }
                            else
                            {
                                proxyRepo.getRemoteStorageContext().removeRemoteAuthenticationSettings();
                            }

                            if ( remoteConnSettings != null )
                            {
                                proxyRepo.setRemoteConnectionSettings( remoteConnSettings );
                            }
                            else
                            {
                                proxyRepo.getRemoteStorageContext().removeRemoteConnectionSettings();
                            }

                            if ( httpProxySettings != null )
                            {
                                proxyRepo.setRemoteProxySettings( httpProxySettings );
                            }
                            else
                            {
                                proxyRepo.getRemoteStorageContext().removeRemoteProxySettings();
                            }

                            // set auto block
                            proxyRepo.setAutoBlockActive( ( (RepositoryProxyResource) model ).isAutoBlockActive() );
                        }

                        if ( repository.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
                        {
                            RepositoryPolicy repoPolicy =
                                EnumUtil.valueOf( model.getRepoPolicy(), RepositoryPolicy.class );
                            repository.adaptToFacet( MavenRepository.class ).setRepositoryPolicy( repoPolicy );

                            if ( repository.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
                            {
                                ChecksumPolicy checksum =
                                    EnumUtil.valueOf( model.getChecksumPolicy(), ChecksumPolicy.class );

                                MavenProxyRepository pRepository = repository.adaptToFacet( MavenProxyRepository.class );
                                pRepository.setChecksumPolicy( checksum );

                                pRepository.setDownloadRemoteIndexes( model.isDownloadRemoteIndexes() );

                                pRepository.setChecksumPolicy( EnumUtil.valueOf( model.getChecksumPolicy(),
                                    ChecksumPolicy.class ) );

                                pRepository.setDownloadRemoteIndexes( model.isDownloadRemoteIndexes() );

                                RepositoryProxyResource proxyModel = (RepositoryProxyResource) model;

                                pRepository.setArtifactMaxAge( proxyModel.getArtifactMaxAge() );

                                pRepository.setMetadataMaxAge( proxyModel.getMetadataMaxAge() );
                            }
                        }
                        else
                        {
                            // This is a total hack to be able to retrieve this data from a non core repo if available
                            try
                            {
                                Method artifactMethod =
                                    repository.getClass().getMethod( "setArtifactMaxAge", int.class );
                                Method metadataMethod =
                                    repository.getClass().getMethod( "setMetadataMaxAge", int.class );

                                RepositoryProxyResource proxyModel = (RepositoryProxyResource) model;

                                if ( artifactMethod != null )
                                {
                                    artifactMethod.invoke( repository, proxyModel.getArtifactMaxAge() );
                                }
                                if ( metadataMethod != null )
                                {
                                    metadataMethod.invoke( repository, proxyModel.getMetadataMaxAge() );
                                }
                            }
                            catch ( Exception e )
                            {
                                // nothing to do here, doesn't support artifactmax age
                            }
                        }

                        repository.setLocalUrl( model.getOverrideLocalStorageUrl() );

                        getNexusConfiguration().saveConfiguration();
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
                }
            }
            catch ( ConfigurationException e )
            {
                handleConfigurationException( e );
            }
            catch ( StorageException e )
            {
                ErrorResponse nexusErrorResponse = getNexusErrorResponse( "*", e.getMessage() );
                throw new PlexusResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Configuration error.",
                                                   nexusErrorResponse );
            }
            catch ( IOException e )
            {
                getLogger().warn( "Got IO Exception!", e );

                throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
            }
        }

        // return current repo
        return this.getRepositoryResourceResponse( request, getRepositoryId( request ) );
    }

    /**
     * Delete an existing repository from nexus.
     * 
     * @param repositoryId The repository to access.
     */
    @Override
    @DELETE
    @ResourceMethodSignature( pathParams = { @PathParam( AbstractRepositoryPlexusResource.REPOSITORY_ID_KEY ) } )
    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        String repoId = this.getRepositoryId( request );
        try
        {
            getNexus().deleteRepository( repoId );

            response.setStatus( Status.SUCCESS_NO_CONTENT );
        }
        catch ( ConfigurationException e )
        {
            getLogger().warn( "Repository not deletable, it has dependants, id=" + repoId );

            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST,
                                         "Repository is not deletable, it has dependants." );
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
        catch ( IOException e )
        {
            getLogger().warn( "Got IO Exception!", e );

            throw new ResourceException( Status.SERVER_ERROR_INTERNAL );
        }
        catch ( AccessDeniedException e )
        {
            getLogger().warn( "Not allowed to delete repository '" + repoId + "'", e );

            throw new ResourceException( Status.CLIENT_ERROR_BAD_REQUEST, "Not allowed to delete repository '" + repoId
                + "'" );
        }
    }

}
