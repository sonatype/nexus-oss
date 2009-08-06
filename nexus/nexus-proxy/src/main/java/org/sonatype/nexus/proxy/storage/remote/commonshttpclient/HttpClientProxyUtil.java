package org.sonatype.nexus.proxy.storage.remote.commonshttpclient;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.repository.ClientSSLRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.NtlmRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public class HttpClientProxyUtil
{
    public static void applyProxyToHttpClient( HttpClient httpClient, RemoteStorageContext ctx, Logger logger )
    {
        httpClient.getParams().setConnectionManagerTimeout( ctx.getRemoteConnectionSettings().getConnectionTimeout() );
        httpClient.getParams().setSoTimeout( ctx.getRemoteConnectionSettings().getConnectionTimeout() );

        HostConfiguration httpConfiguration = httpClient.getHostConfiguration();

        // BASIC and DIGEST auth only
        RemoteAuthenticationSettings ras = ctx.getRemoteAuthenticationSettings();

        if ( ras != null )
        {
            // we have authentication, let's do it preemptive
            httpClient.getParams().setAuthenticationPreemptive( true );

            List<String> authPrefs = new ArrayList<String>( 2 );
            authPrefs.add( AuthPolicy.DIGEST );
            authPrefs.add( AuthPolicy.BASIC );

            if ( ras instanceof ClientSSLRemoteAuthenticationSettings )
            {
                // ClientSSLRemoteAuthenticationSettings cras = (ClientSSLRemoteAuthenticationSettings) ras;

                // TODO - implement this
            }
            else if ( ras instanceof NtlmRemoteAuthenticationSettings )
            {
                NtlmRemoteAuthenticationSettings nras = (NtlmRemoteAuthenticationSettings) ras;

                // Using NTLM auth, adding it as first in policies
                authPrefs.add( 0, AuthPolicy.NTLM );

                logger.info( "... authentication setup for NTLM domain {}" + nras.getNtlmDomain() );

                httpConfiguration.setHost( nras.getNtlmHost() );

                httpClient.getState().setCredentials(
                                                      AuthScope.ANY,
                                                      new NTCredentials( nras.getUsername(), nras.getPassword(), nras
                                                          .getNtlmHost(), nras.getNtlmDomain() ) );
            }
            else if ( ras instanceof UsernamePasswordRemoteAuthenticationSettings )
            {
                UsernamePasswordRemoteAuthenticationSettings uras = (UsernamePasswordRemoteAuthenticationSettings) ras;

                // Using Username/Pwd auth, will not add NTLM
                logger.info( "... authentication setup for remote storage with username " + uras.getUsername() );

                httpClient.getState().setCredentials(
                                                      AuthScope.ANY,
                                                      new UsernamePasswordCredentials( uras.getUsername(), uras
                                                          .getPassword() ) );
            }

            httpClient.getParams().setParameter( AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs );
        }

        RemoteProxySettings rps = ctx.getRemoteProxySettings();

        if ( rps.isEnabled() )
        {
            logger.info( "... proxy setup with host " + rps.getHostname() );

            httpConfiguration.setProxy( rps.getHostname(), rps.getPort() );

            if ( rps.getProxyAuthentication() != null )
            {
                ras = rps.getProxyAuthentication();
                
                List<String> authPrefs = new ArrayList<String>( 2 );
                authPrefs.add( AuthPolicy.DIGEST );
                authPrefs.add( AuthPolicy.BASIC );

                if ( ras instanceof ClientSSLRemoteAuthenticationSettings )
                {
                    // ClientSSLRemoteAuthenticationSettings cras = (ClientSSLRemoteAuthenticationSettings) ras;

                    // TODO - implement this
                }
                else if ( ras instanceof NtlmRemoteAuthenticationSettings )
                {
                    NtlmRemoteAuthenticationSettings nras = (NtlmRemoteAuthenticationSettings) ras;

                    // Using NTLM auth, adding it as first in policies
                    authPrefs.add( 0, AuthPolicy.NTLM );

                    if ( ctx.getRemoteAuthenticationSettings() != null
                        && ( ctx.getRemoteAuthenticationSettings() instanceof NtlmRemoteAuthenticationSettings ) )
                    {
                        logger.warn(
                                          "... Apache Commons HttpClient 3.x is unable to use NTLM auth scheme\n"
                                              + " for BOTH server side and proxy side authentication!\n"
                                              + " You MUST reconfigure server side auth and use BASIC/DIGEST scheme\n"
                                              + " if you have to use NTLM proxy, otherwise it will not work!\n"
                                              + " *** SERVER SIDE AUTH OVERRIDDEN" );
                    }

                    logger.info( "... proxy authentication setup for NTLM domain " + nras.getNtlmDomain() );

                    httpConfiguration.setHost( nras.getNtlmHost() );

                    httpClient.getState().setProxyCredentials(
                                                               AuthScope.ANY,
                                                               new NTCredentials( nras.getUsername(), nras
                                                                   .getPassword(), nras.getNtlmHost(), nras
                                                                   .getNtlmDomain() ) );
                }
                else if ( ras instanceof UsernamePasswordRemoteAuthenticationSettings )
                {
                    UsernamePasswordRemoteAuthenticationSettings uras =
                        (UsernamePasswordRemoteAuthenticationSettings) ras;

                    // Using Username/Pwd auth, will not add NTLM
                    logger.info(
                                      "... proxy authentication setup for remote storage with username "
                                          + uras.getUsername() );

                    httpClient.getState().setProxyCredentials(
                                                               AuthScope.ANY,
                                                               new UsernamePasswordCredentials( uras.getUsername(),
                                                                                                uras.getPassword() ) );
                }

                httpClient.getParams().setParameter( AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs );
            }

        }
    }
}
