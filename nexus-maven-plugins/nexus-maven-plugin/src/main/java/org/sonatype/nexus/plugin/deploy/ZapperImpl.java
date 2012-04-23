package org.sonatype.nexus.plugin.deploy;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.ProxyServer.Protocol;
import com.ning.http.client.Realm;
import com.ning.http.client.Realm.AuthScheme;

import eu.flatwhite.zapper.Client;
import eu.flatwhite.zapper.IOSourceListable;
import eu.flatwhite.zapper.Parameters;
import eu.flatwhite.zapper.client.ahc.AhcClient;
import eu.flatwhite.zapper.fs.DirectoryIOSource;
import eu.flatwhite.zapper.internal.ParametersImpl;

@Component( role = Zapper.class )
public class ZapperImpl
    implements Zapper
{

    @Override
    public void deployDirectory( final ZapperRequest zapperRequest )
        throws IOException
    {
        try
        {
            AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();

            if ( zapperRequest.getProxyProtocol() != null )
            {
                Protocol protocol;
                if ( "http".equalsIgnoreCase( zapperRequest.getProxyProtocol() ) )
                {
                    protocol = Protocol.HTTP;
                }
                else if ( "https".equalsIgnoreCase( zapperRequest.getProxyProtocol() ) )
                {
                    protocol = Protocol.HTTPS;
                }
                else
                {
                    throw new IllegalArgumentException( "Unsupported HTTP proxy protocol: "
                        + zapperRequest.getProxyProtocol() );
                }

                if ( zapperRequest.getProxyUsername() != null )
                {
                    builder.setProxyServer( new ProxyServer( protocol, zapperRequest.getProxyHost(),
                        zapperRequest.getProxyPort(), zapperRequest.getProxyUsername(),
                        zapperRequest.getProxyPassword() ) );
                }
                else
                {
                    builder.setProxyServer( new ProxyServer( protocol, zapperRequest.getProxyHost(),
                        zapperRequest.getProxyPort() ) );
                }
            }

            if ( zapperRequest.getRemoteUsername() != null )
            {
                builder.setRealm( new Realm.RealmBuilder().setPrincipal( zapperRequest.getRemoteUsername() ).setPassword(
                    zapperRequest.getRemotePassword() ).setUsePreemptiveAuth( true ).setScheme( AuthScheme.BASIC ).build() );
            }

            final AsyncHttpClient asyncHttpClient = new AsyncHttpClient( builder.setIOThreadMultiplier( 3 ).build() );

            final Parameters parameters = new ParametersImpl();
            final Client client = new AhcClient( parameters, zapperRequest.getRemoteUrl(), asyncHttpClient );
            final IOSourceListable deployables = new DirectoryIOSource( zapperRequest.getStageRepository() );

            try
            {
                client.upload( deployables );
            }
            finally
            {
                client.close();
            }
        }
        catch ( IOException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new IOException( "Unable to deploy!", e );
        }
    }
}
