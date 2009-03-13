package org.sonatype.nexus.proxy.repository;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.modello.CMirror;
import org.sonatype.nexus.configuration.modello.CRepository;
import org.sonatype.nexus.configuration.validator.ApplicationValidationResponse;
import org.sonatype.nexus.configuration.validator.InvalidConfigurationException;
import org.sonatype.nexus.configuration.validator.ValidationMessage;
import org.sonatype.nexus.configuration.validator.ValidationResponse;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public class AbstractProxyRepositoryConfigurator
    extends AbstractRepositoryConfigurator
{
    public static final String PROXY_MODE = "proxyMode";

    public static final String ITEM_MAX_AGE = "itemMaxAge";

    /** The global remote storage context, without any parent. */
    private RemoteStorageContext globalRemoteStorageContext = new DefaultRemoteStorageContext( null );

    @Override
    public void doConfigure( Repository repository, ApplicationConfiguration configuration, CRepository repo,
        PlexusConfiguration externalConfiguration )
        throws ConfigurationException
    {
        super.doConfigure( repository, configuration, repo, externalConfiguration );

        // proxy stuff

        if ( repo.getRemoteStorage() != null )
        {
            ProxyRepository prepository = repository.adaptToFacet( ProxyRepository.class );

            prepository.setProxyMode( ProxyMode.valueOf( externalConfiguration.getChild( PROXY_MODE ).getValue(
                ProxyMode.ALLOW.toString() ) ) );

            prepository.setItemMaxAge( Integer.parseInt( externalConfiguration.getChild( ITEM_MAX_AGE ).getValue(
                String.valueOf( 1440 ) ) ) );

            try
            {
                if ( repo.getRemoteStorage() != null )
                {
                    RemoteRepositoryStorage rs = getRemoteRepositoryStorage( repo.getId(), repo
                        .getRemoteStorage().getProvider() );

                    rs.validateStorageUrl( repo.getRemoteStorage().getUrl() );

                    prepository.setRemoteUrl( repo.getRemoteStorage().getUrl() );
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

                    DefaultRemoteStorageContext ctx = new DefaultRemoteStorageContext( globalRemoteStorageContext );

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

                    prepository.setRemoteStorageContext( ctx );
                }
                else
                {
                    prepository.setRemoteUrl( null );

                    prepository.setRemoteStorage( null );

                    prepository.setRemoteStorageContext( null );
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
}
