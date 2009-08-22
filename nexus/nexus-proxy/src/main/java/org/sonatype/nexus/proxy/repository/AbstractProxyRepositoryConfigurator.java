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
                    RemoteRepositoryStorage rs =
                        getRemoteRepositoryStorage( repo.getId(), repo.getRemoteStorage().getProvider() );

                    rs.validateStorageUrl( repo.getRemoteStorage().getUrl() );

                    prepository.setRemoteStorage( rs );
                    
                    // the write policy on a proxy repo is read only
                    prepository.setWritePolicy( RepositoryWritePolicy.READ_ONLY );

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
                                                                     authenticationInfoConverter.convertToModel( rsc
                                                                         .getRemoteAuthenticationSettings() ) );
                }
                else
                {
                    repoConfig.getRemoteStorage().setAuthentication( null );
                }

                if ( rsc.hasRemoteConnectionSettings() )
                {
                    repoConfig.getRemoteStorage().setConnectionSettings(
                                                                         globalRemoteConnectionSettings
                                                                             .convertToModel( rsc
                                                                                 .getRemoteConnectionSettings() ) );
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
                                                                            globalHttpProxySettings.convertToModel( rsc
                                                                                .getRemoteProxySettings() ) );
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
