package org.sonatype.nexus.proxy.storage.remote.jetty;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.client.Address;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.jetty.client.HttpDestination;
import org.mortbay.jetty.client.security.ProxyAuthorization;
import org.mortbay.jetty.client.security.Realm;
import org.mortbay.jetty.client.security.RealmResolver;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.remote.AbstractRemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public class JettyClientRemoteRepositoryStorage
    extends AbstractRemoteRepositoryStorage
{
    public static final String JETTY_CLIENT_KEY = "jettyClient";

    @Override
    protected void updateContext( Repository repository, RemoteStorageContext context )
        throws StorageException
    {
        try
        {
            HttpClient jettyClient = new HttpClient();

            final CRemoteConnectionSettings rcs = getRemoteConnectionSettings( context );

            if ( rcs != null )
            {
                jettyClient.setMaxRetries( rcs.getRetrievalRetryCount() );

                jettyClient.setTimeout( rcs.getConnectionTimeout() );
            }

            final CRemoteAuthentication ra = getRemoteAuthenticationSettings( context );

            if ( ra != null && !StringUtils.isEmpty( ra.getUsername() ) )
            {
                if ( !StringUtils.isEmpty( ra.getNtlmDomain() ) || !StringUtils.isEmpty( ra.getNtlmHost() ) )
                {
                    // NTLM not supported
                    throw new StorageException( "NTLM authentication not supported!" );
                }

                jettyClient.setRealmResolver( new RealmResolver()
                {
                    public Realm getRealm( String realmName, HttpDestination destination, String path )
                        throws IOException
                    {
                        return new Realm()
                        {
                            public String getPrincipal()
                            {
                                return ra.getUsername();
                            }

                            public String getCredentials()
                            {
                                return ra.getPassword();
                            }

                            public String getId()
                            {
                                return ra.getUsername();
                            }
                        };
                    }

                } );
            }

            final CRemoteHttpProxySettings hps = getRemoteHttpProxySettings( context );

            if ( hps != null && !StringUtils.isEmpty( hps.getProxyHostname() ) )
            {
                // we have proxy
                jettyClient.setProxy( new Address( hps.getProxyHostname(), hps.getProxyPort() ) );

                if ( hps.getAuthentication() != null && !StringUtils.isEmpty( hps.getAuthentication().getUsername() ) )
                {
                    // we have proxy auth
                    if ( !StringUtils.isEmpty( hps.getAuthentication().getNtlmDomain() )
                        || !StringUtils.isEmpty( hps.getAuthentication().getNtlmHost() ) )
                    {
                        // NTLM not supported
                        throw new StorageException( "NTLM not supported for HTTP Proxy authentication!" );
                    }

                    jettyClient.setProxyAuthentication( new ProxyAuthorization(
                        hps.getAuthentication().getUsername(),
                        hps.getAuthentication().getPassword() ) );
                }

            }

            jettyClient.start();

            context.putRemoteConnectionContextObject( JETTY_CLIENT_KEY, jettyClient );
        }
        catch ( Exception e )
        {
            throw new StorageException( "Could not configure Jetty HttpClient instance", e );
        }
    }

    public boolean containsItem( RepositoryItemUid uid, long newerThen, Map<String, Object> context )
        throws StorageException
    {
        RemoteStorageContext rsc = getRemoteStorageContext( uid.getRepository() );

        ContainsHttpExchange contains = new ContainsHttpExchange( uid, newerThen );

        setUpExchange( contains, rsc );

        executeRequest( rsc, contains );

        try
        {
            return contains.isContained();
        }
        catch ( ItemNotFoundException e )
        {
            return false;
        }
    }

    public AbstractStorageItem retrieveItem( RepositoryItemUid uid, Map<String, Object> context )
        throws ItemNotFoundException,
            StorageException
    {
        RemoteStorageContext rsc = getRemoteStorageContext( uid.getRepository() );

        RetrieveHttpExchange retrieve = new RetrieveHttpExchange( uid );

        setUpExchange( retrieve, rsc );

        executeRequest( rsc, retrieve );

        return retrieve.getStorageItem();
    }

    public void storeItem( StorageItem item )
        throws UnsupportedStorageOperationException,
            StorageException
    {
        RemoteStorageContext rsc = getRemoteStorageContext( item.getRepositoryItemUid().getRepository() );

        StoreHttpExchange store = new StoreHttpExchange( item );

        setUpExchange( store, rsc );

        executeRequest( rsc, store );

        try
        {
            store.isSuccesful();
        }
        catch ( ItemNotFoundException e )
        {
            throw new StorageException( "Server responded with 404 to a " + store.getMethod() + " method!", e );
        }
    }

    public void deleteItem( RepositoryItemUid uid, Map<String, Object> context )
        throws ItemNotFoundException,
            UnsupportedStorageOperationException,
            StorageException
    {
        RemoteStorageContext rsc = getRemoteStorageContext( uid.getRepository() );

        DeleteHttpExchange delete = new DeleteHttpExchange( uid );

        setUpExchange( delete, rsc );

        executeRequest( rsc, delete );

        delete.isSuccesful();
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

    // ----

    protected void setUpExchange( AbstractNexusExchange exchange, RemoteStorageContext context )
        throws StorageException
    {
        URL requestURL = getAbsoluteUrlFromBase( exchange.getRepositoryItemUid() );

        StringBuffer requestStringBuffer = new StringBuffer( requestURL.toString() );

        CRemoteConnectionSettings rcs = getRemoteConnectionSettings( context );

        if ( rcs != null )
        {
            if ( !StringUtils.isEmpty( rcs.getUserAgentString() ) )
            {
                exchange.setRequestHeader( HttpHeaders.USER_AGENT, getRemoteConnectionSettings( context )
                    .getUserAgentString() );
            }

            if ( !StringUtils.isEmpty( rcs.getQueryString() ) )
            {
                if ( requestStringBuffer.lastIndexOf( "?" ) > -1 )
                {
                    requestStringBuffer.append( "&" );
                }
                else
                {
                    requestStringBuffer.append( "?" );
                }

                requestStringBuffer.append( rcs.getQueryString() );
            }
        }

        exchange.setURL( requestStringBuffer.toString() );
    }

    protected void executeRequest( RemoteStorageContext context, AbstractNexusExchange exchange )
        throws StorageException
    {
        HttpClient jettyClient = (HttpClient) context.getRemoteConnectionContextObject( JETTY_CLIENT_KEY );

        try
        {
            jettyClient.send( exchange );

            // TODO: handle redirections
        }
        catch ( IOException e )
        {
            throw new StorageException( "Could not send HttpExchange!", e );
        }
    }
}
