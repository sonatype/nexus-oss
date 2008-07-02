/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.proxy.storage.remote.apachehttp;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.params.HttpConnectionManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.remote.AbstractRemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

/**
 * The Class ApacheHttpClientRemoteStorage.
 * 
 * @author cstamas
 * @plexus.component role-hint="apacheHttpClient4x"
 */
public class ApacheHttpClientRemoteStorage
    extends AbstractRemoteRepositoryStorage
    implements RemoteRepositoryStorage
{

    public static final String CTX_KEY = "apacheHttpClient4x";

    public static final String CTX_KEY_CLIENT = CTX_KEY + ".client";

    public static final String CTX_KEY_PROXY_HOST = CTX_KEY + ".proxyHost";

    // ===============================================================================
    // RemoteStorage iface

    public boolean containsItem( RepositoryItemUid uid, long newerThen )
        throws StorageException
    {
        try
        {
            URL targetUrl = getAbsoluteUrlFromBase( uid );

            HttpHost target = new HttpHost( targetUrl.getHost(), targetUrl.getPort(), targetUrl.getProtocol() );

            HttpHead head = new HttpHead( targetUrl.getPath() );

            HttpResponse response = executeMethod( uid.getRepository(), target, head );
            
            //In case the remote repository doesn't support the HEAD method, we will attempt
            //a GET
            if ( HttpStatus.SC_OK != response.getStatusLine().getStatusCode() )
            {
                HttpGet get = new HttpGet( targetUrl.getPath() );
                
                response = executeMethod( uid.getRepository(), target, get );
            }

            return HttpStatus.SC_OK == response.getStatusLine().getStatusCode()
                && makeDateFromHeader( response.getFirstHeader( "last-modified" ) ) > newerThen;
        }
        catch ( URISyntaxException e )
        {
            throw new StorageException( "Repository URL probably malformed!", e );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.storage.remote.RemoteRepositoryStorage#deleteItem(org.sonatype.nexus.item.RepositoryItemUid)
     */
    public void deleteItem( RepositoryItemUid uid )
        throws ItemNotFoundException,
            UnsupportedStorageOperationException,
            StorageException
    {
        // TODO: do a HTTP OPTION and discover is DELETE allowed
        throw new UnsupportedStorageOperationException( "This operation is not supported on "
            + getAbsoluteUrlFromBase( uid ) );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.storage.remote.RemoteRepositoryStorage#retrieveItem(org.sonatype.nexus.item.RepositoryItemUid)
     */
    public AbstractStorageItem retrieveItem( RepositoryItemUid uid )
        throws ItemNotFoundException,
            StorageException
    {
        try
        {
            URL targetUrl = getAbsoluteUrlFromBase( uid );
            HttpHost target = new HttpHost( targetUrl.getHost(), targetUrl.getPort(), targetUrl.getProtocol() );

            HttpGet method = new HttpGet( targetUrl.getPath() );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Fetching from remote location " + getAbsoluteUrlFromBase( uid ) );
            }

            HttpResponse response = executeMethod( uid.getRepository(), target, method );
            if ( response.getStatusLine().getStatusCode() == HttpStatus.SC_OK )
            {

                if ( method.getURI().getPath().endsWith( "/" ) )
                {
                    // this is a collection and not a file!
                    // httpClient will follow redirections, and the getPath()
                    // _should_
                    // give us URL with ending "/"
                    throw new ItemNotFoundException( uid );
                }

                InputStream is = null;
                try
                {
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();

                    DefaultStorageFileItem httpItem = new DefaultStorageFileItem(
                        uid.getRepository(),
                        uid.getPath(),
                        true,
                        true,
                        new PreparedContentLocator( new ApacheHttpClientInputStream( entity, is ) ) );

                    httpItem.setRemoteUrl( getAbsoluteUrlFromBase( uid ).toString() );

                    if ( entity.getContentLength() != -1 )
                    {
                        // FILE
                        httpItem.setLength( entity.getContentLength() );
                    }
                    if ( entity.getContentType() != null )
                    {
                        httpItem.setMimeType( entity.getContentType().getValue() );
                    }
                    httpItem.setModified( makeDateFromHeader( response.getFirstHeader( "last-modified" ) ) );
                    httpItem.setCreated( httpItem.getModified() );
                    return httpItem;

                }
                catch ( IOException ex )
                {
                    throw new StorageException( "IO Error during response stream handling!", ex );
                }
                catch ( RuntimeException ex )
                {
                    method.abort();
                    throw ex;
                }
            }
            else
            {
                method.abort();
                if ( response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND )
                {
                    throw new ItemNotFoundException( getAbsoluteUrlFromBase( uid ).toString() );
                }
                else
                {
                    throw new StorageException( "The method execution returned result code " + response );
                }
            }
        }
        catch ( URISyntaxException e )
        {
            throw new StorageException( "Repository URL probably malformed!", e );
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.storage.remote.RemoteRepositoryStorage#storeItem(org.sonatype.nexus.item.AbstractStorageItem)
     */
    public void storeItem( AbstractStorageItem item )
        throws UnsupportedStorageOperationException,
            StorageException
    {
        // TODO: do a HTTP OPTION and discover is PUT allowed
        throw new UnsupportedStorageOperationException( "This operation is not supported on "
            + getAbsoluteUrlFromBase( item.getRepositoryItemUid() ) );
    }

    protected void updateContext( RemoteStorageContext ctx )
    {
        DefaultHttpClient httpClient = null;

        HttpHost proxyHttpHost = null;

        getLogger().info( "Creating ApacheHttpClient instance" );

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionManagerParams.setMaxTotalConnections( httpParams, 20 );
        HttpProtocolParams.setVersion( httpParams, HttpVersion.HTTP_1_1 );

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register( new Scheme( "http", PlainSocketFactory.getSocketFactory(), 80 ) );
        schemeRegistry.register( new Scheme( "https", SSLSocketFactory.getSocketFactory(), 443 ) );

        ClientConnectionManager cm = new ThreadSafeClientConnManager( httpParams, schemeRegistry );
        HttpRequestRetryHandler httpRequestRetryHandler = new DefaultHttpRequestRetryHandler( ctx
            .getRemoteConnectionSettings().getRetrievalRetryCount(), true );

        httpClient = new DefaultHttpClient( cm, httpParams );
        httpClient.setHttpRequestRetryHandler( httpRequestRetryHandler );

        httpClient.addRequestInterceptor( new HttpRequestInterceptor()
        {

            public void process( final HttpRequest request, final HttpContext context )
                throws HttpException,
                    IOException
            {
                if ( !request.containsHeader( "Accept-Encoding" ) )
                {
                    request.addHeader( "Accept-Encoding", "gzip" );
                }
            }

        } );

        httpClient.addResponseInterceptor( new HttpResponseInterceptor()
        {

            public void process( final HttpResponse response, final HttpContext context )
                throws HttpException,
                    IOException
            {
                HttpEntity entity = response.getEntity();
                Header ceheader = entity.getContentEncoding();
                if ( ceheader != null )
                {
                    HeaderElement[] codecs = ceheader.getElements();
                    for ( int i = 0; i < codecs.length; i++ )
                    {
                        if ( codecs[i].getName().equalsIgnoreCase( "gzip" ) )
                        {
                            response.setEntity( new GzipDecompressingEntity( response.getEntity() ) );
                            return;
                        }
                    }
                }
            }

        } );

        // BASIC and DIGEST auth only
        if ( ctx.getRemoteAuthenticationSettings() != null
            && ctx.getRemoteAuthenticationSettings().getUsername() != null )
        {
            getLogger().info(
                "... setting authentication setup for remote storage with username "
                    + ctx.getRemoteAuthenticationSettings().getUsername() );

            httpClient.getCredentialsProvider().setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials( ctx.getRemoteAuthenticationSettings().getUsername(), ctx
                    .getRemoteAuthenticationSettings().getPassword() ) );
        }
        else if ( ctx.getRemoteAuthenticationSettings().getNtlmDomain() != null
            || ctx.getRemoteAuthenticationSettings().getNtlmHost() != null )
        {
            getLogger().warn( "NTLM authentication is not supported by " + this.getClass().getName() );
        }

        if ( ctx.getRemoteHttpProxySettings() != null && ctx.getRemoteHttpProxySettings().getProxyHostname() != null )
        {
            getLogger().info( "... proxy setup with host " + ctx.getRemoteHttpProxySettings().getProxyHostname() );
            proxyHttpHost = new HttpHost( ctx.getRemoteHttpProxySettings().getProxyHostname(), ctx
                .getRemoteHttpProxySettings().getProxyPort() );

            if ( ctx.getRemoteHttpProxySettings().getAuthentication().getUsername() != null )
            {
                getLogger().info(
                    "... setting authentication setup for HTTP proxy with username "
                        + ctx.getRemoteHttpProxySettings().getAuthentication().getUsername() );

                httpClient.getCredentialsProvider().setCredentials(
                    new AuthScope( proxyHttpHost.getHostName(), proxyHttpHost.getPort() ),
                    new UsernamePasswordCredentials(
                        ctx.getRemoteHttpProxySettings().getAuthentication().getUsername(),
                        ctx.getRemoteHttpProxySettings().getAuthentication().getPassword() ) );
            }
            else if ( ctx.getRemoteHttpProxySettings().getAuthentication().getNtlmDomain() != null
                || ctx.getRemoteHttpProxySettings().getAuthentication().getNtlmHost() != null )
            {
                getLogger().warn( "NTLM proxy authentication is not supported by " + this.getClass().getName() );
            }

            httpClient.getParams().setParameter( ConnRoutePNames.DEFAULT_PROXY, proxyHttpHost );
        }
        else
        {
            proxyHttpHost = null;
        }

        ctx.getRemoteConnectionContext().put( CTX_KEY_CLIENT, httpClient );

        ctx.getRemoteConnectionContext().put( CTX_KEY_PROXY_HOST, proxyHttpHost );
    }

    /**
     * Execute method.
     * 
     * @param target the target
     * @param request the request
     * @return the http response
     */
    protected HttpResponse executeMethod( Repository repository, HttpHost target, HttpUriRequest request )
    {
        getLogger().debug( "HTTPClient :: executeMethod() ENTER" );

        RemoteStorageContext ctx = getRemoteStorageContext( repository );

        HttpClient httpClient = (HttpClient) ctx.getRemoteConnectionContext().get( CTX_KEY_CLIENT );

        if ( ctx.getRemoteConnectionSettings().getUserAgentString() != null )
        {
            request.setHeader( new BasicHeader( "user-agent", ctx.getRemoteConnectionSettings().getUserAgentString() ) );
        }
        // request.setHeader( new BasicHeader( "accept", "*/*" ) );
        // request.setHeader( new BasicHeader( "accept-language", "en-us" ) );
        // request.setHeader( new BasicHeader( "connection", "Keep-Alive" ) );
        // request.setHeader( new BasicHeader( "cache-control", "no-cache" ) );

        HttpResponse response = null;
        try
        {
            HttpContext httpContext = new BasicHttpContext( httpClient.getDefaultContext() );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "HTTPClient :: executeMethod() EXEC" );
            }

            response = httpClient.execute( target, request, httpContext ); // .execute( req, httpContext );
        }
        catch ( HttpException ex )
        {
            getLogger().error( "Protocol error while executing " + request.getMethod() + " method", ex );
        }
        catch ( IOException ex )
        {
            getLogger().error( "Tranport error while executing " + request.getMethod() + " method", ex );
        }
        
        getLogger().debug( "HTTPClient :: executeMethod() DONE" );
        
        return response;
    }

    /**
     * Make date from header.
     * 
     * @param date the date
     * @return the long
     */
    protected long makeDateFromHeader( Header date )
    {
        long result = System.currentTimeMillis();
        if ( date != null )
        {
            try
            {
                result = DateUtils.parseDate( date.getValue() ).getTime();
            }
            catch ( DateParseException ex )
            {
                getLogger().warn(
                    "Could not parse date '" + date + "', using system current time as item creation time.",
                    ex );
            }
            catch ( NullPointerException ex )
            {
                getLogger().warn( "Parsed date is null, using system current time as item creation time." );
            }
        }
        return result;
    }

    /**
     * The Class GzipDecompressingEntity.
     */
    static class GzipDecompressingEntity
        extends HttpEntityWrapper
    {

        /**
         * Instantiates a new gzip decompressing entity.
         * 
         * @param entity the entity
         */
        public GzipDecompressingEntity( final HttpEntity entity )
        {
            super( entity );
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.http.entity.HttpEntityWrapper#getContent()
         */
        public InputStream getContent()
            throws IOException,
                IllegalStateException
        {
            // the wrapped entity's getContent() decides about repeatability
            InputStream wrappedin = wrappedEntity.getContent();

            return new GZIPInputStream( wrappedin );
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.http.entity.HttpEntityWrapper#getContentLength()
         */
        public long getContentLength()
        {
            // length of ungzipped content is not known
            return -1;
        }

    }

}
