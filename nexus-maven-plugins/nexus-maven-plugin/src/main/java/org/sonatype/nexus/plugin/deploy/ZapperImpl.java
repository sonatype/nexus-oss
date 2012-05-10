/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugin.deploy;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.spice.zapper.Client;
import org.sonatype.spice.zapper.IOSourceListable;
import org.sonatype.spice.zapper.Parameters;
import org.sonatype.spice.zapper.ParametersBuilder;
import org.sonatype.spice.zapper.client.ahc.AhcClientBuilder;
import org.sonatype.spice.zapper.fs.DirectoryIOSource;

import com.ning.http.client.ProxyServer;
import com.ning.http.client.ProxyServer.Protocol;
import com.ning.http.client.Realm;
import com.ning.http.client.Realm.AuthScheme;

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
            final ProxyServer proxyServer;
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
                    proxyServer =
                        new ProxyServer( protocol, zapperRequest.getProxyHost(), zapperRequest.getProxyPort(),
                            zapperRequest.getProxyUsername(), zapperRequest.getProxyPassword() );
                }
                else
                {
                    proxyServer =
                        new ProxyServer( protocol, zapperRequest.getProxyHost(), zapperRequest.getProxyPort() );
                }
            }
            else
            {
                proxyServer = null;
            }

            final Realm realm;
            if ( zapperRequest.getRemoteUsername() != null )
            {
                realm =
                    new Realm.RealmBuilder().setPrincipal( zapperRequest.getRemoteUsername() ).setPassword(
                        zapperRequest.getRemotePassword() ).setUsePreemptiveAuth( true ).setScheme( AuthScheme.BASIC ).build();
            }
            else
            {
                realm = null;
            }

            final Parameters parameters = ParametersBuilder.defaults().build();
            final AhcClientBuilder clientBuilder = new AhcClientBuilder( parameters, zapperRequest.getRemoteUrl() );
            if ( realm != null )
            {
                clientBuilder.withRealm( realm );
            }
            if ( proxyServer != null )
            {
                clientBuilder.withProxy( proxyServer );
            }
            final Client client = clientBuilder.build();
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
