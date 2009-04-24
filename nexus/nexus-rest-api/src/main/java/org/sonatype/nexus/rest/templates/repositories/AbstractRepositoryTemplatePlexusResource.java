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
package org.sonatype.nexus.rest.templates.repositories;

import java.util.Collection;

import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.v1_0_8.CRepositoryShadow;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.NexusCompat;
import org.sonatype.nexus.rest.global.AbstractGlobalConfigurationPlexusResource;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.rest.model.RepositoryListResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;

public abstract class AbstractRepositoryTemplatePlexusResource
    extends AbstractNexusPlexusResource
{
    /** Key to store Repo with which we work against. */
    public static final String REPOSITORY_ID_KEY = "repositoryId";

    /** Repo type hosted. */
    public static final String REPO_TYPE_HOSTED = "hosted";

    /** Repo type proxied. */
    public static final String REPO_TYPE_PROXIED = "proxy";

    /** Repo type virtual (shadow in nexus). */
    public static final String REPO_TYPE_VIRTUAL = "virtual";

    /** Repo type group. */
    public static final String REPO_TYPE_GROUP = "group";

    /**
     * Pull the repository Id out of the Request.
     * 
     * @param request
     * @return
     */
    protected String getRepositoryId( Request request )
    {
        return request.getAttributes().get( REPOSITORY_ID_KEY ).toString();
    }

    // CLEAN
    public String getRestRepoRemoteStatus( ProxyRepository repository, Request request, Response response )
        throws ResourceException
    {
        Form form = request.getResourceRef().getQueryAsForm();

        boolean forceCheck = form.getFirst( "forceCheck" ) != null;

        RemoteStatus rs = repository.getRemoteStatus( forceCheck );

        if ( RemoteStatus.UNKNOWN.equals( rs ) )
        {
            // set status to ACCEPTED, since we have incomplete info
            response.setStatus( Status.SUCCESS_ACCEPTED );
        }

        return rs == null ? null : rs.toString();
    }

    // clean
    public String getRestRepoType( Repository repository )
    {
        if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
        {
            return REPO_TYPE_PROXIED;
        }
        else if ( repository.getRepositoryKind().isFacetAvailable( HostedRepository.class ) )
        {
            return REPO_TYPE_HOSTED;
        }
        else if ( repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
        {
            return REPO_TYPE_VIRTUAL;
        }
        else if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            return REPO_TYPE_GROUP;
        }
        else
        {
            throw new IllegalArgumentException( "The passed model with class" + repository.getClass().getName()
                + " is not recognized!" );
        }
    }

    /**
     * Converting App model to REST DTO.
     */
    public RepositoryResource getRepositoryRestModel( CRepository repository )
    {
        RepositoryResource resource = null;
//
//        if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
//        {
//            resource = getRepositoryProxyRestModel( repository );
//        }
//        else
//        {
//            resource = new RepositoryResource();
//        }
//
//        resource.setProvider( NexusCompat.getRepositoryProviderHint( repository ) );
//
//        resource.setFormat( repository.getRepositoryContentClass().getId() );
//
//        resource.setRepoType( getRestRepoType( repository ) );
//
//        resource.setId( repository.getId() );
//
//        resource.setName( repository.getName() );
//
//        resource.setAllowWrite( repository.isAllowWrite() );
//
//        resource.setBrowseable( repository.isBrowseable() );
//
//        resource.setIndexable( repository.isIndexable() );
//
//        resource.setNotFoundCacheTTL( repository.getNotFoundCacheTimeToLive() );
//
//        resource.setDefaultLocalStorageUrl( repository.getLocalUrl() );
//
//        resource.setOverrideLocalStorageUrl( repository.getLocalUrl() );
//
//        if ( repository.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
//        {
//            resource.setRepoPolicy( repository.adaptToFacet( MavenRepository.class ).getRepositoryPolicy().toString() );
//
//            if ( repository.getRepositoryKind().isFacetAvailable( MavenProxyRepository.class ) )
//            {
//                resource.setChecksumPolicy( repository.adaptToFacet( MavenProxyRepository.class ).getChecksumPolicy().toString() );
//
//                resource.setDownloadRemoteIndexes( repository.adaptToFacet( MavenProxyRepository.class ).isDownloadRemoteIndexes() );
//            }
//        }

        return resource;

    }

    public RepositoryShadowResource getRepositoryShadowRestModel( CRepositoryShadow model )
    {
        RepositoryShadowResource resource = new RepositoryShadowResource();

        resource.setId( model.getId() );

        resource.setName( model.getName() );

        resource.setProvider( model.getType() );

        resource.setRepoType( REPO_TYPE_VIRTUAL );

        //resource.setFormat( getRepoFormat( ShadowRepository.class.getName(), model.getType() ) );

        resource.setShadowOf( model.getShadowOf() );

        resource.setSyncAtStartup( model.isSyncAtStartup() );

        return resource;
    }

    /**
     * Converting App model to REST DTO.
     */
    public RepositoryProxyResource getRepositoryProxyRestModel( CRepository repository )
    {
        RepositoryProxyResource resource = new RepositoryProxyResource();

        resource.setRemoteStorage( new RepositoryResourceRemoteStorage() );

        resource.getRemoteStorage().setRemoteStorageUrl( repository.getRemoteStorage().getUrl() );

        resource.getRemoteStorage().setAuthentication(
                                                       AbstractGlobalConfigurationPlexusResource.convert( repository.getRemoteStorage().getAuthentication() ) );

        resource.getRemoteStorage().setConnectionSettings(
                                                           AbstractGlobalConfigurationPlexusResource.convert( repository.getRemoteStorage().getConnectionSettings() ) );

        resource.getRemoteStorage().setHttpProxySettings(
                                                          AbstractGlobalConfigurationPlexusResource.convert( repository.getRemoteStorage().getHttpProxySettings() ) );

            //resource.setArtifactMaxAge( repository.adaptToFacet( MavenProxyRepository.class ).getArtifactMaxAge() );

            //resource.setMetadataMaxAge( repository.adaptToFacet( MavenProxyRepository.class ).getMetadataMaxAge() );

        return resource;
    }

    /**
     * Converting REST DTO + possible App model to App model. If app model is given, "update" happens, otherwise if
     * target is null, "create".
     * 
     * @param model
     * @param target
     * @return app model, merged or created
     */
    public CRepository getRepositoryAppModel( RepositoryResource model, CRepository target )
    {
        CRepository appModel = new CRepository();

        if ( target != null )
        {
            appModel.setLocalStatus( target.getLocalStatus() );
        }

        appModel.setId( model.getId() );

        appModel.setName( model.getName() );

        //appModel.setType( model.getProvider() );

        appModel.setAllowWrite( model.isAllowWrite() );

        appModel.setBrowseable( model.isBrowseable() );

        appModel.setIndexable( model.isIndexable() );

        appModel.setNotFoundCacheTTL( model.getNotFoundCacheTTL() );

        //appModel.setRepositoryPolicy( model.getRepoPolicy() );

        //appModel.setChecksumPolicy( model.getChecksumPolicy() );

        //appModel.setDownloadRemoteIndexes( model.isDownloadRemoteIndexes() );

        if ( model.getOverrideLocalStorageUrl() != null )
        {
            appModel.setLocalStorage( new CLocalStorage() );
            appModel.getLocalStorage().setUrl( model.getOverrideLocalStorageUrl() );
        }
        else
        {
            appModel.setLocalStorage( null );
        }

        if ( RepositoryProxyResource.class.isAssignableFrom( model.getClass() ) )
        {
            appModel = getRepositoryProxyAppModel( (RepositoryProxyResource) model, appModel );
        }

        return appModel;
    }

    /**
     * Converting REST DTO + possible App model to App model. If app model is given, "update" happens, otherwise if
     * target is null, "create".
     * 
     * @param model
     * @param target
     * @return app model, merged or created
     */
    public CRepository getRepositoryProxyAppModel( RepositoryProxyResource model, CRepository target )
    {
        //target.setArtifactMaxAge( model.getArtifactMaxAge() );

        //target.setMetadataMaxAge( model.getMetadataMaxAge() );

        if ( target.getRemoteStorage() == null )
        {
            target.setRemoteStorage( new CRemoteStorage() );
        }

        target.getRemoteStorage().setUrl( model.getRemoteStorage().getRemoteStorageUrl() );

        if ( model.getRemoteStorage().getAuthentication() != null )
        {
            if ( target.getRemoteStorage().getAuthentication() == null )
            {
                target.getRemoteStorage().setAuthentication( new CRemoteAuthentication() );
            }

            target.getRemoteStorage().getAuthentication().setUsername(
                                                                       model.getRemoteStorage().getAuthentication().getUsername() );

            target.getRemoteStorage().getAuthentication().setPassword(
                                                                       model.getRemoteStorage().getAuthentication().getPassword() );

            target.getRemoteStorage().getAuthentication().setNtlmDomain(
                                                                         model.getRemoteStorage().getAuthentication().getNtlmDomain() );

            target.getRemoteStorage().getAuthentication().setNtlmHost(
                                                                       model.getRemoteStorage().getAuthentication().getNtlmHost() );

            target.getRemoteStorage().getAuthentication().setPrivateKey(
                                                                         model.getRemoteStorage().getAuthentication().getPrivateKey() );

            target.getRemoteStorage().getAuthentication().setPassphrase(
                                                                         model.getRemoteStorage().getAuthentication().getPassphrase() );
        }
        else
        {
            target.getRemoteStorage().setAuthentication( null );
        }

        if ( model.getRemoteStorage().getConnectionSettings() != null )
        {
            if ( target.getRemoteStorage().getConnectionSettings() == null )
            {
                target.getRemoteStorage().setConnectionSettings( new CRemoteConnectionSettings() );
            }

            target.getRemoteStorage().getConnectionSettings().setConnectionTimeout(
                                                                                    model.getRemoteStorage().getConnectionSettings().getConnectionTimeout() * 1000 );

            target.getRemoteStorage().getConnectionSettings().setRetrievalRetryCount(
                                                                                      model.getRemoteStorage().getConnectionSettings().getRetrievalRetryCount() );

            target.getRemoteStorage().getConnectionSettings().setUserAgentCustomizationString(
                                                                                               model.getRemoteStorage().getConnectionSettings().getUserAgentString() );

            target.getRemoteStorage().getConnectionSettings().setQueryString(
                                                                              model.getRemoteStorage().getConnectionSettings().getQueryString() );
        }
        else
        {
            target.getRemoteStorage().setConnectionSettings( null );
        }

        if ( model.getRemoteStorage().getHttpProxySettings() != null )
        {
            if ( target.getRemoteStorage().getHttpProxySettings() == null )
            {
                target.getRemoteStorage().setHttpProxySettings( new CRemoteHttpProxySettings() );
            }

            target.getRemoteStorage().getHttpProxySettings().setProxyHostname(
                                                                               model.getRemoteStorage().getHttpProxySettings().getProxyHostname() );

            target.getRemoteStorage().getHttpProxySettings().setProxyPort(
                                                                           model.getRemoteStorage().getHttpProxySettings().getProxyPort() );

            if ( model.getRemoteStorage().getHttpProxySettings().getAuthentication() != null )
            {

                if ( target.getRemoteStorage().getHttpProxySettings().getAuthentication() == null )
                {
                    target.getRemoteStorage().getHttpProxySettings().setAuthentication( new CRemoteAuthentication() );
                }

                target.getRemoteStorage().getHttpProxySettings().getAuthentication().setUsername(
                                                                                                  model.getRemoteStorage().getHttpProxySettings().getAuthentication().getUsername() );

                target.getRemoteStorage().getHttpProxySettings().getAuthentication().setPassword(
                                                                                                  model.getRemoteStorage().getHttpProxySettings().getAuthentication().getPassword() );

                target.getRemoteStorage().getHttpProxySettings().getAuthentication().setNtlmDomain(
                                                                                                    model.getRemoteStorage().getHttpProxySettings().getAuthentication().getNtlmDomain() );

                target.getRemoteStorage().getHttpProxySettings().getAuthentication().setNtlmHost(
                                                                                                  model.getRemoteStorage().getHttpProxySettings().getAuthentication().getNtlmHost() );

                target.getRemoteStorage().getHttpProxySettings().getAuthentication().setPrivateKey(
                                                                                                    model.getRemoteStorage().getHttpProxySettings().getAuthentication().getPrivateKey() );

                target.getRemoteStorage().getHttpProxySettings().getAuthentication().setPassphrase(
                                                                                                    model.getRemoteStorage().getHttpProxySettings().getAuthentication().getPassphrase() );
            }

        }
        else
        {
            target.getRemoteStorage().setHttpProxySettings( null );
        }

        return target;
    }

}
