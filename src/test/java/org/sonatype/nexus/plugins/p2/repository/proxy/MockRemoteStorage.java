/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository.proxy;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.RemoteStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ByteArrayContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.remote.AbstractRemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteRepositoryStorage;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

@Component( role = RemoteRepositoryStorage.class, hint = "mock" )
public class MockRemoteStorage
    extends AbstractRemoteRepositoryStorage
{

    private Set<String> validUrls = new HashSet<String>();
    private Set<String> downUrls = new HashSet<String>();
    
    private List<MockRequestRecord> requests = new LinkedList<MockRequestRecord>();

    @Override
    protected void updateContext( ProxyRepository repository, RemoteStorageContext context )
        throws RemoteStorageException
    {
    }

    public boolean containsItem( long newerThen, ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteAccessException, RemoteStorageException
    {
        // TODO: not sure what to do here.
        return false;
    }

    public void deleteItem( ProxyRepository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, UnsupportedStorageOperationException, RemoteAccessException, RemoteStorageException
    {
        throw new UnsupportedStorageOperationException( "This is a mock, no deleting!" );
    }

    public String getProviderId()
    {
        return "mock";
    }

    public boolean isReachable( ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteAccessException, RemoteStorageException
    {
        // TODO: not sure what to do here, this must be for the status check
        return false;
    }

    public AbstractStorageItem retrieveItem( ProxyRepository repository, ResourceStoreRequest request, String baseUrl )
        throws ItemNotFoundException, RemoteAccessException, RemoteStorageException
    {
        this.requests.add( new MockRequestRecord( repository, request, baseUrl ) );

        System.out.println( "request: " + request.getRequestPath() );
        System.out.println( "baseUrl: " + baseUrl );

        String requestUrl = baseUrl.substring( 0, baseUrl.length()-1 ) + request.getRequestPath();
        
        if( this.downUrls.contains( baseUrl ) )
        {
            throw new RemoteStorageException( "Mock "+baseUrl+" is expected to be down." );
        }

        if( this.validUrls.contains( requestUrl ) )
        {
            return new DefaultStorageFileItem( repository, request, true, false, new ByteArrayContentLocator( "Mock".getBytes(), "plain/text" ) );
        }
        
        // else
        throw new ItemNotFoundException(request);
    }

    public void storeItem( ProxyRepository repository, StorageItem item )
        throws UnsupportedStorageOperationException, RemoteAccessException, RemoteStorageException
    {
        throw new UnsupportedStorageOperationException( "This is a mock, no writing!" );
    }

    public void validateStorageUrl( String url )
        throws RemoteStorageException
    {
        // do nothing
    }

    public List<MockRequestRecord> getRequests()
    {
        return requests;
    }

    public void setValidUrls( Set<String> validUrls )
    {
        this.validUrls = validUrls;
    }

    public Set<String> getValidUrls()
    {
        return validUrls;
    }

    public void setDownUrls( Set<String> downUrls )
    {
        this.downUrls = downUrls;
    }

    public Set<String> getDownUrls()
    {
        return downUrls;
    }

    static class MockRequestRecord
    {
        ProxyRepository repository;

        ResourceStoreRequest request;

        String baseUrl;

        public MockRequestRecord( ProxyRepository repository, ResourceStoreRequest request, String baseUrl )
        {
            this.repository = repository;
            this.request = request;
            this.baseUrl = baseUrl;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( ( baseUrl == null ) ? 0 : baseUrl.hashCode() );
            result = prime * result + ( ( repository == null ) ? 0 : repository.hashCode() );
            result = prime * result + ( ( request == null ) ? 0 : request.hashCode() );
            return result;
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            MockRequestRecord other = (MockRequestRecord) obj;
            if ( baseUrl == null )
            {
                if ( other.baseUrl != null )
                    return false;
            }
            else if ( !baseUrl.equals( other.baseUrl ) )
                return false;
            if ( repository == null )
            {
                if ( other.repository != null )
                    return false;
            }
            else if ( !repository.equals( other.repository ) )
                return false;
            if ( request == null )
            {
                if ( other.request != null )
                    return false;
            }
            else if ( !request.equals( other.request ) )
                return false;
            return true;
        }

    }

}
