package org.sonatype.nexus.configuration.model;

import java.io.File;

import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.repository.ClientSSLRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.NtlmRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteConnectionSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;

public class RemoteSettingsUtil
{
    public static RemoteAuthenticationSettings convertFromModel( CRemoteAuthentication model )
    {
        if ( model != null )
        {
            if ( StringUtils.isNotBlank( model.getKeyStore() ) || StringUtils.isNotBlank( model.getTrustStore() ) )
            {
                return new ClientSSLRemoteAuthenticationSettings( new File( model.getTrustStore() ), model
                    .getTrustStorePassword(), new File( model.getKeyStore() ), model.getKeyStorePassword() );
            }
            else if ( StringUtils.isNotBlank( model.getNtlmDomain() ) )
            {
                return new NtlmRemoteAuthenticationSettings( model.getUsername(), model.getPassword(), model
                    .getNtlmDomain(), model.getNtlmHost() );
            }
            else
            {
                return new UsernamePasswordRemoteAuthenticationSettings( model.getUsername(), model.getPassword() );
            }
        }
        else
        {
            return null;
        }
    }

    public static RemoteConnectionSettings convertFromModel( CRemoteConnectionSettings model )
    {
        if ( model != null )
        {
            RemoteConnectionSettings remoteConnectionSettings = new RemoteConnectionSettings();

            remoteConnectionSettings.setConnectionTimeout( model.getConnectionTimeout() );

            remoteConnectionSettings.setQueryString( model.getQueryString() );

            remoteConnectionSettings.setRetrievalRetryCount( model.getRetrievalRetryCount() );

            remoteConnectionSettings.setUserAgentCustomizationString( model.getUserAgentCustomizationString() );

            return remoteConnectionSettings;
        }
        else
        {
            return null;
        }
    }

    public static RemoteProxySettings convertFromModel( CRemoteHttpProxySettings model )
    {
        if ( model != null )
        {
            if ( model.isBlockInheritance() )
            {
                return null;
            }
            
            RemoteProxySettings remoteProxySettings = new RemoteProxySettings();

            remoteProxySettings.setHostname( model.getProxyHostname() );

            remoteProxySettings.setPort( model.getProxyPort() );

            if ( model.getAuthentication() != null )
            {
                remoteProxySettings.setProxyAuthentication( convertFromModel( model.getAuthentication() ) );
            }
            else
            {
                remoteProxySettings.setProxyAuthentication( null );
            }

            return remoteProxySettings;
        }
        else
        {
            return null;
        }
    }

    public static CRemoteAuthentication convertToModel( RemoteAuthenticationSettings settings )
    {
        if ( settings == null )
        {
            return null;
        }
        else
        {
            CRemoteAuthentication remoteAuthentication = new CRemoteAuthentication();

            if ( settings instanceof NtlmRemoteAuthenticationSettings )
            {
                NtlmRemoteAuthenticationSettings up = (NtlmRemoteAuthenticationSettings) settings;

                remoteAuthentication.setUsername( up.getUsername() );

                remoteAuthentication.setPassword( up.getPassword() );

                remoteAuthentication.setNtlmDomain( up.getNtlmDomain() );

                remoteAuthentication.setNtlmHost( up.getNtlmHost() );
            }
            else if ( settings instanceof UsernamePasswordRemoteAuthenticationSettings )
            {
                UsernamePasswordRemoteAuthenticationSettings up =
                    (UsernamePasswordRemoteAuthenticationSettings) settings;

                remoteAuthentication.setUsername( up.getUsername() );

                remoteAuthentication.setPassword( up.getPassword() );
            }
            else if ( settings instanceof ClientSSLRemoteAuthenticationSettings )
            {
                ClientSSLRemoteAuthenticationSettings cs = (ClientSSLRemoteAuthenticationSettings) settings;

                remoteAuthentication.setKeyStore( cs.getKeyStore().getAbsolutePath() );

                remoteAuthentication.setKeyStorePassword( cs.getKeyStorePassword() );

                remoteAuthentication.setTrustStore( cs.getTrustStore().getAbsolutePath() );

                remoteAuthentication.setTrustStorePassword( cs.getTrustStorePassword() );
            }
            else
            {
                // ??
            }

            return remoteAuthentication;
        }
    }

    public static CRemoteConnectionSettings convertToModel( RemoteConnectionSettings settings )
    {
        if ( settings == null )
        {
            return null;
        }
        else
        {
            CRemoteConnectionSettings model = new CRemoteConnectionSettings();

            model.setConnectionTimeout( settings.getConnectionTimeout() );

            model.setQueryString( settings.getQueryString() );

            model.setRetrievalRetryCount( settings.getRetrievalRetryCount() );

            model.setUserAgentCustomizationString( settings.getUserAgentCustomizationString() );

            return model;
        }
    }

    public static CRemoteHttpProxySettings convertToModel( RemoteProxySettings settings )
    {
        if ( settings == null )
        {
            return null;
        }
        else
        {
            CRemoteHttpProxySettings model = new CRemoteHttpProxySettings();

            model.setProxyHostname( settings.getHostname() );

            model.setProxyPort( settings.getPort() );

            model.setAuthentication( convertToModel( settings.getProxyAuthentication() ) );

            return model;
        }
    }
}
