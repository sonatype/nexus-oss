package org.sonatype.nexus.proxy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RemoteAccessException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
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

    private Map<String, String> validUrlContentMap = new HashMap<String, String>();
    private Map<String, Integer> valueUrlFailConfigMap = new HashMap<String, Integer>();
    private Map<String, Integer> valueUrlFailResultMap = new HashMap<String, Integer>();

    private Set<String> downUrls = new HashSet<String>();

    private List<MockRequestRecord> requests = new LinkedList<MockRequestRecord>();

    @Override
    protected void updateContext( ProxyRepository repository, RemoteStorageContext context )
        throws StorageException
    {
    }

    public boolean containsItem( long newerThen, ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteAccessException, StorageException
    {
        // TODO: not sure what to do here.
        return false;
    }

    public void deleteItem( ProxyRepository repository, ResourceStoreRequest request )
        throws ItemNotFoundException, UnsupportedStorageOperationException, RemoteAccessException, StorageException
    {
        throw new UnsupportedStorageOperationException( "This is a mock, no deleting!" );
    }

    public String getProviderId()
    {
        return "mock";
    }

    public boolean isReachable( ProxyRepository repository, ResourceStoreRequest request )
        throws RemoteAccessException, StorageException
    {
        // TODO: not sure what to do here, this must be for the status check
        return false;
    }

    public AbstractStorageItem retrieveItem( ProxyRepository repository, ResourceStoreRequest request, String baseUrl )
        throws ItemNotFoundException, RemoteAccessException, StorageException
    {
        this.requests.add( new MockRequestRecord( repository, request, baseUrl ) );

//        System.out.println( "request: " + request.getRequestPath() );
//        System.out.println( "baseUrl: " + baseUrl );

        String requestUrl = baseUrl.substring( 0, baseUrl.length() - 1 ) + request.getRequestPath();

        if( this.valueUrlFailConfigMap.containsKey( requestUrl ) )
        {
            int expectedFailCount = this.valueUrlFailConfigMap.get( requestUrl );
            int actualFailCount = 0;
            if( this.valueUrlFailResultMap.containsKey( requestUrl ) )
            {
                actualFailCount = this.valueUrlFailResultMap.get( requestUrl );
            }
             
            if( actualFailCount < expectedFailCount )
            {
                this.valueUrlFailResultMap.put( requestUrl, actualFailCount + 1 );
                throw new StorageException( "Mock Remote Storage is pretending to be down.");
            }
        }
        
        if ( this.downUrls.contains( baseUrl ) )
        {
            throw new StorageException( "Mock " + baseUrl + " is expected to be down." );
        }

        if ( this.validUrlContentMap.containsKey( requestUrl ) )
        {
            return new DefaultStorageFileItem( repository, request, true, false,
                                               new ByteArrayContentLocator( this.validUrlContentMap.get( requestUrl ).getBytes(), "plain/text" ) );
        }

        // else
        throw new ItemNotFoundException( request );
    }

    public void storeItem( ProxyRepository repository, StorageItem item )
        throws UnsupportedStorageOperationException, RemoteAccessException, StorageException
    {
        throw new UnsupportedStorageOperationException( "This is a mock, no writing!" );
    }

    public void validateStorageUrl( String url )
        throws StorageException
    {
        // do nothing
    }

    public List<MockRequestRecord> getRequests()
    {
        return requests;
    }

    public Map<String, String> getValidUrlContentMap()
    {
        return validUrlContentMap;
    }

    public void setValidUrlContentMap( Map<String, String> validUrlContentMap )
    {
        this.validUrlContentMap = validUrlContentMap;
    }

    public void setDownUrls( Set<String> downUrls )
    {
        this.downUrls = downUrls;
    }

    public Set<String> getDownUrls()
    {
        return downUrls;
    }

    public void setValueUrlFailConfigMap( Map<String, Integer> valueUrlFailConfigMap )
    {
        this.valueUrlFailConfigMap = valueUrlFailConfigMap;
    }

    public Map<String, Integer> getValueUrlFailConfigMap()
    {
        return valueUrlFailConfigMap;
    }

    public static class MockRequestRecord
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
            else if ( !request.getRequestPath().equals( other.request.getRequestPath() ) )
                return false;
            return true;
        }

    }

}
