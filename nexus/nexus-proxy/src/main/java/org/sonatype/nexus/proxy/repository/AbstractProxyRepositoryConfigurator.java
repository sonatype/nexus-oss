package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.ExternalConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CMirror;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.RemoteSettingsUtil;
import org.sonatype.nexus.configuration.validator.ApplicationValidationResponse;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public abstract class AbstractProxyRepositoryConfigurator
    extends AbstractRepositoryConfigurator
{
    @Override
    public void doApplyConfiguration( Repository repository, ApplicationConfiguration configuration, CRepository repo,
                                      ExternalConfiguration externalConfiguration )
        throws ConfigurationException
    {
        super.doApplyConfiguration( repository, configuration, repo, externalConfiguration );

        // proxy stuff, but is optional!

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

                    List<CMirror> mirrors = (List<CMirror>) repo.getRemoteStorage().getMirrors();

                    if ( mirrors != null && mirrors.size() > 0 )
                    {
                        List<Mirror> runtimeMirrors = new ArrayList<Mirror>();

                        for ( CMirror mirror : mirrors )
                        {
                            runtimeMirrors.add( new Mirror( mirror.getId(), mirror.getUrl() ) );
                        }

                        prepository.getDownloadMirrors().setMirrors( runtimeMirrors );
                    }
                    else
                    {
                        prepository.getDownloadMirrors().setMirrors( null );
                    }

                    if ( repo.getRemoteStorage().getAuthentication() != null )
                    {
                        prepository.setRemoteAuthenticationSettings( RemoteSettingsUtil.convertFromModel( repo
                            .getRemoteStorage().getAuthentication() ) );
                    }

                    if ( repo.getRemoteStorage().getConnectionSettings() != null )
                    {
                        prepository.setRemoteConnectionSettings( RemoteSettingsUtil.convertFromModel( repo.getRemoteStorage()
                            .getConnectionSettings() ) );
                    }

                    if ( repo.getRemoteStorage().getHttpProxySettings() != null )
                    {
                        if ( repo.getRemoteStorage().getHttpProxySettings().isBlockInheritance() )
                        {
                            prepository.setRemoteProxySettings( null );
                        }
                        else
                        {
                            prepository.setRemoteProxySettings( RemoteSettingsUtil.convertFromModel( repo.getRemoteStorage()
                                .getHttpProxySettings() ) );
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
                                     CRepository repoConfig, ExternalConfiguration externalConfiguration )
    {
        super.doPrepareForSave( repository, configuration, repoConfig, externalConfiguration );

        if ( repository instanceof ProxyRepository )
        {
            // real cast needed here, adapt would return null!
            ProxyRepository prepository = (ProxyRepository) repository;

            if ( repoConfig.getRemoteStorage() != null )
            {
                List<Mirror> mirrors = (List<Mirror>) prepository.getDownloadMirrors().getMirrors();

                if ( mirrors != null && mirrors.size() > 0 )
                {
                    List<CMirror> runtimeMirrors = new ArrayList<CMirror>();

                    for ( Mirror mirror : mirrors )
                    {
                        CMirror cmirror = new CMirror();

                        cmirror.setId( mirror.getId() );
                        cmirror.setUrl( mirror.getUrl() );
                        runtimeMirrors.add( cmirror );
                    }

                    repoConfig.getRemoteStorage().setMirrors( runtimeMirrors );
                }
                else
                {
                    repoConfig.getRemoteStorage().getMirrors().clear();
                }

                RemoteStorageContext rsc = prepository.getRemoteStorageContext();

                if ( rsc.hasRemoteAuthenticationSettings() )
                {
                    repoConfig.getRemoteStorage().setAuthentication(
                                                                     RemoteSettingsUtil.convertToModel( rsc
                                                                         .getRemoteAuthenticationSettings() ) );
                }
                else
                {
                    repoConfig.getRemoteStorage().setAuthentication( null );
                }

                if ( rsc.hasRemoteConnectionSettings() )
                {
                    repoConfig.getRemoteStorage().setConnectionSettings(
                                                                         RemoteSettingsUtil.convertToModel( rsc
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
                                                                            RemoteSettingsUtil.convertToModel( rsc
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
