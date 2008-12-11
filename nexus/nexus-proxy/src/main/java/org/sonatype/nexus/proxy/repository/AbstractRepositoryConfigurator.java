/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.repository;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.RepositoryStatusConverter;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.application.validator.ApplicationValidationResponse;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.plugins.PluginRepositoryConfigurator;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public abstract class AbstractRepositoryConfigurator
    implements RepositoryConfigurator
{
    @Requirement
    private RepositoryStatusConverter repositoryStatusConverter;

    @Requirement( role = PluginRepositoryConfigurator.class )
    private Map<String, PluginRepositoryConfigurator> pluginRepositoryConfigurators;

    public Repository updateRepositoryFromModel( Repository old, ApplicationConfiguration configuration,
        CRepository repo, RemoteStorageContext rsc, LocalRepositoryStorage ls, RemoteRepositoryStorage rs )
        throws InvalidConfigurationException
    {
        AbstractRepository repository = (AbstractRepository) old;

        repository.setId( repo.getId() );
        repository.setName( repo.getName() );
        repository.setLocalStatus( repositoryStatusConverter.localStatusFromModel( repo.getLocalStatus() ) );
        repository.setAllowWrite( repo.isAllowWrite() );
        repository.setBrowseable( repo.isBrowseable() );
        repository.setProxyMode( repositoryStatusConverter.proxyModeFromModel( repo.getProxyMode() ) );
        repository.setIndexable( repo.isIndexable() );
        repository.setNotFoundCacheTimeToLive( repo.getNotFoundCacheTTL() );
        repository.setUserManaged( repo.isUserManaged() );
        repository.setExposed( repo.isExposed() );
        repository.setNotFoundCacheActive( repo.isNotFoundCacheActive() );

        repository.setItemMaxAge( repo.getArtifactMaxAge() );

        // Setting common things on a repository

        // NX-198: filling up the default variable to store the "default" local URL
        File defaultStorageFile = new File( new File( configuration.getWorkingDirectory(), "storage" ), repository
            .getId() );

        try
        {
            repo.defaultLocalStorageUrl = defaultStorageFile.toURL().toString();
        }
        catch ( MalformedURLException e )
        {
            // will not happen, not user settable
            throw new InvalidConfigurationException( "Malformed URL for LocalRepositoryStorage!", e );
        }

        String localUrl = null;

        if ( repo.getLocalStorage() != null )
        {
            localUrl = repo.getLocalStorage().getUrl();
        }
        else
        {
            localUrl = repo.defaultLocalStorageUrl;

            // Default dir is going to be valid
            defaultStorageFile.mkdirs();
        }

        try
        {
            ls.validateStorageUrl( localUrl );

            repository.setLocalUrl( localUrl );
            repository.setLocalStorage( ls );
        }
        catch ( StorageException e )
        {
            ValidationResponse response = new ApplicationValidationResponse();

            ValidationMessage error = new ValidationMessage(
                "overrideLocalStorageUrl",
                "Repository has an invalid local storage URL '" + localUrl,
                "Invalid file location" );

            response.addValidationError( error );

            throw new InvalidConfigurationException( response );
        }

        try
        {
            if ( repo.getRemoteStorage() != null )
            {
                rs.validateStorageUrl( repo.getRemoteStorage().getUrl() );

                repository.setRemoteUrl( repo.getRemoteStorage().getUrl() );
                repository.setRemoteStorage( rs );

                DefaultRemoteStorageContext ctx = new DefaultRemoteStorageContext( rsc );

                if ( repo.getRemoteStorage().getConnectionSettings() != null )
                {
                    ctx.putRemoteConnectionContextObject( RemoteStorageContext.REMOTE_CONNECTIONS_SETTINGS, repo
                        .getRemoteStorage().getConnectionSettings() );
                }

                if ( repo.getRemoteStorage().getHttpProxySettings() != null )
                {
                    ctx.putRemoteConnectionContextObject( RemoteStorageContext.REMOTE_HTTP_PROXY_SETTINGS, repo
                        .getRemoteStorage().getHttpProxySettings() );
                }

                if ( repo.getRemoteStorage().getAuthentication() != null )
                {
                    ctx.putRemoteConnectionContextObject( RemoteStorageContext.REMOTE_AUTHENTICATION_SETTINGS, repo
                        .getRemoteStorage().getAuthentication() );
                }

                repository.setRemoteStorageContext( ctx );
            }
            else
            {
                repository.setRemoteUrl( null );
                
                repository.setRemoteStorage( null );
                
                repository.setRemoteStorageContext( null );
            }
        }
        catch ( StorageException e )
        {
            ValidationResponse response = new ApplicationValidationResponse();

            ValidationMessage error = new ValidationMessage( "remoteStorageUrl", e.getMessage(), e.getMessage() );

            response.addValidationError( error );

            throw new InvalidConfigurationException( response );
        }

        for ( PluginRepositoryConfigurator configurator : pluginRepositoryConfigurators.values() )
        {
            if ( configurator.isHandledRepository( repository ) )
            {
                configurator.configureRepository( repository );
            }
        }

        return repository;
    }
}
