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
package org.sonatype.nexus.error.reporting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.base.Preconditions;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.DefaultHttpRoutePlanner;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.jira.connector.internal.HttpClientConnector;
import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.proxy.repository.NtlmRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.utils.UserAgentBuilder;

/**
 *
 */
@Named
public class NexusPRConnector
    extends HttpClientConnector
    implements Disposable
{

    private static final Logger logger = LoggerFactory.getLogger( NexusPRConnector.class );
    
    private NexusConfiguration config;

    private final UserAgentBuilder uaBuilder;

    private DefaultHttpClient client;

    @Inject
    public NexusPRConnector( final NexusConfiguration config, final UserAgentBuilder uaBuilder )
    {
        this.config = config;
        this.uaBuilder = uaBuilder;
    }

    @Override
    protected HttpClient client()
    {
        if ( client == null )
        {
            client = (DefaultHttpClient) super.client();
        }

        // always configure with current proxy and params... settings may have changed
        client.setParams( createHttpParams( config.getGlobalRemoteStorageContext() ) );
        configureProxy( client, config.getGlobalRemoteStorageContext() );

        return client;
    }
    
    @Override
    protected HttpParams requestParams()
    {
        final HttpParams params = super.requestParams();
        HttpProtocolParams.setUserAgent( params, uaBuilder.formatUserAgentString( config.getGlobalRemoteStorageContext() ) + " (PROBLEMREPORTING)" );
        return params;
    }
    
    private static void configureProxy( final DefaultHttpClient httpClient,
                                        final RemoteStorageContext ctx )
    {
        final RemoteProxySettings rps = ctx.getRemoteProxySettings();

        if ( rps.isEnabled() )
        {
            final HttpHost proxy = new HttpHost( rps.getHostname(), rps.getPort() );
            httpClient.getParams().setParameter( ConnRoutePNames.DEFAULT_PROXY, proxy );

            // check if we have non-proxy hosts
            if ( rps.getNonProxyHosts() != null && !rps.getNonProxyHosts().isEmpty() )
            {
                final Set<Pattern> nonProxyHostPatterns = new HashSet<Pattern>( rps.getNonProxyHosts().size() );
                for ( String nonProxyHostRegex : rps.getNonProxyHosts() )
                {
                    try
                    {
                        nonProxyHostPatterns.add( Pattern.compile( nonProxyHostRegex, Pattern.CASE_INSENSITIVE ) );
                    }
                    catch ( PatternSyntaxException e )
                    {
                        logger.warn( "Invalid non proxy host regex: {}", nonProxyHostRegex, e );
                    }
                }
                httpClient.setRoutePlanner(
                    new NonProxyHostsAwareHttpRoutePlanner(
                        httpClient.getConnectionManager().getSchemeRegistry(), nonProxyHostPatterns
                    )
                );

            }

            configureAuthentication( httpClient, rps.getHostname(), rps.getPort(), rps.getProxyAuthentication() );
        }
    }

    private static void configureAuthentication( final DefaultHttpClient httpClient,
                                                 final String proxyHost,
                                                 final int proxyPort, 
                                                 final RemoteAuthenticationSettings ras )
    {
        if ( ras != null )
        {
            List<String> authorisationPreference = new ArrayList<String>( 2 );
            authorisationPreference.add( AuthPolicy.DIGEST );
            authorisationPreference.add( AuthPolicy.BASIC );

            Credentials credentials = null;

            if ( ras instanceof NtlmRemoteAuthenticationSettings )
            {
                final NtlmRemoteAuthenticationSettings nras = (NtlmRemoteAuthenticationSettings) ras;

                // Using NTLM auth, adding it as first in policies
                authorisationPreference.add( 0, AuthPolicy.NTLM );

                credentials = new NTCredentials(
                    nras.getUsername(), nras.getPassword(), nras.getNtlmHost(), nras.getNtlmDomain()
                );

            }
            else if ( ras instanceof UsernamePasswordRemoteAuthenticationSettings )
            {
                UsernamePasswordRemoteAuthenticationSettings uras = (UsernamePasswordRemoteAuthenticationSettings) ras;

                credentials = new UsernamePasswordCredentials( uras.getUsername(), uras.getPassword() );
            }

            if ( credentials != null )
            {
                httpClient.getCredentialsProvider().setCredentials( new AuthScope(proxyHost, proxyPort), credentials );
            }

            httpClient.getParams().setParameter( AuthPNames.PROXY_AUTH_PREF, authorisationPreference );
        }
    }

    private static HttpParams createHttpParams( final RemoteStorageContext ctx )
    {
        final HttpParams params = new BasicHttpParams();

        // getting the timeout from RemoteStorageContext. The value we get depends on per-repo and global settings.
        // The value will "cascade" from repo level to global level, see implementation.
        int timeout = ctx.getRemoteConnectionSettings().getConnectionTimeout();

        params.setParameter( CoreConnectionPNames.CONNECTION_TIMEOUT, timeout );
        params.setParameter( CoreConnectionPNames.SO_TIMEOUT, timeout );
        return params;
    }

    @Override
    public void dispose()
    {
        client.getConnectionManager().shutdown();
    }

    /**
     * An {@link org.apache.http.conn.routing.HttpRoutePlanner} that bypasses proxy for specific hosts.
     *
     * @since 2.0
     */
    static class NonProxyHostsAwareHttpRoutePlanner
        extends DefaultHttpRoutePlanner
    {
    
        // ----------------------------------------------------------------------
        // Implementation fields
        // ----------------------------------------------------------------------
    
        /**
         * Set of patterns for matching hosts names against. Never null.
         */
        private final Set<Pattern> nonProxyHostPatterns;
    
        // ----------------------------------------------------------------------
        // Constructors
        // ----------------------------------------------------------------------
    
        NonProxyHostsAwareHttpRoutePlanner( final SchemeRegistry schemeRegistry,
                                            final Set<Pattern> nonProxyHostPatterns )
        {
            super( schemeRegistry );
            this.nonProxyHostPatterns = Preconditions.checkNotNull( nonProxyHostPatterns );
        }
    
        // ----------------------------------------------------------------------
        // Public methods
        // ----------------------------------------------------------------------
    
        public HttpRoute determineRoute( final org.apache.http.HttpHost target,
                                         final HttpRequest request,
                                         final HttpContext context )
            throws HttpException
        {
            Object proxy = null;
            if ( noProxyFor( target.getHostName() ) )
            {
                proxy = request.getParams().getParameter( ConnRouteParams.DEFAULT_PROXY );
                if ( proxy != null )
                {
                    request.getParams().removeParameter( ConnRouteParams.DEFAULT_PROXY );
                }
            }
            try
            {
                return super.determineRoute( target, request, context );
            }
            finally
            {
                if ( proxy != null )
                {
                    request.getParams().setParameter( ConnRouteParams.DEFAULT_PROXY, proxy );
                }
            }
        }
    
        // ----------------------------------------------------------------------
        // Implementation methods
        // ----------------------------------------------------------------------
    
        private boolean noProxyFor( final String hostName )
        {
            for ( final Pattern nonProxyHostPattern : nonProxyHostPatterns )
            {
                if ( nonProxyHostPattern.matcher( hostName ).matches() )
                {
                    return true;
                }
            }
            return false;
        }
    
    }
}
