package org.sonatype.nexus.index.updater.jetty;

import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.eclipse.jetty.client.Address;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpDestination;
import org.eclipse.jetty.client.security.ProxyAuthorization;
import org.eclipse.jetty.client.security.Realm;
import org.eclipse.jetty.client.security.RealmResolver;
import org.eclipse.jetty.http.HttpFields;
import org.sonatype.nexus.index.updater.ResourceFetcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class JettyResourceFetcher
    implements ResourceFetcher
{

    // configuration fields.
    private int maxConnections;

    private int connectionTimeoutMs = 30000; // 30 seconds

    private int transactionTimeoutMs = 60 * 30 * 1000; // 30 minutes.

    private boolean useCache;

    private ProxyInfo proxyInfo;

    private AuthenticationInfo authenticationInfo;

    private HttpFields headers;

    private int maxRedirects = 4;

    // END: configuration fields.

    // transient fields.
    private HttpClient httpClient;

    private String host;

    private String url;

    private final TransferListenerSupport listenerSupport = new TransferListenerSupport();

    public void retrieve( final String name, final File targetFile )
        throws IOException, FileNotFoundException
    {
        HttpFields exchangeHeaders = buildHeaders();

        StringBuilder getUrl = new StringBuilder( url );
        if ( getUrl.charAt( getUrl.length() - 1 ) != '/' && !name.startsWith( "/" ) )
        {
            getUrl.append( '/' );
        }
        getUrl.append( name );

        ResourceExchange exchange =
            new ResourceExchange( targetFile, exchangeHeaders, maxRedirects, getUrl.toString(), listenerSupport );

        exchange = get( exchange );
        while ( exchange.prepareForRedirect() )
        {
            exchange = get( exchange );
        }
    }

    private ResourceExchange get( final ResourceExchange exchange )
        throws IOException
    {
        String url = exchange.getOriginalUrl();
        httpClient.send( exchange );
        try
        {
            if ( !exchange.waitFor( transactionTimeoutMs ) )
            {
                listenerSupport.fireTransferError( url, new IOException( "Transaction timed out." ),
                                                   TransferEvent.REQUEST_GET );
            }
        }
        catch ( InterruptedException e )
        {
            IOException err = new IOException( "Transfer interrupted: " + e.getMessage() );
            err.initCause( e );

            throw err;
        }

        int responseStatus = exchange.getResponseStatus();
        switch ( responseStatus )
        {
            case ServerResponse.SC_OK:
            case ServerResponse.SC_NOT_MODIFIED:
            case ServerResponse.SC_MOVED_PERMANENTLY:
            case ServerResponse.SC_MOVED_TEMPORARILY:
                break;

            case ServerResponse.SC_FORBIDDEN:
                throw new IOException( "Transfer failed: [" + responseStatus + "] " + url );

            case ServerResponse.SC_UNAUTHORIZED:
                throw new IOException( "Transfer failed: Not authorized" );

            case ServerResponse.SC_PROXY_AUTHENTICATION_REQUIRED:
                throw new IOException( "Transfer failed: Not authorized by proxy" );

            case ServerResponse.SC_NOT_FOUND:
                throw new IOException( "Transfer failed: " + url + " does not exist" );

            default:
            {
                IOException ex = new IOException( "Transfer failed: [" + responseStatus + "] " + url );
                listenerSupport.fireTransferError( url, ex, TransferEvent.REQUEST_GET );

                throw ex;
            }
        }

        return exchange;
    }

    public void connect( final String id, final String url )
        throws IOException
    {
        this.url = url;
        URL u = new URL( url );
        host = u.getHost();

        httpClient = new HttpClient();

        httpClient.setConnectorType( HttpClient.CONNECTOR_SELECT_CHANNEL );
        if ( maxConnections > 0 )
        {
            httpClient.setMaxConnectionsPerAddress( maxConnections );
        }

        if ( connectionTimeoutMs > 0 )
        {
            httpClient.setTimeout( connectionTimeoutMs );
        }

        httpClient.registerListener( NtlmListener.class.getName() );

        NtlmListener.setHelper( new NtlmConnectionHelper( this ) );

        setupClient();

        try
        {
            httpClient.start();
        }
        catch ( Exception e )
        {
            try
            {
                disconnect();
            }
            catch ( IOException internalError )
            {
                // best attempt to make things right.
            }
            finally
            {
                httpClient = null;
            }

            if ( e instanceof IOException )
            {
                throw (IOException) e;
            }

            IOException err = new IOException( e.getLocalizedMessage() );
            err.initCause( e );

            throw err;
        }
    }

    public void disconnect()
        throws IOException
    {
        if ( httpClient != null )
        {
            try
            {
                httpClient.stop();
            }
            catch ( Exception e )
            {
                if ( e instanceof IOException )
                {
                    throw (IOException) e;
                }

                IOException err = new IOException( e.getLocalizedMessage() );
                err.initCause( e );

                throw err;
            }
            finally
            {
                httpClient = null;
            }
        }
    }

    public boolean isUseCache()
    {
        return useCache;
    }

    public ProxyInfo getProxyInfo()
    {
        return proxyInfo;
    }

    public AuthenticationInfo getAuthenticationInfo()
    {
        return authenticationInfo;
    }

    public HttpFields getHttpHeaders()
    {
        return headers;
    }

    protected void setupClient()
        throws IOException
    {
        if ( proxyInfo != null && proxyInfo.getHost() != null )
        {
            String proxyType = proxyInfo.getType();
            if ( !proxyType.equalsIgnoreCase( ProxyInfo.PROXY_HTTP.toLowerCase() ) )
            {
                throw new IOException( "Connection failed: " + proxyType + " is not supported" );
            }

            httpClient.setProxy( new Address( proxyInfo.getHost(), proxyInfo.getPort() ) );

            if ( proxyInfo.getUserName() != null )
            {
                httpClient.setProxyAuthentication( new ProxyAuthorization( proxyInfo.getUserName(),
                                                                           proxyInfo.getPassword() ) );
            }
        }

        final String targetHost = host;

        AuthenticationInfo authInfo = getAuthenticationInfo();
        if ( authInfo != null && authInfo.getUserName() != null )
        {
            httpClient.setRealmResolver( new RealmResolver()
            {
                public Realm getRealm( final String realmName, final HttpDestination destination, final String path )
                    throws IOException
                {
                    return new Realm()
                    {
                        public String getCredentials()
                        {
                            return getAuthenticationInfo().getPassword();
                        }

                        public String getPrincipal()
                        {
                            return getAuthenticationInfo().getUserName();
                        }

                        public String getId()
                        {
                            return targetHost;
                        }
                    };
                }
            } );
        }
    }

    public int getMaxRedirects()
    {
        return maxRedirects;
    }

    public JettyResourceFetcher setMaxRedirects( final int maxRedirects )
    {
        this.maxRedirects = maxRedirects;
        return this;
    }

    public int getMaxConnections()
    {
        return maxConnections;
    }

    public JettyResourceFetcher setMaxConnections( final int maxConnections )
    {
        this.maxConnections = maxConnections;
        return this;
    }

    public int getConnectionTimeoutMillis()
    {
        return connectionTimeoutMs;
    }

    public JettyResourceFetcher setConnectionTimeoutMillis( final int connectionTimeoutMs )
    {
        this.connectionTimeoutMs = connectionTimeoutMs;
        return this;
    }

    public HttpFields getHeaders()
    {
        return headers;
    }

    public JettyResourceFetcher setHeaders( final HttpFields headers )
    {
        this.headers = headers;
        return this;
    }

    public JettyResourceFetcher setUseCache( final boolean useCache )
    {
        this.useCache = useCache;
        return this;
    }

    public JettyResourceFetcher setProxyInfo( final ProxyInfo proxyInfo )
    {
        this.proxyInfo = proxyInfo;
        return this;
    }

    public JettyResourceFetcher setAuthenticationInfo( final AuthenticationInfo authenticationInfo )
    {
        this.authenticationInfo = authenticationInfo;
        return this;
    }

    public JettyResourceFetcher addTransferListener( final TransferListener listener )
    {
        listenerSupport.addTransferListener( listener );
        return this;
    }

    private HttpFields buildHeaders()
    {
        HttpFields result = new HttpFields();
        if ( headers != null )
        {
            result.add( headers );
        }
        else
        {
            result.add( "Accept-Encoding", "gzip" );
            if ( !useCache )
            {
                result.add( "Pragma", "no-cache" );
                result.add( "Cache-Control", "no-cache, no-store" );
            }
        }

        return result;
    }

    public int getTransactionTimeoutMillis()
    {
        return transactionTimeoutMs;
    }

    public void setTransactionTimeoutMillis( final int transactionTimeoutMs )
    {
        this.transactionTimeoutMs = transactionTimeoutMs;
    }

}
