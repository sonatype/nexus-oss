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
package org.sonatype.nexus.proxy.storage.remote.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.util.EntityUtils;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessDeniedException;
import org.sonatype.nexus.proxy.RemoteAuthenticationNeededException;
import org.sonatype.nexus.proxy.RemoteStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.remote.AbstractHTTPRemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.utils.UserAgentBuilder;

/**
 * Apache HTTP client (4) {@link RemoteRepositoryStorage} implementation.
 *
 * @since 2.0
 */
@Named( HttpClientRemoteStorage.PROVIDER_STRING )
@Singleton
class HttpClientRemoteStorage
    extends AbstractHTTPRemoteRepositoryStorage
    implements RemoteRepositoryStorage
{

    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    /**
     * ID of this provider.
     */
    public static final String PROVIDER_STRING = "apacheHttpClient4x";

    /**
     * The namespace prefix to be used to store provider specific keys in {@link RemoteStorageContext}.
     */
    public static final String CTX_KEY = PROVIDER_STRING;

    /**
     * HTTP header key sent back by Nexus in case of a missing artifact.
     */
    public static final String NEXUS_MISSING_ARTIFACT_HEADER = "x-nexus-missing-artifact";

    /**
     * Created items while retrieving, can be read.
     */
    private static final boolean CAN_READ = true;

    /**
     * Created items while retrieving, can be written.
     */
    private static final boolean CAN_WRITE = true;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @Inject
    HttpClientRemoteStorage( final UserAgentBuilder userAgentBuilder,
                             final ApplicationStatusSource applicationStatusSource,
                             final MimeSupport mimeSupport )
    {
        super( userAgentBuilder, applicationStatusSource, mimeSupport );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public String getProviderId()
    {
        return PROVIDER_STRING;
    }

    @Override
    public AbstractStorageItem retrieveItem( final ProxyRepository repository,
                                             final ResourceStoreRequest request,
                                             final String baseUrl )
        throws ItemNotFoundException, RemoteStorageException
    {
        final URL remoteURL =
            appendQueryString( getAbsoluteUrlFromBase( baseUrl, request.getRequestPath() ), repository );

        final HttpGet method = new HttpGet( remoteURL.toExternalForm() );

        final HttpResponse httpResponse = executeRequest( repository, request, method );

        if ( httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK )
        {

            if ( method.getURI().getPath().endsWith( "/" ) )
            {
                // this is a collection and not a file!
                // httpClient will follow redirections, and the getPath()
                // _should_
                // give us URL with ending "/"
                release( httpResponse );
                throw new ItemNotFoundException(
                    "The remoteURL we got to looks like is a collection, and Nexus cannot fetch collections over plain HTTP (remoteUrl=\""
                        + remoteURL.toString() + "\")", request, repository );
            }

            InputStream is;
            try
            {
                is = httpResponse.getEntity().getContent();

                String mimeType = EntityUtils.getContentMimeType( httpResponse.getEntity() );
                if ( mimeType == null )
                {
                    mimeType = getMimeSupport().guessMimeTypeFromPath(
                        repository.getMimeRulesSource(), request.getRequestPath()
                    );
                }

                final DefaultStorageFileItem httpItem = new DefaultStorageFileItem(
                    repository, request, CAN_READ, CAN_WRITE, new PreparedContentLocator( is, mimeType )
                );

                if ( httpResponse.getEntity().getContentLength() != -1 )
                {
                    httpItem.setLength( httpResponse.getEntity().getContentLength() );
                }
                httpItem.setRemoteUrl( remoteURL.toString() );
                httpItem.setModified( makeDateFromHeader( httpResponse.getFirstHeader( "last-modified" ) ) );
                httpItem.setCreated( httpItem.getModified() );
                httpItem.getItemContext().putAll( request.getRequestContext() );

                return httpItem;
            }
            catch ( IOException ex )
            {
                release( httpResponse );
                throw new RemoteStorageException( "IO Error during response stream handling [repositoryId=\""
                                                      + repository.getId() + "\", requestPath=\""
                                                      + request.getRequestPath() + "\", remoteUrl=\""
                                                      + remoteURL.toString() + "\"]!", ex );
            }
            catch ( RuntimeException ex )
            {
                release( httpResponse );
                throw ex;
            }
        }
        else
        {
            release( httpResponse );
            if ( httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND )
            {
                throw new ItemNotFoundException(
                    "The remoteURL we requested does not exists on remote server (remoteUrl=\"" + remoteURL.toString()
                        + "\")", request, repository );
            }
            else
            {
                throw new RemoteStorageException( "The method execution returned result code "
                                                      + httpResponse.getStatusLine().getStatusCode()
                                                      + ". [repositoryId=\"" + repository.getId() + "\", requestPath=\""
                                                      + request.getRequestPath()
                                                      + "\", remoteUrl=\"" + remoteURL.toString() + "\"]" );
            }
        }
    }

    @Override
    public void storeItem( final ProxyRepository repository,
                           final StorageItem item )
        throws UnsupportedStorageOperationException, RemoteStorageException
    {
        if ( !( item instanceof StorageFileItem ) )
        {
            throw new UnsupportedStorageOperationException( "Storing of non-files remotely is not supported!" );
        }

        final StorageFileItem fileItem = (StorageFileItem) item;

        final ResourceStoreRequest request = new ResourceStoreRequest( item );

        final URL remoteUrl = appendQueryString( getAbsoluteUrlFromBase( repository, request ), repository );

        final HttpPut method = new HttpPut( remoteUrl.toExternalForm() );

        final InputStreamEntity entity;
        try
        {
            entity = new InputStreamEntity( fileItem.getInputStream(), fileItem.getLength() );
        }
        catch ( IOException e )
        {
            throw new RemoteStorageException(
                e.getMessage()
                    + " [repositoryId=\"" + repository.getId()
                    + "\", requestPath=\"" + request.getRequestPath()
                    + "\", remoteUrl=\"" + remoteUrl.toString()
                    + "\"]",
                e
            );
        }

        entity.setContentType( fileItem.getMimeType() );
        method.setEntity( entity );

        final HttpResponse httpResponse = executeRequestAndRelease( repository, request, method );
        final int statusCode = httpResponse.getStatusLine().getStatusCode();

        if ( statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED
            && statusCode != HttpStatus.SC_NO_CONTENT && statusCode != HttpStatus.SC_ACCEPTED )
        {
            throw new RemoteStorageException( "Unexpected response code while executing " + method.getMethod()
                                                  + " method [repositoryId=\"" + repository.getId()
                                                  + "\", requestPath=\"" + request.getRequestPath()
                                                  + "\", remoteUrl=\"" + remoteUrl.toString()
                                                  + "\"]. Expected: \"any success (2xx)\". Received: "
                                                  + statusCode + " : "
                                                  + httpResponse.getStatusLine().getReasonPhrase() );
        }
    }

    @Override
    public void deleteItem( final ProxyRepository repository,
                            final ResourceStoreRequest request )
        throws ItemNotFoundException, UnsupportedStorageOperationException, RemoteStorageException
    {
        final URL remoteUrl = appendQueryString( getAbsoluteUrlFromBase( repository, request ), repository );

        final HttpDelete method = new HttpDelete( remoteUrl.toExternalForm() );

        final HttpResponse httpResponse = executeRequestAndRelease( repository, request, method );
        final int statusCode = httpResponse.getStatusLine().getStatusCode();

        if ( statusCode != HttpStatus.SC_OK
            && statusCode != HttpStatus.SC_NO_CONTENT
            && statusCode != HttpStatus.SC_ACCEPTED )
        {
            throw new RemoteStorageException( "The response to HTTP " + method.getMethod()
                                                  + " was unexpected HTTP Code " + statusCode + " : "
                                                  + httpResponse.getStatusLine().getReasonPhrase()
                                                  + " [repositoryId=\"" + repository.getId() + "\", requestPath=\""
                                                  + request.getRequestPath()
                                                  + "\", remoteUrl=\"" + remoteUrl.toString() + "\"]" );
        }
    }

    @Override
    protected boolean checkRemoteAvailability( final long newerThen,
                                               final ProxyRepository repository,
                                               final ResourceStoreRequest request,
                                               final boolean isStrict )
        throws RemoteStorageException
    {
        final URL remoteUrl = appendQueryString( getAbsoluteUrlFromBase( repository, request ), repository );

        HttpRequestBase method;
        HttpResponse httpResponse = null;
        int statusCode = HttpStatus.SC_BAD_REQUEST;

        // artifactory hack, it pukes on HEAD so we will try with GET if HEAD fails
        boolean doGet = false;

        {
            method = new HttpHead( remoteUrl.toExternalForm() );
            try
            {
                httpResponse = executeRequestAndRelease( repository, request, method );
                statusCode = httpResponse.getStatusLine().getStatusCode();
            }
            catch ( RemoteStorageException e )
            {
                // If HEAD failed, attempt a GET. Some repos may not support HEAD method
                doGet = true;

                getLogger().debug( "HEAD method failed, will attempt GET. Exception: " + e.getMessage(), e );
            }
            finally
            {
                // HEAD returned error, but not exception, try GET before failing
                if ( !doGet && statusCode != HttpStatus.SC_OK )
                {
                    doGet = true;

                    getLogger().debug( "HEAD method failed, will attempt GET. Status: " + statusCode );
                }
            }
        }

        {
            if ( doGet )
            {
                // create a GET
                method = new HttpGet( remoteUrl.toExternalForm() );

                // execute it
                httpResponse = executeRequestAndRelease( repository, request, method );
                statusCode = httpResponse.getStatusLine().getStatusCode();
            }
        }

        // if we are not strict and remote is S3
        if ( !isStrict && isRemotePeerAmazonS3Storage( repository ) )
        {
            // if we are relaxed, we will accept any HTTP response code below 500. This means anyway the HTTP
            // transaction succeeded. This method was never really detecting that the remoteUrl really denotes a root of
            // repository (how could we do that?)
            // this "relaxed" check will help us to "pass" S3 remote storage.
            return statusCode >= HttpStatus.SC_OK && statusCode <= HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }
        else
        {
            // non relaxed check is strict, and will select only the OK response
            if ( statusCode == HttpStatus.SC_OK )
            {
                // we have it
                // we have newer if this below is true
                return makeDateFromHeader( httpResponse.getFirstHeader( "last-modified" ) ) > newerThen;
            }
            else if ( ( statusCode >= HttpStatus.SC_MULTIPLE_CHOICES && statusCode < HttpStatus.SC_BAD_REQUEST )
                || statusCode == HttpStatus.SC_NOT_FOUND )
            {
                return false;
            }
            else
            {
                throw new RemoteStorageException( "Unexpected response code while executing " + method.getMethod()
                                                      + " method [repositoryId=\"" + repository.getId()
                                                      + "\", requestPath=\"" + request.getRequestPath()
                                                      + "\", remoteUrl=\"" + remoteUrl.toString()
                                                      + "\"]. Expected: \"SUCCESS (200)\". Received: "
                                                      + statusCode + " : " +
                                                      httpResponse.getStatusLine().getReasonPhrase() );
            }
        }
    }

    @Override
    protected void updateContext( final ProxyRepository repository,
                                  final RemoteStorageContext ctx )
    {
        // reset current http client, if exists
        HttpClientUtil.release( CTX_KEY, ctx );

        // and create a new one
        HttpClientUtil.configure( CTX_KEY, ctx, getLogger() );
    }

    @Override
    protected String getS3FlagKey()
    {
        return HttpClientUtil.getS3FlagKey( CTX_KEY );
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Executes the HTTP request.
     * <p/>
     * In case of any exception thrown by HttpClient, it will release the connection. In other cases it
     * is the duty of caller to do it, or process the input stream.
     *
     * @param repository  to execute the HTTP method fpr
     * @param request     resource store request that triggered the HTTP request
     * @param httpRequest HTTP request to be executed
     * @return response of making the request
     * @throws RemoteStorageException If an error occurred during execution of HTTP request
     */
    private HttpResponse executeRequest( final ProxyRepository repository,
                                         final ResourceStoreRequest request,
                                         final HttpUriRequest httpRequest )
        throws RemoteStorageException
    {
        final URI methodUri = httpRequest.getURI();

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "Invoking HTTP " + httpRequest.getMethod() + " method against remote location " + methodUri
            );
        }

        final RemoteStorageContext ctx = getRemoteStorageContext( repository );

        final HttpClient httpClient = HttpClientUtil.getHttpClient( CTX_KEY, ctx );

        httpRequest.setHeader( "user-agent", formatUserAgentString( ctx, repository ) );
        httpRequest.setHeader( "accept", "*/*" );
        httpRequest.setHeader( "accept-language", "en-us" );
        httpRequest.setHeader( "accept-encoding", "gzip,deflate,identity" );
        httpRequest.setHeader( "cache-control", "no-cache" );

        // HTTP keep alive should not be used, except when NTLM is used
        final Boolean isNtlmUsed = HttpClientUtil.isNTLMAuthenticationUsed( CTX_KEY, ctx );
        if ( isNtlmUsed == null || !isNtlmUsed )
        {
            httpRequest.setHeader( "Connection", "close" );
            httpRequest.setHeader( "Proxy-Connection", "close" );
        }

        HttpResponse httpResponse = null;
        try
        {
            httpResponse = httpClient.execute( httpRequest );
            final int statusCode = httpResponse.getStatusLine().getStatusCode();

            final Header httpServerHeader = httpResponse.getFirstHeader( "server" );
            checkForRemotePeerAmazonS3Storage(
                repository, httpServerHeader == null ? null : httpServerHeader.getValue()
            );

            Header proxyReturnedErrorHeader = httpResponse.getFirstHeader( NEXUS_MISSING_ARTIFACT_HEADER );
            boolean proxyReturnedError =
                proxyReturnedErrorHeader != null && Boolean.valueOf( proxyReturnedErrorHeader.getValue() );

            if ( statusCode == HttpStatus.SC_FORBIDDEN )
            {
                throw new RemoteAccessDeniedException(
                    repository, methodUri.toASCIIString(), httpResponse.getStatusLine().getReasonPhrase()
                );
            }
            else if ( statusCode == HttpStatus.SC_UNAUTHORIZED )
            {
                throw new RemoteAuthenticationNeededException(
                    repository, httpResponse.getStatusLine().getReasonPhrase()
                );
            }
            else if ( statusCode == HttpStatus.SC_OK && proxyReturnedError )
            {
                throw new RemoteStorageException(
                    "Invalid artifact found, most likely a proxy redirected to an HTML error page."
                );
            }

            return httpResponse;
        }
        catch ( RemoteStorageException ex )
        {
            release( httpResponse );
            throw ex;
        }
        catch ( ClientProtocolException ex )
        {
            release( httpResponse );
            throw new RemoteStorageException( "Protocol error while executing " + httpRequest.getMethod()
                                                  + " method. [repositoryId=\"" + repository.getId()
                                                  + "\", requestPath=\"" + request.getRequestPath()
                                                  + "\", remoteUrl=\"" + methodUri.toASCIIString() + "\"]", ex );
        }
        catch ( IOException ex )
        {
            release( httpResponse );
            throw new RemoteStorageException( "Transport error while executing " + httpRequest.getMethod()
                                                  + " method [repositoryId=\"" + repository.getId()
                                                  + "\", requestPath=\"" + request.getRequestPath()
                                                  + "\", remoteUrl=\"" + methodUri.toASCIIString() + "\"]", ex );
        }
    }

    /**
     * Executes the HTTP request and automatically releases any related resources.
     *
     * @param repository  to execute the HTTP method fpr
     * @param request     resource store request that triggered the HTTP request
     * @param httpRequest HTTP request to be executed
     * @return response of making the request
     * @throws RemoteStorageException If an error occurred during execution of HTTP request
     */
    private HttpResponse executeRequestAndRelease( final ProxyRepository repository,
                                                   final ResourceStoreRequest request,
                                                   final HttpUriRequest httpRequest )
        throws RemoteStorageException
    {
        final HttpResponse httpResponse = executeRequest( repository, request, httpRequest );
        release( httpResponse );
        return httpResponse;
    }

    /**
     * Make date from header.
     *
     * @param date the date
     * @return the long
     */
    private long makeDateFromHeader( final Header date )
    {
        long result = System.currentTimeMillis();
        if ( date != null )
        {
            try
            {
                result = DateUtil.parseDate( date.getValue() ).getTime();
            }
            catch ( DateParseException ex )
            {
                getLogger().warn(
                    "Could not parse date '" + date + "', using system current time as item creation time.", ex );
            }
            catch ( NullPointerException ex )
            {
                getLogger().warn( "Parsed date is null, using system current time as item creation time." );
            }
        }
        return result;
    }

    /**
     * Appends repository configured additional query string to provided URL.
     *
     * @param url        to append to
     * @param repository that may contain additional query string
     * @return URL with appended query string or original URL if repository does not have an configured query string
     * @throws RemoteStorageException if query string could not be appended (resulted in an Malformed URL exception)
     */
    private URL appendQueryString( final URL url,
                                   final ProxyRepository repository )
        throws RemoteStorageException
    {
        final RemoteStorageContext ctx = getRemoteStorageContext( repository );

        final String queryString = ctx.getRemoteConnectionSettings().getQueryString();
        if ( StringUtils.isNotBlank( queryString ) )
        {
            try
            {
                if ( StringUtils.isBlank( url.getQuery() ) )
                {
                    return new URL( url.toExternalForm() + "?" + queryString );
                }
                else
                {
                    return new URL( url.toExternalForm() + "&" + queryString );
                }
            }
            catch ( MalformedURLException e )
            {
                throw new RemoteStorageException(
                    "Could not append query string \"" + queryString + "\" to url \"" + url + "\"", e
                );
            }
        }
        return url;
    }

    /**
     * Releases connection resources (back to pool). If an exception appears during releasing, exception is just logged.
     *
     * @param httpResponse to be released
     */
    private void release( final HttpResponse httpResponse )
    {
        if ( httpResponse != null )
        {
            try
            {
                EntityUtils.consume( httpResponse.getEntity() );
            }
            catch ( IOException e )
            {
                getLogger().warn( e.getMessage() );
            }
        }
    }

}
