/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.sonatype.nexus.index.updater.jetty;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.codehaus.plexus.util.IOUtil;
import org.eclipse.jetty.http.HttpFields;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

public class NtlmConnectionHelper
{
    private final JettyResourceFetcher resourceFetcher;

    private HttpURLConnection urlConnection;

    private String previousProxyExclusions;

    private String previousHttpProxyHost;

    private String previousHttpProxyPort;

    NtlmConnectionHelper( final JettyResourceFetcher fetcher )
    {
        resourceFetcher = fetcher;
    }

    public void send( final ResourceExchange exchange )
    {
        try
        {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append( exchange.getScheme().toString() );
            urlBuilder.append( "://" );
            urlBuilder.append( exchange.getAddress().toString() );
            urlBuilder.append( exchange.getURI().replace( "//", "/" ) );
            URL url = new URL( urlBuilder.toString() );

            String method = exchange.getMethod();

            setupConnection( url );

            if ( method.equalsIgnoreCase( "GET" ) )
            {
                doGet( url, exchange, true );
            }
            else if ( method.equalsIgnoreCase( "HEAD" ) )
            {
                doGet( url, exchange, false );
            }

            closeConnection();
        }
        catch ( Exception e )
        {
        }
    }

    public void doGet( final URL url, final ResourceExchange exchange, final boolean doGet )
        throws Exception
    {
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty( "Accept-Encoding", "gzip" );
        if ( !resourceFetcher.isUseCache() )
        {
            urlConnection.setRequestProperty( "Pragma", "no-cache" );
        }

        addHeaders( urlConnection );

        if ( doGet )
        {
            urlConnection.setRequestMethod( "GET" );
        }
        else
        {
            urlConnection.setRequestMethod( "HEAD" );
        }

        int responseCode = urlConnection.getResponseCode();
        if ( responseCode == HttpURLConnection.HTTP_FORBIDDEN || responseCode == HttpURLConnection.HTTP_UNAUTHORIZED )
        {
            throw new AuthorizationException( "Access denied to: " + url );
        }

        if ( doGet )
        {
            InputStream is = urlConnection.getInputStream();
            String contentEncoding = urlConnection.getHeaderField( "Content-Encoding" );
            boolean isGZipped = contentEncoding == null ? false : "gzip".equalsIgnoreCase( contentEncoding );
            if ( isGZipped )
            {
                is = new GZIPInputStream( is );
            }

            ByteArrayOutputStream content = new ByteArrayOutputStream();
            IOUtil.copy( is, content );
            exchange.setResponseContentBytes( content.toByteArray() );
        }

        exchange.setLastModified( urlConnection.getLastModified() );
        exchange.setContentLength( urlConnection.getContentLength() );
        exchange.setResponseStatus( responseCode );
    }

    private void addHeaders( final URLConnection urlConnection )
    {
        HttpFields httpHeaders = resourceFetcher.getHttpHeaders();
        if ( httpHeaders != null )
        {
            for ( Enumeration<String> names = httpHeaders.getFieldNames(); names.hasMoreElements(); )
            {
                String name = names.nextElement();
                urlConnection.setRequestProperty( name, httpHeaders.getStringField( name ) );
            }
        }
    }

    protected void setupConnection( final URL url )
        throws ConnectionException, AuthenticationException
    {
        previousHttpProxyHost = System.getProperty( "http.proxyHost" );
        previousHttpProxyPort = System.getProperty( "http.proxyPort" );
        previousProxyExclusions = System.getProperty( "http.nonProxyHosts" );

        final ProxyInfo proxyInfo = resourceFetcher.getProxyInfo();
        if ( proxyInfo != null )
        {
            System.setProperty( "http.proxyHost", proxyInfo.getHost() );
            System.setProperty( "http.proxyPort", String.valueOf( proxyInfo.getPort() ) );
            if ( proxyInfo.getNonProxyHosts() != null )
            {
                System.setProperty( "http.nonProxyHosts", proxyInfo.getNonProxyHosts() );
            }
            else
            {
                System.getProperties().remove( "http.nonProxyHosts" );
            }
        }
        else
        {
            System.getProperties().remove( "http.proxyHost" );
            System.getProperties().remove( "http.proxyPort" );
        }

        AuthenticationInfo authenticationInfo = resourceFetcher.getAuthenticationInfo();
        final boolean hasProxy = ( proxyInfo != null && proxyInfo.getUserName() != null );
        final boolean hasAuthentication = ( authenticationInfo != null && authenticationInfo.getUserName() != null );
        if ( hasProxy || hasAuthentication )
        {
            Authenticator.setDefault( new Authenticator()
            {
                @Override
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    if ( hasProxy && getRequestorType() == RequestorType.PROXY )
                    {
                        String password = "";
                        if ( proxyInfo.getPassword() != null )
                        {
                            password = proxyInfo.getPassword();
                        }

                        return new PasswordAuthentication( proxyInfo.getUserName(), password.toCharArray() );
                    }

                    if ( hasAuthentication )
                    {
                        String password = "";
                        AuthenticationInfo authenticationInfo = resourceFetcher.getAuthenticationInfo();
                        if ( authenticationInfo.getPassword() != null )
                        {
                            password = authenticationInfo.getPassword();
                        }

                        return new PasswordAuthentication( authenticationInfo.getUserName(), password.toCharArray() );
                    }

                    return super.getPasswordAuthentication();
                }
            } );
        }
        else
        {
            Authenticator.setDefault( null );
        }
    }

    public void closeConnection()
        throws ConnectionException
    {
        if ( urlConnection != null )
        {
            urlConnection.disconnect();
        }
        if ( previousHttpProxyHost != null )
        {
            System.setProperty( "http.proxyHost", previousHttpProxyHost );
        }
        else
        {
            System.getProperties().remove( "http.proxyHost" );
        }
        if ( previousHttpProxyPort != null )
        {
            System.setProperty( "http.proxyPort", previousHttpProxyPort );
        }
        else
        {
            System.getProperties().remove( "http.proxyPort" );
        }
        if ( previousProxyExclusions != null )
        {
            System.setProperty( "http.nonProxyHosts", previousProxyExclusions );
        }
        else
        {
            System.getProperties().remove( "http.nonProxyHosts" );
        }
    }
}
