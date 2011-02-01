package org.sonatype.nexus.proxy.storage.remote.ahc;

import java.net.MalformedURLException;
import java.net.URL;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.ahc.AhcProvider;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessDeniedException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.RemoteAuthenticationNeededException;
import org.sonatype.nexus.proxy.RemoteStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.remote.AbstractRemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.ahc.AhcContentLocator.ResponseInputStream;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;
import com.ning.http.util.DateUtil;
import com.ning.http.util.DateUtil.DateParseException;

/**
 * AsyncHttpClient powered RemoteRepositoryStorage.
 * 
 * @author cstamas
 */
@Component( role = RemoteRepositoryStorage.class, hint = AhcRemoteRepositoryStorage.PROVIDER_STRING )
public class AhcRemoteRepositoryStorage
    extends AbstractRemoteRepositoryStorage
{
    public static final String PROVIDER_STRING = "async-http-client";

    private static final String CTX_KEY = PROVIDER_STRING;

    private static final String CTX_KEY_CLIENT = CTX_KEY + ".client";

    private static final String CTX_KEY_S3_FLAG = CTX_KEY + ".remoteIsAmazonS3";

    private static final boolean HEAD_THEN_GET = true;

    @Requirement
    private AhcProvider ahcProvider;

    @Override
    public String getProviderId()
    {
        return PROVIDER_STRING;
    }

    @Override
    public void validateStorageUrl( String url )
        throws RemoteStorageException
    {
        try
        {
            URL u = new URL( url );

            if ( !"http".equals( u.getProtocol().toLowerCase() ) && !"https".equals( u.getProtocol().toLowerCase() ) )
            {
                throw new RemoteStorageException( "Unsupported protocol, only HTTP/HTTPS protocols are supported: "
                    + u.getProtocol().toLowerCase() );
            }
        }
        catch ( MalformedURLException e )
        {
            throw new RemoteStorageException( "Malformed URL", e );
        }
    }

    @Override
    public boolean isReachable( ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteAccessException, RemoteStorageException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsItem( long newerThen, ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteAccessException, RemoteStorageException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AbstractStorageItem retrieveItem( ProxyRepository repository, ResourceStoreRequest request, String baseUrl )
        throws ItemNotFoundException, RemoteAccessException, RemoteStorageException
    {
        final URL remoteURL = getAbsoluteUrlFromBase( baseUrl, request.getRequestPath() );

        final String itemUrl = remoteURL.toString();

        final AsyncHttpClient client = getClient( repository );

        try
        {
            // TODO: this is code duplication while experimenting with AHC, will cleanup later!
            if ( HEAD_THEN_GET )
            {
                Response response = client.prepareHead( itemUrl ).execute().get();

                // expected: 200 OK
                validateResponse( repository, request, "HEAD", itemUrl, response, 200 );

                long length = -1;

                try
                {
                    length = Long.parseLong( response.getHeader( "content-length" ) );
                }
                catch ( NumberFormatException e )
                {
                    // neglect
                }

                long lastModified = System.currentTimeMillis();

                try
                {
                    lastModified = DateUtil.parseDate( response.getHeader( "last-modified" ) ).getTime();
                }
                catch ( DateParseException e )
                {
                    // neglect
                }

                // reusable content locator, since it will do it's own GET on getContent() invocation.
                AhcContentLocator contentLocator =
                    new AhcContentLocator( client, itemUrl, length, lastModified, response.getContentType(), null );

                DefaultStorageFileItem result =
                    new DefaultStorageFileItem( repository, request, true /* canRead */, true /* canWrite */,
                        contentLocator );

                result.setLength( contentLocator.getLength() );

                result.setModified( contentLocator.getLastModified() );

                result.setCreated( result.getModified() );

                result.setRemoteUrl( contentLocator.getItemUrl() );

                result.getItemContext().setParentContext( request.getRequestContext() );

                return result;
            }
            else
            {
                ResponseInputStream ris = AhcContentLocator.fetchContent( client, itemUrl );

                Response response = ris.getResponse();

                // expected: 200 OK
                validateResponse( repository, request, "GET", itemUrl, response, 200 );

                long length = -1;

                try
                {
                    length = Long.parseLong( response.getHeader( "content-length" ) );
                }
                catch ( NumberFormatException e )
                {
                    // neglect
                }

                long lastModified = System.currentTimeMillis();

                try
                {
                    lastModified = DateUtil.parseDate( response.getHeader( "last-modified" ) ).getTime();
                }
                catch ( DateParseException e )
                {
                    // neglect
                }

                // non-reusable content locator, since we did a GET and we are passing in the Body
                AhcContentLocator contentLocator =
                    new AhcContentLocator( client, itemUrl, length, lastModified, response.getContentType(), ris );

                DefaultStorageFileItem result =
                    new DefaultStorageFileItem( repository, request, true /* canRead */, true /* canWrite */,
                        contentLocator );

                result.setLength( contentLocator.getLength() );

                result.setModified( contentLocator.getLastModified() );

                result.setCreated( result.getModified() );

                result.setRemoteUrl( contentLocator.getItemUrl() );

                result.getItemContext().setParentContext( request.getRequestContext() );

                return result;
            }
        }
        catch ( Exception e )
        {
            throw new RemoteStorageException( e );
        }
    }

    @Override
    public void storeItem( ProxyRepository repository, StorageItem item )
        throws UnsupportedStorageOperationException, RemoteAccessException, RemoteStorageException
    {
        if ( !( item instanceof StorageFileItem ) )
        {
            throw new UnsupportedStorageOperationException( "Storing of non-files remotely is not supported!" );
        }

        final StorageFileItem fItem = (StorageFileItem) item;

        final ResourceStoreRequest request = new ResourceStoreRequest( item );

        final URL remoteURL = getAbsoluteUrlFromBase( repository, request );

        final String itemUrl = remoteURL.toString();

        final AsyncHttpClient client = getClient( repository );

        try
        {
            Response response =
                client.preparePut( itemUrl ).setBody( new StorageFileItemBodyGenerator( fItem ) ).execute().get();

            // expected: 200 OK, 201 CREATED, 202 ACCEPTED, 204 NO_CONTENT
            validateResponse( repository, request, "PUT", itemUrl, response, 200, 201, 202, 204 );
        }
        catch ( Exception e )
        {
            throw new RemoteStorageException( e );
        }
    }

    @Override
    public void deleteItem( ProxyRepository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, UnsupportedStorageOperationException, RemoteAccessException,
        RemoteStorageException
    {
        final URL remoteURL = getAbsoluteUrlFromBase( repository, request );

        final String itemUrl = remoteURL.toString();

        final AsyncHttpClient client = getClient( repository );

        try
        {
            Response response = client.prepareDelete( itemUrl ).execute().get();

            // expected: 200 OK, 202 ACCEPTED, 204 NO_CONTENT
            validateResponse( repository, request, "DELETE", itemUrl, response, 200, 202, 204 );
        }
        catch ( Exception e )
        {
            throw new RemoteStorageException( e );
        }
    }

    protected void validateResponse( final ProxyRepository repository, final ResourceStoreRequest request,
                                     final String method, final String remoteUrl, final Response response,
                                     int... expectedCodes )
        throws ItemNotFoundException, RemoteStorageException
    {
        // maintain the S3 flag
        checkForRemotePeerAmazonS3Storage( repository, response );

        if ( AHCUtils.isAnyOfTheseStatusCodes( response, expectedCodes ) )
        {
            return;
        }

        if ( response.isRedirected() )
        {
            getLogger().info(
                String.format(
                    "Proxy repository %s (id=%s) got redirected from %s, please verify your remoteUrl is up-to-date!",
                    repository.getName(), repository.getId(), remoteUrl ) );
        }

        // 404 NotFound
        if ( 404 == response.getStatusCode() )
        {
            throw new ItemNotFoundException( request, repository );
        }

        // 401 Unauthorized
        if ( 401 == response.getStatusCode() )
        {
            throw new RemoteAuthenticationNeededException( repository, remoteUrl, response.getStatusText() );
        }

        // 403 Forbidden
        if ( 403 == response.getStatusCode() )
        {
            throw new RemoteAccessDeniedException( repository, remoteUrl, response.getStatusText() );
        }

        // anything else "unexpected"?
        throw new RemoteStorageException( String.format(
            "Coult not perform %s against Url %s, unexpected response is %s", method, remoteUrl,
            response.getStatusText() ) );
    }

    protected AsyncHttpClient getClient( final ProxyRepository repository )
        throws RemoteStorageException
    {
        RemoteStorageContext ctx = getRemoteStorageContext( repository );

        AsyncHttpClient httpClient = (AsyncHttpClient) ctx.getContextObject( CTX_KEY_CLIENT );

        return httpClient;
    }

    @Override
    protected void updateContext( ProxyRepository repository, RemoteStorageContext context )
        throws RemoteStorageException
    {
        getLogger().info(
            "Remote storage settings change detected for ProxyRepository ID=\"" + repository.getId() + "\" (\""
                + repository.getName() + "\"), updating HTTP transport..." );

        final AsyncHttpClientConfig.Builder clientConfigBuilder = ahcProvider.getAsyncHttpClient( repository, context );

        // set user agent
        clientConfigBuilder.setUserAgent( formatUserAgentString( context, repository ) );

        // handle compression
        clientConfigBuilder.setCompressionEnabled( true );

        // enable redirects
        clientConfigBuilder.setFollowRedirects( true );

        final AsyncHttpClient client = new AsyncHttpClient( clientConfigBuilder.build() );

        context.putContextObject( CTX_KEY_CLIENT, client );

        // we don't know is remote S3, we have new URL maybe so recheck is needed
        context.removeContextObject( CTX_KEY_S3_FLAG );
    }

    /**
     * Returns {@code true} if only and only if we are positive that remote peer (remote URL of passed in
     * ProxyRepository) points to a remote repository that is hosted by Amazon S3 Storage. This method will return false
     * as long as we don't make very 1st HTTP request to remote peer. After that 1st request, we retain the status until
     * ProxyRepository configuration changes. See {@link https://issues.sonatype.org/browse/NEXUS-3338} for more.
     * 
     * @param repository that needs to be checked.
     * @return true only if we know that ProxyRepository in question points to Amazon S3 storage.
     * @throws RemoteStorageException in case of some error.
     */
    public boolean isRemotePeerAmazonS3Storage( ProxyRepository repository )
        throws RemoteStorageException
    {
        RemoteStorageContext ctx = getRemoteStorageContext( repository );

        if ( ctx.hasContextObject( CTX_KEY_S3_FLAG ) )
        {
            Boolean flag = (Boolean) ctx.getContextObject( CTX_KEY_S3_FLAG );

            if ( flag != null && flag.booleanValue() )
            {
                // it is S3 if we have CTX_KEY_S3_FLAG set, the flag value is not null, and flag value is true
                // if flag is False, we know it is not S3
                // if flag is null, we still did not contact remote, so we were not able to tell yet
                return true;
            }
        }

        return false;
    }

    /**
     * Checks is remote a S3 server and puts a Boolean into remote storage context, thus preventing any further checks
     * (we check only once).
     * 
     * @param repository
     * @param response
     * @throws RemoteStorageException
     */
    protected void checkForRemotePeerAmazonS3Storage( ProxyRepository repository, Response response )
        throws RemoteStorageException
    {
        RemoteStorageContext ctx = getRemoteStorageContext( repository );

        // we already know the result, do nothing
        if ( ctx.hasContextObject( CTX_KEY_S3_FLAG ) )
        {
            return;
        }

        // for now, we check the HTTP response header "Server: AmazonS3"
        String hdr = response.getHeader( "server" );

        boolean isAmazonS3 = ( hdr != null ) && ( hdr.toLowerCase().contains( "amazons3" ) );

        if ( isAmazonS3 )
        {
            ctx.putContextObject( CTX_KEY_S3_FLAG, Boolean.TRUE );

            getLogger().warn(
                "The proxy repository \""
                    + repository.getName()
                    + "\" (ID="
                    + repository.getId()
                    + ") is backed by Amazon S3 service. This means that Nexus can't reliably detect the validity of your setup (baseUrl of proxy repository)!" );
        }
        else
        {
            ctx.putContextObject( CTX_KEY_S3_FLAG, Boolean.FALSE );
        }
    }
}
