/*
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
package org.sonatype.nexus.proxy.storage.remote.commonshttpclient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.httpclient.CustomMultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.proxy.repository.ClientSSLRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.NtlmRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteHttpProxySettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.util.SystemPropertiesHelper;

/**
 * @deprecated Use httpclient4 components instead
 */
@Deprecated
public class HttpClientProxyUtil
{

    public static final String CONNECTION_POOL_SIZE_KEY = "httpClient.connectionPoolSize";

    public static final String NTLM_IS_IN_USE_KEY = "httpClient.ntlmIsInUse";

    private static final Logger LOGGER = LoggerFactory.getLogger( HttpClientProxyUtil.class );

    public static void applyProxyToHttpClient( HttpClient httpClient, RemoteStorageContext ctx, Logger logger )
    {
        httpClient.setHttpConnectionManager( new CustomMultiThreadedHttpConnectionManager() );

        // getting the timeout from RemoteStorageContext. The value we get depends on per-repo and global settings.
        // The value will "cascade" from repo level to global level, see imple of it.
        int timeout = ctx.getRemoteConnectionSettings().getConnectionTimeout();

        // getting the connection pool size, using a little trick to allow us "backdoor" to tune it using system
        // properties, but defaulting it to the same we had before (httpClient defaults)
        int connectionPoolSize =
            SystemPropertiesHelper.getInteger( CONNECTION_POOL_SIZE_KEY,
                MultiThreadedHttpConnectionManager.DEFAULT_MAX_TOTAL_CONNECTIONS );

        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout( timeout );
        httpClient.getHttpConnectionManager().getParams().setSoTimeout( timeout );
        // httpClient.getHttpConnectionManager().getParams().setTcpNoDelay( true );
        httpClient.getHttpConnectionManager().getParams().setMaxTotalConnections( connectionPoolSize );
        // NOTE: connPool is _per_ repo, hence all of those will connect to same host (unless mirrors are used)
        // so, we are violating intentionally the RFC and we let the whole pool size to chase same host
        httpClient.getHttpConnectionManager().getParams().setMaxConnectionsPerHost(
            HostConfiguration.ANY_HOST_CONFIGURATION, connectionPoolSize );

        // Setting auth if needed
        HostConfiguration httpConfiguration = httpClient.getHostConfiguration();

        // BASIC and DIGEST auth only
        RemoteAuthenticationSettings ras = ctx.getRemoteAuthenticationSettings();

        boolean isSimpleAuthUsed = false;
        boolean isNtlmUsed = false;

        if ( ras != null )
        {
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

                logger( logger ).info( "... authentication setup for NTLM domain '{}'", nras.getNtlmDomain() );

                httpConfiguration.setHost( nras.getNtlmHost() );

                httpClient.getState().setCredentials(
                    AuthScope.ANY,
                    new NTCredentials( nras.getUsername(), nras.getPassword(), nras.getNtlmHost(), nras.getNtlmDomain() ) );

                isNtlmUsed = true;
            }
            else if ( ras instanceof UsernamePasswordRemoteAuthenticationSettings )
            {
                UsernamePasswordRemoteAuthenticationSettings uras = (UsernamePasswordRemoteAuthenticationSettings) ras;

                // Using Username/Pwd auth, will not add NTLM
                logger( logger ).info( "... authentication setup for remote storage with username '{}'",
                    uras.getUsername() );

                httpClient.getState().setCredentials( AuthScope.ANY,
                    new UsernamePasswordCredentials( uras.getUsername(), uras.getPassword() ) );

                isSimpleAuthUsed = true;
            }

            httpClient.getParams().setParameter( AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs );
        }

        boolean isProxyUsed = false;

        final RemoteProxySettings rps = ctx.getRemoteProxySettings();
        if ( rps != null )
        {
            final RemoteHttpProxySettings rhps = rps.getHttpProxySettings();

            if ( rhps != null && rhps.isEnabled() )
            {
                isProxyUsed = true;

                logger( logger ).info( "... proxy setup with host '{}'", rhps.getHostname() );

                httpConfiguration.setProxy( rhps.getHostname(), rhps.getPort() );

                // check if we have non-proxy hosts
                if ( rps.getNonProxyHosts() != null && !rps.getNonProxyHosts().isEmpty() )
                {
                    Set<Pattern> nonProxyHostPatterns = new HashSet<Pattern>( rps.getNonProxyHosts().size() );
                    for ( String nonProxyHostRegex : rps.getNonProxyHosts() )
                    {
                        try
                        {
                            nonProxyHostPatterns.add( Pattern.compile( nonProxyHostRegex, Pattern.CASE_INSENSITIVE ) );
                        }
                        catch ( PatternSyntaxException e )
                        {
                            logger( logger ).warn( "Invalid non proxy host regex: {}", nonProxyHostRegex, e );
                        }
                    }
                    httpConfiguration.getParams().setParameter(
                        CustomMultiThreadedHttpConnectionManager.NON_PROXY_HOSTS_PATTERNS_KEY, nonProxyHostPatterns );
                }

                if ( rhps.getProxyAuthentication() != null )
                {
                    ras = rhps.getProxyAuthentication();

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
                            logger( logger ).warn(
                                "... Apache Commons HttpClient 3.x is unable to use NTLM auth scheme\n"
                                    + " for BOTH server side and proxy side authentication!\n"
                                    + " You MUST reconfigure server side auth and use BASIC/DIGEST scheme\n"
                                    + " if you have to use NTLM proxy, otherwise it will not work!\n"
                                    + " *** SERVER SIDE AUTH OVERRIDDEN" );
                        }

                        logger( logger ).info( "... proxy authentication setup for NTLM domain '{}'",
                                               nras.getNtlmDomain() );

                        httpConfiguration.setHost( nras.getNtlmHost() );

                        httpClient.getState().setProxyCredentials(
                            AuthScope.ANY,
                            new NTCredentials( nras.getUsername(), nras.getPassword(), nras.getNtlmHost(),
                                               nras.getNtlmDomain() ) );

                        isNtlmUsed = true;
                    }
                    else if ( ras instanceof UsernamePasswordRemoteAuthenticationSettings )
                    {
                        UsernamePasswordRemoteAuthenticationSettings uras =
                            (UsernamePasswordRemoteAuthenticationSettings) ras;

                        // Using Username/Pwd auth, will not add NTLM
                        logger( logger ).info( "... proxy authentication setup for remote storage with username '{}'",
                                               uras.getUsername() );

                        httpClient.getState().setProxyCredentials( AuthScope.ANY,
                                                                   new UsernamePasswordCredentials( uras.getUsername(),
                                                                                                    uras.getPassword() ) );
                    }

                    httpClient.getParams().setParameter( AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs );
                }
            }
        }

        // set preemptive only for simplest scenario:
        // no proxy and BASIC auth is used
        if ( isSimpleAuthUsed && !isProxyUsed )
        {
            logger( logger ).info(
                "... simple scenario: simple authentication used with no proxy in between target and us,"
                    + " will use preemptive authentication" );

            // we have authentication, let's do it preemptive
            httpClient.getParams().setAuthenticationPreemptive( true );
        }

        // mark the fact that NTLM is in use
        // but ONLY IF IT CHANGED!
        // Otherwise, doing it always, actually marks the ctx itself as "changed", causing an avalanche of other
        // consequences, like resetting all the HTTP clients of all remote storages (coz they think there is a change
        // in proxy or remote connection settings, etc).
        final Boolean isNtlmUsedOldValue = (Boolean) ctx.getContextObject( NTLM_IS_IN_USE_KEY );
        if ( isNtlmUsedOldValue == null || isNtlmUsedOldValue.booleanValue() != isNtlmUsed )
        {
            if ( isNtlmUsed )
            {
                ctx.putContextObject( NTLM_IS_IN_USE_KEY, Boolean.TRUE );
            }
            else
            {
                ctx.putContextObject( NTLM_IS_IN_USE_KEY, Boolean.FALSE );
            }
        }
    }

    private static Logger logger( final Logger logger )
    {
        if ( logger != null )
        {
            return logger;
        }
        return LOGGER;
    }

}
