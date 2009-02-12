/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessDeniedException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.RemoteAuthenticationNeededException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.remote.AbstractRemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

/**
 * The Class CommonsHttpClientRemoteStorage.
 * 
 * @author cstamas
 */
@Component( role = RemoteRepositoryStorage.class, hint = "apacheHttpClient3x" )
public class CommonsHttpClientRemoteStorage
    extends AbstractRemoteRepositoryStorage
    implements RemoteRepositoryStorage
{
    public static final String CTX_KEY = "apacheHttpClient3x";

    public static final String CTX_KEY_CLIENT = CTX_KEY + ".client";

    public static final String CTX_KEY_HTTP_CONFIGURATION = CTX_KEY + ".httpConfiguration";

    // ===============================================================================
    // RemoteStorage iface

    public String getName()
    {
        return CTX_KEY;
    }

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

    public boolean isReachable( ProxyRepository repository, Map<String, Object> context )
        throws RemoteAccessException,
            StorageException
    {
        return checkRemoteAvailability( 0, repository, context, RepositoryItemUid.PATH_ROOT, isReachableCheckRelaxed() );
    }

    public boolean containsItem( long newerThen, ProxyRepository repository, Map<String, Object> context, String path )
        throws RemoteAccessException,
            StorageException
    {
        return checkRemoteAvailability( newerThen, repository, context, path, false );
    }

    public AbstractStorageItem retrieveItem( ProxyRepository repository, Map<String, Object> context, String baseUrl,
        String path )
        throws ItemNotFoundException,
            RemoteAccessException,
            StorageException
    {
        URL remoteURL = getAbsoluteUrlFromBase( baseUrl, path );

        HttpMethod method = null;

        method = new GetMethod( remoteURL.toString() );

        int response = executeMethod( repository, context, path, method, remoteURL );

        if ( response == HttpStatus.SC_OK )
        {

            if ( method.getPath().endsWith( "/" ) )
            {
                // this is a collection and not a file!
                // httpClient will follow redirections, and the getPath()
                // _should_
                // give us URL with ending "/"
                method.releaseConnection();

                throw new ItemNotFoundException( remoteURL.toString() );
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
                    repository,
                    path,
                    true,
                    true,
                    new PreparedContentLocator( new HttpClientInputStream( get, is ) ) );

                httpItem.setRemoteUrl( remoteURL.toString() );

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
                throw new ItemNotFoundException( remoteURL.toString() );
            }
            else
            {
                throw new StorageException( "The method execution returned result code " + response );
            }
        }
    }

    public void storeItem( ProxyRepository repository, Map<String, Object> context, StorageItem item )
        throws UnsupportedStorageOperationException,
            RemoteAccessException,
            StorageException
    {
        if ( !( item instanceof StorageFileItem ) )
        {
            throw new UnsupportedStorageOperationException( "Storing of non-files remotely is not supported!" );
        }

        StorageFileItem fItem = (StorageFileItem) item;

        URL remoteURL = getAbsoluteUrlFromBase( repository, context, item.getPath() );

        PutMethod method = new PutMethod( remoteURL.toString() );

        try
        {
            method.setRequestEntity( new InputStreamRequestEntity( fItem.getInputStream(), fItem.getLength(), fItem
                .getMimeType() ) );

            int response = executeMethod( repository, context, item.getPath(), method, remoteURL );

            if ( response != HttpStatus.SC_OK && response != HttpStatus.SC_CREATED
                && response != HttpStatus.SC_NO_CONTENT && response != HttpStatus.SC_ACCEPTED )
            {
                throw new StorageException( "The response to HTTP " + method.getName() + " was unexpected HTTP Code "
                    + response + " : " + HttpStatus.getStatusText( response ) );
            }
        }
        catch ( IOException e )
        {
            throw new StorageException( e );
        }
        finally
        {
            method.releaseConnection();
        }
    }

    public void deleteItem( ProxyRepository repository, Map<String, Object> context, String path )
        throws ItemNotFoundException,
            UnsupportedStorageOperationException,
            RemoteAccessException,
            StorageException
    {
        URL remoteURL = getAbsoluteUrlFromBase( repository, context, path );

        DeleteMethod method = new DeleteMethod( remoteURL.toString() );

        try
        {
            int response = executeMethod( repository, context, path, method, remoteURL );

            if ( response != HttpStatus.SC_OK && response != HttpStatus.SC_NO_CONTENT
                && response != HttpStatus.SC_ACCEPTED )
            {
                throw new StorageException( "The response to HTTP " + method.getName() + " was unexpected HTTP Code "
                    + response + " : " + HttpStatus.getStatusText( response ) );
            }
        }
        finally
        {
            method.releaseConnection();
        }
    }

    /**
     * Gets the http client.
     * 
     * @return the http client
     */
    protected void updateContext( ProxyRepository repository, RemoteStorageContext ctx )
    {
        HttpClient httpClient = null;

        HostConfiguration httpConfiguration = null;

        getLogger().debug( "Creating CommonsHttpClient instance" );

        int timeout = getRemoteConnectionSettings( ctx ).getConnectionTimeout();

        HttpConnectionManagerParams connManagerParams = new HttpConnectionManagerParams();
        connManagerParams.setConnectionTimeout( timeout );
        connManagerParams.setSoTimeout( timeout );
        connManagerParams.setTcpNoDelay( true );

        MultiThreadedHttpConnectionManager connManager = new MultiThreadedHttpConnectionManager();
        connManager.setParams( connManagerParams );

        httpClient = new HttpClient( connManager );
        httpClient.getParams().setConnectionManagerTimeout( timeout );
        httpClient.getParams().setSoTimeout( timeout );

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
    }

    /**
     * Execute method. In case of any exception thrown by HttpClient, it will release the connection. In other cases it
     * is the duty of caller to do it, or process the input stream.
     * 
     * @param method the method
     * @return the int
     */
    protected int executeMethod( ProxyRepository repository, Map<String, Object> context, String path,
        HttpMethod method, URL remoteUrl )
        throws RemoteAccessException,
            StorageException
    {
        if ( getLogger().isDebugEnabled() )
        {
            try
            {
                getLogger().debug(
                    "Invoking HTTP " + method.getName() + " method against remote location " + method.getURI() );
            }
            catch ( URIException e )
            {
                getLogger().debug( "Could not format debug log message", e );
            }
        }

        RemoteStorageContext ctx = getRemoteStorageContext( repository );

        HttpClient httpClient = (HttpClient) ctx.getRemoteConnectionContext().get( CTX_KEY_CLIENT );

        HostConfiguration httpConfiguration = (HostConfiguration) ctx.getRemoteConnectionContext().get(
            CTX_KEY_HTTP_CONFIGURATION );

        method.setRequestHeader( new Header( "user-agent", formatUserAgentString( ctx, repository ) ) );
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

        if ( !StringUtils.isEmpty( getRemoteConnectionSettings( ctx ).getQueryString() ) )
        {
            method.setQueryString( getRemoteConnectionSettings( ctx ).getQueryString() );
        }

        int resultCode = 0;

        try
        {
            resultCode = httpClient.executeMethod( httpConfiguration, method );

            if ( resultCode == HttpStatus.SC_FORBIDDEN )
            {
                throw new RemoteAccessDeniedException( repository, remoteUrl, HttpStatus
                    .getStatusText( HttpStatus.SC_FORBIDDEN ) );
            }
            else if ( resultCode == HttpStatus.SC_UNAUTHORIZED )
            {
                throw new RemoteAuthenticationNeededException( repository, HttpStatus
                    .getStatusText( HttpStatus.SC_UNAUTHORIZED ) );
            }
        }
        catch ( StorageException e )
        {
            method.releaseConnection();

            throw e;
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

    /**
     * Are we "relaxed" regarding the interpretation of responses when checking remote availability.
     * 
     * @return
     */
    protected boolean isReachableCheckRelaxed()
    {
        return true;
    }

    /**
     * Initially, this method is here only to share the code for "availability check" and for "contains" check.
     * Unfortunately, the "availability" check cannot be done at RemoteStorage level, since it is completely repository
     * layout unaware and is able to tell only about the existence of remote server and that the URI on it exists. This
     * "availability" check will have to be moved upper into repository, since it is aware of "what it holds".
     * Ultimately, this method will check is the remote server "present" and is responding or not. But nothing more.
     * 
     * @param newerThen
     * @param repository
     * @param context
     * @param path
     * @param relaxedCheck
     * @return
     * @throws RemoteAuthenticationNeededException
     * @throws RemoteAccessException
     * @throws StorageException
     */
    protected boolean checkRemoteAvailability( long newerThen, ProxyRepository repository, Map<String, Object> context,
        String path, boolean relaxedCheck )
        throws RemoteAuthenticationNeededException,
            RemoteAccessException,
            StorageException
    {
        URL remoteURL = getAbsoluteUrlFromBase( repository, context, path );

        HttpMethodBase method = new HeadMethod( remoteURL.toString() );

        int response = HttpStatus.SC_BAD_REQUEST;

        // artifactory hack, it pukes on HEAD so we will try with GET if HEAD fails
        boolean doGet = false;

        try
        {
            response = executeMethod( repository, context, path, method, remoteURL );
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
            method = new GetMethod( remoteURL.toString() );

            try
            {
                // execute it
                response = executeMethod( repository, context, path, method, remoteURL );
            }
            finally
            {
                // and release it immediately
                method.releaseConnection();
            }
        }

        if ( relaxedCheck )
        {
            // if we are relaxed, we will accept any HTTP response code below 500. This means anyway the HTTP
            // transaction succeeded. This method was never really detecting that the remoteUrl really denotes a root of
            // repository (how could we do that?)
            // this "relaxed" check will help us to "pass" S3 remote storage.
            return response >= HttpStatus.SC_OK && response <= HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }
        else
        {
            // non relaxed check is strict, and will select only the OK response
            if ( response == HttpStatus.SC_OK )
            {
                // we have it
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
    }

}
