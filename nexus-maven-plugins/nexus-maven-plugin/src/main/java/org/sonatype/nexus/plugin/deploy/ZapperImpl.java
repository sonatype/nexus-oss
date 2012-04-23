package org.sonatype.nexus.plugin.deploy;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
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
    public void deployDirectory( final String remoteUrl, final File directory )
        throws IOException
    {
        try
        {
            final Parameters parameters = new ParametersImpl();

            final Realm realm =
                new Realm.RealmBuilder().setPrincipal( "admin" ).setPassword( "admin123" ).setUsePreemptiveAuth( true ).setScheme(
                    AuthScheme.BASIC ).build();
            final AsyncHttpClientConfig config =
                new AsyncHttpClientConfig.Builder().setRealm( realm ).setIOThreadMultiplier( 3 ).build();
            final AsyncHttpClient asyncHttpClient = new AsyncHttpClient( config );
            final Client client = new AhcClient( parameters, remoteUrl, asyncHttpClient );
            final IOSourceListable deployables = new DirectoryIOSource( directory );

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
