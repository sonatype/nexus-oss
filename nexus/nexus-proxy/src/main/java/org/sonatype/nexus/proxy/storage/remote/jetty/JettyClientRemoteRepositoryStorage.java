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
package org.sonatype.nexus.proxy.storage.remote.jetty;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.RemoteAuthenticationNeededException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.remote.AbstractRemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

public class JettyClientRemoteRepositoryStorage
    extends AbstractRemoteRepositoryStorage
{
    public static final String CTX_KEY = "jettyClient";

    @Override
    protected void updateContext( ProxyRepository repository, RemoteStorageContext context )
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

            context.putRemoteConnectionContextObject( CTX_KEY, jettyClient );
        }
        catch ( Exception e )
        {
            throw new StorageException( "Could not configure Jetty HttpClient instance", e );
        }
    }

    public boolean isReachable( ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteAuthenticationNeededException,
            RemoteAccessException,
            StorageException
    {
        boolean result = false;

        try
        {
            request.pushRequestPath( RepositoryItemUid.PATH_ROOT );

            result = containsItem( repository, request );
        }
        finally
        {
            request.popRequestPath();
        }

        return result;
    }

    public boolean containsItem( long newerThen, ProxyRepository repository, ResourceStoreRequest request )
        throws StorageException
    {
        RemoteStorageContext rsc = getRemoteStorageContext( repository );

        ContainsHttpExchange contains = new ContainsHttpExchange( repository, request, newerThen );

        setUpExchange( contains, rsc, repository, request );

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

    public AbstractStorageItem retrieveItem( ProxyRepository repository, ResourceStoreRequest request, String baseUrl )
        throws ItemNotFoundException,
            StorageException
    {
        RemoteStorageContext rsc = getRemoteStorageContext( repository );

        RetrieveHttpExchange retrieve = new RetrieveHttpExchange( repository, request );

        setUpExchange( retrieve, rsc, repository, request );

        executeRequest( rsc, retrieve );

        return retrieve.getStorageItem();
    }

    public void storeItem( ProxyRepository repository, StorageItem item )
        throws UnsupportedStorageOperationException,
            StorageException
    {
        RemoteStorageContext rsc = getRemoteStorageContext( repository );

        StoreHttpExchange store = new StoreHttpExchange( repository, item );

        setUpExchange( store, rsc, repository, new ResourceStoreRequest( item ) );

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

    public void deleteItem( ProxyRepository repository, ResourceStoreRequest request )
        throws ItemNotFoundException,
            UnsupportedStorageOperationException,
            StorageException
    {
        RemoteStorageContext rsc = getRemoteStorageContext( repository );

        DeleteHttpExchange delete = new DeleteHttpExchange( repository, request );

        setUpExchange( delete, rsc, repository, request );

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

    protected void setUpExchange( AbstractNexusExchange exchange, RemoteStorageContext rsc, ProxyRepository repository,
        ResourceStoreRequest request )
        throws StorageException
    {
        URL requestURL = getAbsoluteUrlFromBase( repository, request );

        StringBuffer requestStringBuffer = new StringBuffer( requestURL.toString() );

        CRemoteConnectionSettings rcs = getRemoteConnectionSettings( rsc );

        if ( rcs != null )
        {
            exchange.setRequestHeader( HttpHeaders.USER_AGENT, formatUserAgentString( rsc, repository ) );

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
        HttpClient jettyClient = (HttpClient) context.getRemoteConnectionContextObject( CTX_KEY );

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

    public String getName()
    {
        return CTX_KEY;
    }
}
