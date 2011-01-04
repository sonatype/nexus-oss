/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationMessage;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.application.AuthenticationInfoConverter;
import org.sonatype.nexus.configuration.application.GlobalHttpProxySettings;
import org.sonatype.nexus.configuration.application.GlobalRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.validator.ApplicationValidationResponse;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public abstract class AbstractProxyRepositoryConfigurator
    extends AbstractRepositoryConfigurator
{
    @Requirement
    private AuthenticationInfoConverter authenticationInfoConverter;

    @Requirement
    private GlobalRemoteConnectionSettings globalRemoteConnectionSettings;

    @Requirement
    private GlobalHttpProxySettings globalHttpProxySettings;

    @Override
    public void doApplyConfiguration( Repository repository, ApplicationConfiguration configuration,
                                      CRepositoryCoreConfiguration coreConfig )
        throws ConfigurationException
    {
        super.doApplyConfiguration( repository, configuration, coreConfig );

        // proxy stuff, but is optional!

        // FIXME: hm, we are called when we are dirty, so....
        CRepository repo = coreConfig.getConfiguration( true );

        if ( repo.getRemoteStorage() != null )
        {
            // NOTE: we are intentionally _casting_ it, not calling adaptToFacet(), since repo implementation
            // still does not know that is should be a proxy repo!
            ProxyRepository prepository = (ProxyRepository) repository;

            try
            {
                if ( repo.getRemoteStorage() != null )
                {
                    RemoteRepositoryStorage oldRemoteStorage = prepository.getRemoteStorage();

                    RemoteRepositoryStorage configRemoteStorage =
                        getRemoteRepositoryStorage( repo.getId(), repo.getRemoteStorage().getProvider() );

                    // detect do we really need to set remote storage
                    if ( oldRemoteStorage == null || oldRemoteStorage != configRemoteStorage )
                    {
                        // validate the remoteUrl with new remote storage
                        configRemoteStorage.validateStorageUrl( repo.getRemoteStorage().getUrl() );

                        // set the chosen remote storage
                        prepository.setRemoteStorage( configRemoteStorage );
                    }
                    else
                    {
                        // just validate
                        oldRemoteStorage.validateStorageUrl( repo.getRemoteStorage().getUrl() );
                    }

                    if ( repo.getRemoteStorage().getAuthentication() != null )
                    {
                        prepository.setRemoteAuthenticationSettings( authenticationInfoConverter
                            .convertAndValidateFromModel( repo.getRemoteStorage().getAuthentication() ) );
                    }

                    if ( repo.getRemoteStorage().getConnectionSettings() != null )
                    {
                        prepository.setRemoteConnectionSettings( globalRemoteConnectionSettings
                            .convertAndValidateFromModel( repo.getRemoteStorage().getConnectionSettings() ) );
                    }

                    if ( repo.getRemoteStorage().getHttpProxySettings() != null )
                    {
                        if ( repo.getRemoteStorage().getHttpProxySettings().isBlockInheritance() )
                        {
                            prepository.setRemoteProxySettings( null );
                        }
                        else
                        {
                            prepository.setRemoteProxySettings( globalHttpProxySettings
                                .convertAndValidateFromModel( repo.getRemoteStorage().getHttpProxySettings() ) );
                        }
                    }
                }
                else
                {
                    prepository.setRemoteStorage( null );
                }
            }
            catch ( StorageException e )
            {
                ValidationResponse response = new ApplicationValidationResponse();

                ValidationMessage error = new ValidationMessage( "remoteStorageUrl", e.getMessage(), e.getMessage() );

                response.addValidationError( error );

                throw new InvalidConfigurationException( response );
            }
        }
    }

    @Override
    protected void doPrepareForSave( Repository repository, ApplicationConfiguration configuration,
                                     CRepositoryCoreConfiguration coreConfiguration )
    {
        super.doPrepareForSave( repository, configuration, coreConfiguration );

        if ( repository instanceof ProxyRepository )
        {
            // real cast needed here, adapt would return null!
            ProxyRepository prepository = (ProxyRepository) repository;

            // FIXME: hm, we are called when we are dirty, so....
            CRepository repoConfig = coreConfiguration.getConfiguration( true );

            if ( repoConfig.getRemoteStorage() != null )
            {
                RemoteStorageContext rsc = prepository.getRemoteStorageContext();

                if ( rsc.hasRemoteAuthenticationSettings() )
                {
                    repoConfig.getRemoteStorage().setAuthentication(
                        authenticationInfoConverter.convertToModel( rsc.getRemoteAuthenticationSettings() ) );
                }
                else
                {
                    repoConfig.getRemoteStorage().setAuthentication( null );
                }

                if ( rsc.hasRemoteConnectionSettings() )
                {
                    repoConfig.getRemoteStorage().setConnectionSettings(
                        globalRemoteConnectionSettings.convertToModel( rsc.getRemoteConnectionSettings() ) );
                }
                else
                {
                    repoConfig.getRemoteStorage().setConnectionSettings( null );
                }

                if ( rsc.hasRemoteProxySettings() )
                {
                    if ( rsc.getRemoteProxySettings() != null )
                    {
                        repoConfig.getRemoteStorage().setHttpProxySettings(
                            globalHttpProxySettings.convertToModel( rsc.getRemoteProxySettings() ) );
                    }
                    else
                    {
                        repoConfig.getRemoteStorage().setHttpProxySettings( new CRemoteHttpProxySettings() );

                        repoConfig.getRemoteStorage().getHttpProxySettings().setBlockInheritance( true );
                    }
                }
                else
                {
                    repoConfig.getRemoteStorage().setHttpProxySettings( null );
                }
            }
        }
    }

    protected RemoteRepositoryStorage getRemoteRepositoryStorage( String repoId, String provider )
        throws InvalidConfigurationException
    {
        try
        {
            return getPlexusContainer().lookup( RemoteRepositoryStorage.class, provider );
        }
        catch ( ComponentLookupException e )
        {
            throw new InvalidConfigurationException( "Repository " + repoId
                + " have remote storage with unsupported provider: " + provider, e );
        }
    }
}
