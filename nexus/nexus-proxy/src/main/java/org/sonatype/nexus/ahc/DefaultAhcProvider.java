package org.sonatype.nexus.ahc;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.repository.ClientSSLRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.NtlmRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.utils.UserAgentBuilder;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Realm;

@Component( role = AhcProvider.class )
public class DefaultAhcProvider
    implements AhcProvider
{
    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    @Requirement
    private UserAgentBuilder userAgentBuilder;

    private AsyncHttpClient sharedClient;

    @Override
    public synchronized AsyncHttpClient getAsyncHttpClient()
    {
        if ( sharedClient == null )
        {
            // TODO: nexus wide singleton or new instance per invocation?
            final Builder configBuilder =
                getAsyncHttpClientConfigBuilder( applicationConfiguration.getGlobalRemoteStorageContext() );

            configBuilder.setUserAgent( userAgentBuilder.formatGenericUserAgentString() );

            sharedClient = new AsyncHttpClient( configBuilder.build() );
        }

        return sharedClient;
    }

    @Override
    public Builder getAsyncHttpClient( final ProxyRepository repository, final RemoteStorageContext ctx )
    {
        final Builder result = getAsyncHttpClientConfigBuilder( ctx );

        result.setUserAgent( userAgentBuilder.formatRemoteRepositoryStorageUserAgentString( repository, ctx ) );

        // enable redirects for RRS use
        result.setFollowRedirects( true );

        return result;
    }

    // ==

    protected Builder getAsyncHttpClientConfigBuilder( final RemoteStorageContext ctx )
    {
        final AsyncHttpClientConfig.Builder result = new AsyncHttpClientConfig.Builder();

        // timeout
        final int timeout = ctx.getRemoteConnectionSettings().getConnectionTimeout();
        result.setConnectionTimeoutInMs( timeout );
        result.setRequestTimeoutInMs( timeout );
        
        // handle compression
        result.setCompressionEnabled( true );

        // remote auth
        RemoteAuthenticationSettings ras = ctx.getRemoteAuthenticationSettings();

        if ( ras != null )
        {
            Realm realm = null;

            if ( ras instanceof ClientSSLRemoteAuthenticationSettings )
            {
                // ClientSSLRemoteAuthenticationSettings cras = (ClientSSLRemoteAuthenticationSettings) ras;

                // TODO - implement this
            }
            else if ( ras instanceof NtlmRemoteAuthenticationSettings )
            {
                NtlmRemoteAuthenticationSettings nras = (NtlmRemoteAuthenticationSettings) ras;

                // rb.setNtlmHost( nras.getNtlmHost() )?
                realm =
                    new Realm.RealmBuilder().setPrincipal( nras.getUsername() ).setPassword( nras.getPassword() ).setDomain(
                        nras.getNtlmDomain() ).build();
            }
            else if ( ras instanceof UsernamePasswordRemoteAuthenticationSettings )
            {
                UsernamePasswordRemoteAuthenticationSettings uras = (UsernamePasswordRemoteAuthenticationSettings) ras;

                realm =
                    new Realm.RealmBuilder().setPrincipal( uras.getUsername() ).setPassword( uras.getPassword() ).setUsePreemptiveAuth(
                        true ).build();
            }

            if ( realm != null )
            {
                result.setRealm( realm );
            }
        }

        // proxy
        RemoteProxySettings rps = ctx.getRemoteProxySettings();

        if ( rps.isEnabled() )
        {
            ProxyServer proxy = null;

            if ( rps.getNonProxyHosts() != null && !rps.getNonProxyHosts().isEmpty() )
            {
                // set non-proxy hosts
            }

            if ( rps.getProxyAuthentication() != null )
            {
                ras = rps.getProxyAuthentication();

                if ( ras instanceof ClientSSLRemoteAuthenticationSettings )
                {
                    // ClientSSLRemoteAuthenticationSettings cras = (ClientSSLRemoteAuthenticationSettings) ras;

                    // TODO - implement this
                }
                else if ( ras instanceof NtlmRemoteAuthenticationSettings )
                {
                    // AHC-10
                    NtlmRemoteAuthenticationSettings nras = (NtlmRemoteAuthenticationSettings) ras;

                    // TODO: depends on AHC-10 for proper implementation, this below is fairly incomplete
                    proxy = new ProxyServer( rps.getHostname(), rps.getPort(), nras.getUsername(), nras.getPassword() );
                }
                else if ( ras instanceof UsernamePasswordRemoteAuthenticationSettings )
                {
                    UsernamePasswordRemoteAuthenticationSettings uras =
                        (UsernamePasswordRemoteAuthenticationSettings) ras;

                    proxy = new ProxyServer( rps.getHostname(), rps.getPort(), uras.getUsername(), uras.getPassword() );
                }
            }
            else
            {
                proxy = new ProxyServer( rps.getHostname(), rps.getPort() );
            }

            // to avoid NPEs while incomplete
            if ( proxy != null )
            {
                result.setProxyServer( proxy );
            }
        }

        return result;
    }
}
