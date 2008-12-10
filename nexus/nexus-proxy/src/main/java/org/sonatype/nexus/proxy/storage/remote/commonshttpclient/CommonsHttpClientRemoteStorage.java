/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.proxy.storage.remote.commonshttpclient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.remote.AbstractRemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

/**
 * The Class CommonsHttpClientRemoteStorage.
 * 
 * @author cstamas
 * @plexus.component role-hint="apacheHttpClient3x"
 */
public class CommonsHttpClientRemoteStorage
    extends AbstractRemoteRepositoryStorage
    implements RemoteRepositoryStorage
{
    public static final String CTX_KEY = "apacheHttpClient3x";

    public static final String CTX_KEY_CLIENT = CTX_KEY + ".client";

    public static final String CTX_KEY_HTTP_RETRY_HANDLER = CTX_KEY + ".httpRetryHandler";

    public static final String CTX_KEY_HTTP_CONFIGURATION = CTX_KEY + ".httpConfiguration";

    // ===============================================================================
    // RemoteStorage iface

    public void validateStorageUrl( String url )
        throws StorageException
    {
        try
        {
            URL u = new URL( url );

            if ( !"http".equals( u.getProtocol().toLowerCase() ) && !"https".equals( u.getProtocol().toLowerCase() ) )
            {
                throw new StorageException( "Unsupported protocol: " + u.getProtocol().toLowerCase() );
            }
        }
        catch ( MalformedURLException e )
        {
            throw new StorageException( "Malformed URL", e );
        }
    }

    public boolean containsItem( RepositoryItemUid uid, long newerThen, Map<String, Object> context )
        throws StorageException
    {
        HttpMethodBase method = new HeadMethod( getAbsoluteUrlFromBase( uid ).toString() );

        int response = HttpStatus.SC_BAD_REQUEST;

        boolean doGet = false;

        try
        {
            response = executeMethod( uid, method );
        }
        catch ( StorageException e )
        {
            // If HEAD failed, attempt a GET. Some repos may not support HEAD method
            doGet = true;

            getLogger().debug( "HEAD method failed, will attempt GET.  Exception: " + e.getMessage() );
        }
        finally
        {
            method.releaseConnection();

            // HEAD returned error, but not exception, try GET before failing
            if ( doGet == false && response != HttpStatus.SC_OK )
            {
                doGet = true;
                getLogger().debug( "HEAD method failed, will attempt GET.  Status: " + response );
            }
        }

        if ( doGet )
        {
            // create a GET
            method = new GetMethod( getAbsoluteUrlFromBase( uid ).toString() );

            try
            {
                // execute it
                response = executeMethod( uid, method );
            }
            finally
            {
                // and release it immediately
                method.releaseConnection();
            }
        }

        if ( response == HttpStatus.SC_OK )
        {
            // we have newer if this below is true
            return makeDateFromHeader( method.getResponseHeader( "last-modified" ) ) > newerThen;
        }
        else if ( ( response >= HttpStatus.SC_MULTIPLE_CHOICES && response < HttpStatus.SC_BAD_REQUEST )
            || response == HttpStatus.SC_NOT_FOUND )
        {
            return false;
        }
        else
        {
            throw new StorageException( "The response to HTTP " + method.getName() + " was unexpected HTTP Code "
                + response + " : " + HttpStatus.getStatusText( response ) );
        }
    }

    public void deleteItem( RepositoryItemUid uid, Map<String, Object> context )
        throws ItemNotFoundException,
            UnsupportedStorageOperationException,
            StorageException
    {
        // TODO: do a HTTP OPTION and discover is DELETE allowed
        throw new UnsupportedStorageOperationException( "This operation is not supported on "
            + getAbsoluteUrlFromBase( uid ).toString() );
    }

    public AbstractStorageItem retrieveItem( RepositoryItemUid uid, Map<String, Object> context )
        throws ItemNotFoundException,
            StorageException
    {
        HttpMethod method = null;
        method = new GetMethod( getAbsoluteUrlFromBase( uid ).toString() );

        int response = executeMethod( uid, method );
        if ( response == HttpStatus.SC_OK )
        {

            if ( method.getPath().endsWith( "/" ) )
            {
                // this is a collection and not a file!
                // httpClient will follow redirections, and the getPath()
                // _should_
                // give us URL with ending "/"
                method.releaseConnection();
                throw new ItemNotFoundException( uid );
            }

            GetMethod get = (GetMethod) method;
            InputStream is = null;
            try
            {
                is = get.getResponseBodyAsStream();
                if ( get.getResponseHeader( "Content-Encoding" ) != null
                    && "gzip".equals( get.getResponseHeader( "Content-Encoding" ).getValue() ) )
                {
                    is = new GZIPInputStream( is );
                }

                DefaultStorageFileItem httpItem = new DefaultStorageFileItem(
                    uid.getRepository(),
                    uid.getPath(),
                    true,
                    true,
                    new PreparedContentLocator( new HttpClientInputStream( get, is ) ) );
                httpItem.setRemoteUrl( getAbsoluteUrlFromBase( uid ).toString() );
                if ( get.getResponseContentLength() != -1 )
                {
                    // FILE
                    httpItem.setLength( get.getResponseContentLength() );
                }
                if ( method.getResponseHeader( "content-type" ) != null )
                {
                    httpItem.setMimeType( method.getResponseHeader( "content-type" ).getValue() );
                }
                httpItem.setModified( makeDateFromHeader( method.getResponseHeader( "last-modified" ) ) );
                httpItem.setCreated( httpItem.getModified() );
                return httpItem;

            }
            catch ( IOException ex )
            {
                method.releaseConnection();

                throw new StorageException( "IO Error during response stream handling!", ex );
            }
            catch ( RuntimeException ex )
            {
                method.releaseConnection();

                throw ex;
            }
        }
        else
        {
            method.releaseConnection();

            if ( response == HttpStatus.SC_NOT_FOUND )
            {
                throw new ItemNotFoundException( getAbsoluteUrlFromBase( uid ).toString() );
            }
            else
            {
                throw new StorageException( "The method execution returned result code " + response );
            }
        }
    }

    public void storeItem( StorageItem item )
        throws UnsupportedStorageOperationException,
            StorageException
    {
        // TODO: do a HTTP OPTION and discover is PUT allowed
        throw new UnsupportedStorageOperationException( "This operation is not supported on "
            + getAbsoluteUrlFromBase( item.getRepositoryItemUid() ).toString() );
    }

    /**
     * Gets the http client.
     * 
     * @return the http client
     */
    protected void updateContext( Repository repository, RemoteStorageContext ctx )
    {
        HttpClient httpClient = null;

        HttpMethodRetryHandler httpRetryHandler = null;

        HostConfiguration httpConfiguration = null;

        getLogger().debug( "Creating CommonsHttpClient instance" );
        httpRetryHandler = new DefaultHttpMethodRetryHandler( getRemoteConnectionSettings( ctx )
            .getRetrievalRetryCount(), false );
        httpClient = new HttpClient( new MultiThreadedHttpConnectionManager() );
        httpClient.getParams().setConnectionManagerTimeout( getRemoteConnectionSettings( ctx ).getConnectionTimeout() );

        httpConfiguration = httpClient.getHostConfiguration();

        // BASIC and DIGEST auth only
        if ( getRemoteAuthenticationSettings( ctx ) != null
            && getRemoteAuthenticationSettings( ctx ).getUsername() != null )
        {
            // we have proxy authentication, let's do it preemptive
            httpClient.getParams().setAuthenticationPreemptive( true );

            List<String> authPrefs = new ArrayList<String>( 2 );
            authPrefs.add( AuthPolicy.DIGEST );
            authPrefs.add( AuthPolicy.BASIC );

            if ( getRemoteAuthenticationSettings( ctx ).getNtlmDomain() != null )
            {
                // Using NTLM auth, adding it as first in policies
                authPrefs.add( 0, AuthPolicy.NTLM );

                getLogger().info(
                    "... authentication setup for NTLM domain {}"
                        + getRemoteAuthenticationSettings( ctx ).getNtlmDomain() );
                httpConfiguration.setHost( getRemoteAuthenticationSettings( ctx ).getNtlmHost() );

                httpClient.getState().setCredentials(
                    AuthScope.ANY,
                    new NTCredentials(
                        getRemoteAuthenticationSettings( ctx ).getUsername(),
                        getRemoteAuthenticationSettings( ctx ).getPassword(),
                        getRemoteAuthenticationSettings( ctx ).getNtlmHost(),
                        getRemoteAuthenticationSettings( ctx ).getNtlmDomain() ) );
            }
            else
            {

                // Using Username/Pwd auth, will not add NTLM
                getLogger().info(
                    "... setting authentication setup for remote storage with username "
                        + getRemoteAuthenticationSettings( ctx ).getUsername() );

                httpClient.getState().setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(
                        getRemoteAuthenticationSettings( ctx ).getUsername(),
                        getRemoteAuthenticationSettings( ctx ).getPassword() ) );

            }
            httpClient.getParams().setParameter( AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs );
        }

        if ( getRemoteHttpProxySettings( ctx ) != null && getRemoteHttpProxySettings( ctx ).getProxyHostname() != null )
        {
            getLogger().info( "... proxy setup with host " + getRemoteHttpProxySettings( ctx ).getProxyHostname() );
            httpConfiguration.setProxy(
                getRemoteHttpProxySettings( ctx ).getProxyHostname(),
                getRemoteHttpProxySettings( ctx ).getProxyPort() );

            if ( getRemoteHttpProxySettings( ctx ).getAuthentication() != null
                && getRemoteHttpProxySettings( ctx ).getAuthentication().getUsername() != null )
            {
                List<String> authPrefs = new ArrayList<String>( 2 );
                authPrefs.add( AuthPolicy.DIGEST );
                authPrefs.add( AuthPolicy.BASIC );

                if ( getRemoteHttpProxySettings( ctx ).getAuthentication().getNtlmDomain() != null )
                {

                    // Using NTLM auth, adding it as first in policies
                    authPrefs.add( 0, AuthPolicy.NTLM );

                    if ( getRemoteHttpProxySettings( ctx ).getAuthentication().getUsername() != null )
                    {
                        getLogger().warn(
                            "... CommonsHttpClient is unable to use NTLM auth scheme\n"
                                + " for BOTH server side and proxy side authentication!\n"
                                + " You MUST reconfigure server side auth and use BASIC/DIGEST scheme\n"
                                + " if you have to use NTLM proxy, since otherwise it will not work!\n"
                                + " *** SERVER SIDE AUTH OVERRIDDEN" );
                    }
                    getLogger().info(
                        "... proxy authentication setup for NTLM domain "
                            + getRemoteHttpProxySettings( ctx ).getAuthentication().getNtlmDomain() );
                    httpConfiguration.setHost( getRemoteHttpProxySettings( ctx ).getAuthentication().getNtlmHost() );

                    httpClient.getState().setProxyCredentials(
                        AuthScope.ANY,
                        new NTCredentials(
                            getRemoteHttpProxySettings( ctx ).getAuthentication().getUsername(),
                            getRemoteHttpProxySettings( ctx ).getAuthentication().getPassword(),
                            getRemoteHttpProxySettings( ctx ).getAuthentication().getNtlmHost(),
                            getRemoteHttpProxySettings( ctx ).getAuthentication().getNtlmDomain() ) );
                }
                else
                {

                    // Using Username/Pwd auth, will not add NTLM
                    getLogger().info(
                        "... proxy authentication setup for http proxy "
                            + getRemoteHttpProxySettings( ctx ).getProxyHostname() );

                    httpClient.getState().setProxyCredentials(
                        AuthScope.ANY,
                        new UsernamePasswordCredentials( getRemoteHttpProxySettings( ctx )
                            .getAuthentication().getUsername(), getRemoteHttpProxySettings( ctx )
                            .getAuthentication().getPassword() ) );

                }
                httpClient.getParams().setParameter( AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs );
            }

        }

        ctx.getRemoteConnectionContext().put( CTX_KEY_CLIENT, httpClient );

        ctx.getRemoteConnectionContext().put( CTX_KEY_HTTP_CONFIGURATION, httpConfiguration );

        ctx.getRemoteConnectionContext().put( CTX_KEY_HTTP_RETRY_HANDLER, httpRetryHandler );
    }

    /**
     * Execute method. In case of any exception thrown by HttpClient, it will release the connection. In other cases it
     * is the duty of caller to do it, or process the input stream.
     * 
     * @param method the method
     * @return the int
     */
    protected int executeMethod( RepositoryItemUid uid, HttpMethod method )
        throws StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger()
                .debug(
                    "Invoking HTTP " + method.getName() + " method against remote location "
                        + getAbsoluteUrlFromBase( uid ) );
        }

        RemoteStorageContext ctx = getRemoteStorageContext( uid.getRepository() );

        HttpClient httpClient = (HttpClient) ctx.getRemoteConnectionContext().get( CTX_KEY_CLIENT );

        HttpMethodRetryHandler httpRetryHandler = (HttpMethodRetryHandler) ctx.getRemoteConnectionContext().get(
            CTX_KEY_HTTP_RETRY_HANDLER );

        HostConfiguration httpConfiguration = (HostConfiguration) ctx.getRemoteConnectionContext().get(
            CTX_KEY_HTTP_CONFIGURATION );

        method.setRequestHeader( new Header( "user-agent", formatUserAgentString( ctx, uid.getRepository() ) ) );
        method.setRequestHeader( new Header( "accept", "*/*" ) );
        method.setRequestHeader( new Header( "accept-language", "en-us" ) );
        method.setRequestHeader( new Header( "accept-encoding", "gzip, identity" ) );
        method.setRequestHeader( new Header( "cache-control", "no-cache" ) );

        // HTTP keep alive should not be used
        method.setRequestHeader( new Header( "Connection", "close" ) );
        method.setRequestHeader( new Header( "Proxy-Connection", "close" ) );

        // to use HTTP keep alive
        // method.setRequestHeader( new Header( "Connection", "Keep-Alive" ) );

        method.setFollowRedirects( true );
        method.getParams().setParameter( HttpMethodParams.RETRY_HANDLER, httpRetryHandler );

        if ( !StringUtils.isEmpty( getRemoteConnectionSettings( ctx ).getQueryString() ) )
        {
            method.setQueryString( getRemoteConnectionSettings( ctx ).getQueryString() );
        }

        int resultCode = 0;

        try
        {
            resultCode = httpClient.executeMethod( httpConfiguration, method );
        }
        catch ( HttpException ex )
        {
            method.releaseConnection();

            throw new StorageException( "Protocol error while executing " + method.getName() + " method", ex );
        }
        catch ( IOException ex )
        {
            method.releaseConnection();

            throw new StorageException( "Tranport error while executing " + method.getName() + " method", ex );
        }

        return resultCode;
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
                result = DateUtil.parseDate( date.getValue() ).getTime();
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

    public String getName()
    {
        return CTX_KEY;
    }

}
