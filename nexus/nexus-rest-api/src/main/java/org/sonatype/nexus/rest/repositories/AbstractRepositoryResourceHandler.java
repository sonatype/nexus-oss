/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.rest.repositories;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.configuration.model.CLocalStorage;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRemoteStorage;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryShadow;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.RemoteStatus;
import org.sonatype.nexus.rest.AbstractNexusResourceHandler;
import org.sonatype.nexus.rest.global.AbstractGlobalConfigurationResourceHandler;
import org.sonatype.nexus.rest.model.RepositoryProxyResource;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.rest.model.RepositoryResourceRemoteStorage;
import org.sonatype.nexus.rest.model.RepositoryShadowResource;

/**
 * Base class for Repository Resource Handlers.
 * 
 * @author cstamas
 */
public class AbstractRepositoryResourceHandler
    extends AbstractNexusResourceHandler
{

    /** Key to store Repo with which we work against. */
    public static final String REPOSITORY_ID_KEY = "repositoryId";

    /** Repo type hosted. */
    public static final String REPO_TYPE_HOSTED = "hosted";

    /** Repo type proxied. */
    public static final String REPO_TYPE_PROXIED = "proxy";

    /** Repo type virtual (shadow in nexus). */
    public static final String REPO_TYPE_VIRTUAL = "virtual";

    /**
     * Standard resource constructor.
     * 
     * @param context
     * @param request
     * @param response
     */
    public AbstractRepositoryResourceHandler( Context context, Request request, Response response )
    {
        super( context, request, response );
    }

    /**
     * Converting App model to REST DTO.
     */
    public RepositoryShadowResource getRepositoryShadowRestModel( CRepositoryShadow model )
    {
        RepositoryShadowResource resource = new RepositoryShadowResource();

        resource.setRepoType( REPO_TYPE_VIRTUAL );

        resource.setId( model.getId() );

        resource.setName( model.getName() );

        resource.setShadowOf( model.getShadowOf() );

        resource.setFormat( model.getType() );

        resource.setSyncAtStartup( model.isSyncAtStartup() );

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
    public CRepositoryShadow getRepositoryShadowAppModel( RepositoryShadowResource model, CRepositoryShadow target )
    {
        if ( target == null )
        {
            target = new CRepositoryShadow();

            target.setId( model.getId() );

            target.setLocalStatus( Configuration.LOCAL_STATUS_IN_SERVICE );
        }

        target.setName( model.getName() );

        target.setShadowOf( model.getShadowOf() );

        target.setSyncAtStartup( model.isSyncAtStartup() );

        target.setType( model.getFormat() );

        return target;
    }

    /**
     * Converting App model to REST DTO.
     */
    public RepositoryResource getRepositoryRestModel( CRepository model )
    {
        String repoType = getRestRepoType( model );
        RepositoryResource resource = null;
        if ( REPO_TYPE_PROXIED.equals( repoType ) )
        {
            resource = getRepositoryProxyRestModel( model );
        }
        else
        {
            resource = new RepositoryResource();
        }

        resource.setFormat( model.getType() );

        resource.setRepoType( repoType );

        resource.setId( model.getId() );

        resource.setName( model.getName() );

        resource.setAllowWrite( model.isAllowWrite() );

        resource.setBrowseable( model.isBrowseable() );

        resource.setIndexable( model.isIndexable() );

        resource.setNotFoundCacheTTL( model.getNotFoundCacheTTL() );

        resource.setDefaultLocalStorageUrl( model.defaultLocalStorageUrl );

        resource.setOverrideLocalStorageUrl( model.getLocalStorage() != null ? model.getLocalStorage().getUrl() : null );

        resource.setRepoPolicy( getRestRepoPolicy( model ) );

        resource.setChecksumPolicy( getRestChecksumPolicy( model ) );

        resource.setDownloadRemoteIndexes( model.isDownloadRemoteIndexes() );

        return resource;

    }

    /**
     * Converting App model to REST DTO.
     */
    public RepositoryProxyResource getRepositoryProxyRestModel( CRepository model )
    {
        RepositoryProxyResource resource = new RepositoryProxyResource();

        resource.setRemoteStorage( new RepositoryResourceRemoteStorage() );

        resource.getRemoteStorage().setRemoteStorageUrl( model.getRemoteStorage().getUrl() );

        resource.getRemoteStorage().setAuthentication(
            AbstractGlobalConfigurationResourceHandler.convert( model.getRemoteStorage().getAuthentication() ) );

        resource.getRemoteStorage().setConnectionSettings(
            AbstractGlobalConfigurationResourceHandler.convert( model.getRemoteStorage().getConnectionSettings() ) );

        resource.getRemoteStorage().setHttpProxySettings(
            AbstractGlobalConfigurationResourceHandler.convert( model.getRemoteStorage().getHttpProxySettings() ) );

        resource.setArtifactMaxAge( model.getArtifactMaxAge() );

        resource.setMetadataMaxAge( model.getMetadataMaxAge() );

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
        if ( target == null )
        {
            target = new CRepository();

            target.setId( model.getId() );
        }

        target.setName( model.getName() );

        target.setType( model.getFormat() );

        target.setAllowWrite( model.isAllowWrite() );

        target.setBrowseable( model.isBrowseable() );

        target.setIndexable( model.isIndexable() );

        target.setNotFoundCacheTTL( model.getNotFoundCacheTTL() );

        target.setRepositoryPolicy( model.getRepoPolicy() );

        target.setChecksumPolicy( model.getChecksumPolicy() );

        target.setDownloadRemoteIndexes( model.isDownloadRemoteIndexes() );

        if ( model.getOverrideLocalStorageUrl() != null )
        {
            if ( target.getLocalStorage() == null )
            {
                target.setLocalStorage( new CLocalStorage() );
            }

            target.getLocalStorage().setUrl( model.getOverrideLocalStorageUrl() );
        }
        else
        {
            target.setLocalStorage( null );
        }

        if ( RepositoryProxyResource.class.isAssignableFrom( model.getClass() ) )
        {
            target = getRepositoryProxyAppModel( (RepositoryProxyResource) model, target );
        }

        return target;
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
        target.setArtifactMaxAge( model.getArtifactMaxAge() );

        target.setMetadataMaxAge( model.getMetadataMaxAge() );

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

            target.getRemoteStorage().getConnectionSettings().setUserAgentString(
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

    public String getRestRepoType( Object model )
    {
        if ( CRepository.class.isAssignableFrom( model.getClass() ) )
        {
            CRepository m = (CRepository) model;

            if ( m.getRemoteStorage() != null && m.getRemoteStorage().getUrl() != null )
            {
                return AbstractRepositoryResourceHandler.REPO_TYPE_PROXIED;
            }
            else
            {
                return AbstractRepositoryResourceHandler.REPO_TYPE_HOSTED;
            }
        }
        else if ( CRepositoryShadow.class.isAssignableFrom( model.getClass() ) )
        {
            return AbstractRepositoryResourceHandler.REPO_TYPE_VIRTUAL;
        }
        else
        {
            throw new IllegalArgumentException( "The passed model with class" + model.getClass().getName()
                + " is not a Repository or RepositoryShadow!" );
        }
    }

    public String getRestRepoPolicy( Object model )
    {
        if ( CRepository.class.isAssignableFrom( model.getClass() ) )
        {
            CRepository m = (CRepository) model;

            return m.getRepositoryPolicy();
        }
        else
        {
            throw new IllegalArgumentException( "The passed model with class" + model.getClass().getName()
                + " is not a hosted or proxied Repository!" );
        }
    }

    public String getRestChecksumPolicy( Object model )
    {
        if ( CRepository.class.isAssignableFrom( model.getClass() ) )
        {
            CRepository m = (CRepository) model;

            return m.getChecksumPolicy();
        }
        else
        {
            throw new IllegalArgumentException( "The passed model with class" + model.getClass().getName()
                + " is not a hosted or proxied Repository!" );
        }
    }

    public String getRestRepoLocalStatus( Object model )
    {
        if ( CRepository.class.isAssignableFrom( model.getClass() ) )
        {
            CRepository m = (CRepository) model;

            return m.getLocalStatus();
        }
        else if ( CRepositoryShadow.class.isAssignableFrom( model.getClass() ) )
        {
            CRepositoryShadow m = (CRepositoryShadow) model;

            return m.getLocalStatus();
        }
        else
        {
            throw new IllegalArgumentException( "The passed model with class" + model.getClass().getName()
                + " is not a Repository or RepositoryShadow!" );
        }
    }

    public String getRestRepoRemoteStatus( Object model )
    {
        try
        {
            if ( CRepository.class.isAssignableFrom( model.getClass() ) )
            {
                Form form = getRequest().getResourceRef().getQueryAsForm();

                boolean forceCheck = form.getFirst( "forceCheck" ) != null;

                CRepository m = (CRepository) model;

                RemoteStatus rs = getNexus().getRepository( m.getId() ).getRemoteStatus( forceCheck );

                if ( RemoteStatus.UNKNOWN.equals( rs ) )
                {
                    // set status to ACCEPTED, since we have incomplete info
                    getResponse().setStatus( Status.SUCCESS_ACCEPTED );
                }

                return rs == null ? null : rs.toString();
            }
            else
            {
                return null;
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            return null;
        }
    }

    public String getRestRepoProxyMode( Object model )
    {
        if ( CRepository.class.isAssignableFrom( model.getClass() ) )
        {
            CRepository m = (CRepository) model;

            return m.getProxyMode().toString();
        }
        else
        {
            return null;
        }
    }

}
