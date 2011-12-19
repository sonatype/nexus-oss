/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.p2.repository;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.mime.MimeSupport;
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
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.utils.UserAgentBuilder;

@Named( "mock" )
@Singleton
public class MockRemoteStorage
    extends AbstractRemoteRepositoryStorage
{

    private Set<String> validUrls = new HashSet<String>();

    private Set<String> downUrls = new HashSet<String>();

    private final List<MockRequestRecord> requests = new LinkedList<MockRequestRecord>();

    @Inject
    protected MockRemoteStorage( final UserAgentBuilder userAgentBuilder,
                                 final ApplicationStatusSource applicationStatusSource, final MimeSupport mimeSupport )
    {
        super( userAgentBuilder, applicationStatusSource, mimeSupport );
    }

    @Override
    protected void updateContext( final ProxyRepository repository, final RemoteStorageContext context )
        throws RemoteStorageException
    {
    }

    @Override
    public boolean containsItem( final long newerThen, final ProxyRepository repository,
                                 final ResourceStoreRequest request )
        throws RemoteAccessException, RemoteStorageException
    {
        // TODO: not sure what to do here.
        return false;
    }

    @Override
    public void deleteItem( final ProxyRepository repository, final ResourceStoreRequest request )
        throws ItemNotFoundException, UnsupportedStorageOperationException, RemoteAccessException,
        RemoteStorageException
    {
        throw new UnsupportedStorageOperationException( "This is a mock, no deleting!" );
    }

    @Override
    public String getProviderId()
    {
        return "mock";
    }

    @Override
    public boolean isReachable( final ProxyRepository repository, final ResourceStoreRequest request )
        throws RemoteAccessException, RemoteStorageException
    {
        // TODO: not sure what to do here, this must be for the status check
        return false;
    }

    @Override
    public AbstractStorageItem retrieveItem( final ProxyRepository repository, final ResourceStoreRequest request,
                                             final String baseUrl )
        throws ItemNotFoundException, RemoteAccessException, RemoteStorageException
    {
        requests.add( new MockRequestRecord( repository, request, baseUrl ) );

        System.out.println( "request: " + request.getRequestPath() );
        System.out.println( "baseUrl: " + baseUrl );

        final String requestUrl = baseUrl.substring( 0, baseUrl.length() - 1 ) + request.getRequestPath();

        if ( downUrls.contains( baseUrl ) )
        {
            throw new RemoteStorageException( "Mock " + baseUrl + " is expected to be down." );
        }

        if ( validUrls.contains( requestUrl ) )
        {
            return new DefaultStorageFileItem( repository, request, true, false, new ByteArrayContentLocator(
                "Mock".getBytes(), "plain/text" ) );
        }

        // else
        throw new ItemNotFoundException( request );
    }

    @Override
    public void storeItem( final ProxyRepository repository, final StorageItem item )
        throws UnsupportedStorageOperationException, RemoteAccessException, RemoteStorageException
    {
        throw new UnsupportedStorageOperationException( "This is a mock, no writing!" );
    }

    @Override
    public void validateStorageUrl( final String url )
        throws RemoteStorageException
    {
        // do nothing
    }

    public List<MockRequestRecord> getRequests()
    {
        return requests;
    }

    public void setValidUrls( final Set<String> validUrls )
    {
        this.validUrls = validUrls;
    }

    public Set<String> getValidUrls()
    {
        return validUrls;
    }

    public void setDownUrls( final Set<String> downUrls )
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

        public MockRequestRecord( final ProxyRepository repository, final ResourceStoreRequest request,
                                  final String baseUrl )
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
        public boolean equals( final Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            if ( obj == null )
            {
                return false;
            }
            if ( getClass() != obj.getClass() )
            {
                return false;
            }
            final MockRequestRecord other = (MockRequestRecord) obj;
            if ( baseUrl == null )
            {
                if ( other.baseUrl != null )
                {
                    return false;
                }
            }
            else if ( !baseUrl.equals( other.baseUrl ) )
            {
                return false;
            }
            if ( repository == null )
            {
                if ( other.repository != null )
                {
                    return false;
                }
            }
            else if ( !repository.equals( other.repository ) )
            {
                return false;
            }
            if ( request == null )
            {
                if ( other.request != null )
                {
                    return false;
                }
            }
            else if ( !request.equals( other.request ) )
            {
                return false;
            }
            return true;
        }

    }

}
