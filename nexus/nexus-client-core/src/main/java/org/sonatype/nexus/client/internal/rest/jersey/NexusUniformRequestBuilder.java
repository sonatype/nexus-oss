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
package org.sonatype.nexus.client.internal.rest.jersey;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Locale;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;

import org.sonatype.nexus.client.core.BadRequestException;
import org.sonatype.nexus.client.core.NotFoundException;
import org.sonatype.nexus.client.core.spi.rest.jersey.UniformRequestBuilder;
import org.sonatype.nexus.client.internal.msg.ErrorResponse;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

/**
 * @since 2.3
 */
public class NexusUniformRequestBuilder
    implements UniformRequestBuilder
{

    private final WebResource.Builder delegate;

    public NexusUniformRequestBuilder( final WebResource.Builder delegate )
    {
        this.delegate = checkNotNull( delegate );
    }

    @Override
    public void delete()
        throws UniformInterfaceException, ClientHandlerException
    {
        handle( new Operation<Object>()
        {
            @Override
            public Object perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                delegate.delete();
                return null;
            }
        } );
    }

    @Override
    public <T> T delete( final Class<T> c )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.delete( c );
            }
        } );
    }

    @Override
    public <T> T delete( final Class<T> c, final Object requestEntity )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.delete( c, requestEntity );
            }
        } );
    }

    @Override
    public <T> T delete( final GenericType<T> gt )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.delete( gt );
            }
        } );
    }

    @Override
    public <T> T delete( final GenericType<T> gt, final Object requestEntity )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.delete( gt, requestEntity );
            }
        } );
    }

    @Override
    public void delete( final Object requestEntity )
        throws UniformInterfaceException, ClientHandlerException
    {
        handle( new Operation<Object>()
        {
            @Override
            public Object perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                delegate.delete( requestEntity );
                return null;
            }
        } );
    }

    @Override
    public <T> T get( final Class<T> c )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.get( c );
            }
        } );
    }

    @Override
    public <T> T get( final GenericType<T> gt )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.get( gt );
            }
        } );
    }

    @Override
    public ClientResponse head()
        throws ClientHandlerException
    {
        return handle( new Operation<ClientResponse>()
        {
            @Override
            public ClientResponse perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.head();
            }
        } );
    }

    @Override
    public void method( final String method )
        throws UniformInterfaceException, ClientHandlerException
    {
        handle( new Operation<Object>()
        {
            @Override
            public Object perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                delegate.method( method );
                return null;
            }
        } );
    }

    @Override
    public <T> T method( final String method, final Class<T> c )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.method( method, c );
            }
        } );
    }

    @Override
    public <T> T method( final String method, final Class<T> c, final Object requestEntity )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.method( method, c, requestEntity );
            }
        } );
    }

    @Override
    public <T> T method( final String method, final GenericType<T> gt )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.method( method, gt );
            }
        } );
    }

    @Override
    public <T> T method( final String method, final GenericType<T> gt, final Object requestEntity )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.method( method, gt, requestEntity );
            }
        } );
    }

    @Override
    public void method( final String method, final Object requestEntity )
        throws UniformInterfaceException, ClientHandlerException
    {
        handle( new Operation<Object>()
        {
            @Override
            public Object perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                delegate.method( method, requestEntity );
                return null;
            }
        } );
    }

    @Override
    public <T> T options( final Class<T> c )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.options( c );
            }
        } );
    }

    @Override
    public <T> T options( final GenericType<T> gt )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.options( gt );
            }
        } );
    }

    @Override
    public void post()
        throws UniformInterfaceException, ClientHandlerException
    {
        handle( new Operation<Object>()
        {
            @Override
            public Object perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                delegate.post();
                return null;
            }
        } );
    }

    @Override
    public <T> T post( final Class<T> c )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.post( c );
            }
        } );
    }

    @Override
    public <T> T post( final Class<T> c, final Object requestEntity )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.post( c, requestEntity );
            }
        } );
    }

    @Override
    public <T> T post( final GenericType<T> gt )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.post( gt );
            }
        } );
    }

    @Override
    public <T> T post( final GenericType<T> gt, final Object requestEntity )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.post( gt, requestEntity );
            }
        } );
    }

    @Override
    public void post( final Object requestEntity )
        throws UniformInterfaceException, ClientHandlerException
    {
        handle( new Operation<Object>()
        {
            @Override
            public Object perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                delegate.post( requestEntity );
                return null;
            }
        } );
    }

    @Override
    public void put()
        throws UniformInterfaceException, ClientHandlerException
    {
        handle( new Operation<Object>()
        {
            @Override
            public Object perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                delegate.put();
                return null;
            }
        } );
    }

    @Override
    public <T> T put( final Class<T> c )
        throws UniformInterfaceException, ClientHandlerException
    {

        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.put( c );
            }
        } );
    }

    @Override
    public <T> T put( final Class<T> c, final Object requestEntity )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.put( c, requestEntity );
            }
        } );
    }

    @Override
    public <T> T put( final GenericType<T> gt )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.put( gt );
            }
        } );
    }

    @Override
    public <T> T put( final GenericType<T> gt, final Object requestEntity )
        throws UniformInterfaceException, ClientHandlerException
    {
        return handle( new Operation<T>()
        {
            @Override
            public T perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                return delegate.put( gt, requestEntity );
            }
        } );
    }

    @Override
    public void put( final Object requestEntity )
        throws UniformInterfaceException, ClientHandlerException
    {
        handle( new Operation<Object>()
        {
            @Override
            public Object perform()
                throws UniformInterfaceException, ClientHandlerException
            {
                delegate.put( requestEntity );
                return null;
            }
        } );
    }

    @Override
    public UniformRequestBuilder entity( final Object entity )
    {
        delegate.entity( entity );
        return this;
    }

    @Override
    public UniformRequestBuilder entity( final Object entity, final MediaType type )
    {
        delegate.entity( entity, type );
        return this;
    }

    @Override
    public UniformRequestBuilder entity( final Object entity, final String type )
    {
        delegate.entity( entity, type );
        return this;
    }

    @Override
    public UniformRequestBuilder type( final MediaType type )
    {
        delegate.type( type );
        return this;
    }

    @Override
    public UniformRequestBuilder type( final String type )
    {
        delegate.type( type );
        return this;
    }

    @Override
    public UniformRequestBuilder accept( final MediaType... types )
    {
        delegate.accept( types );
        return this;
    }

    @Override
    public UniformRequestBuilder accept( final String... types )
    {
        delegate.accept( types );
        return this;
    }

    @Override
    public UniformRequestBuilder acceptLanguage( final Locale... locales )
    {
        delegate.acceptLanguage( locales );
        return this;
    }

    @Override
    public UniformRequestBuilder acceptLanguage( final String... locales )
    {
        delegate.acceptLanguage( locales );
        return this;
    }

    @Override
    public UniformRequestBuilder cookie( final Cookie cookie )
    {
        delegate.cookie( cookie );
        return this;
    }

    @Override
    public UniformRequestBuilder header( final String name, final Object value )
    {
        delegate.header( name, value );
        return this;
    }

    private static interface Operation<T>
    {

        T perform()
            throws UniformInterfaceException, ClientHandlerException;

    }

    private <T> T handle( final Operation<T> callable )
    {
        try
        {
            return callable.perform();
        }
        catch ( final UniformInterfaceException e )
        {
            final ClientResponse response = e.getResponse();
            try
            {
                if ( response.getStatus() == ClientResponse.Status.BAD_REQUEST.getStatusCode() )
                {
                    if ( response.hasEntity() )
                    {
                        final ErrorResponse errors = response.getEntity( ErrorResponse.class );
                        throw new BadRequestException( errors );
                    }
                }
                else if ( response.getStatus() == ClientResponse.Status.NOT_FOUND.getStatusCode() )
                {
                    throw new NotFoundException();
                }
                throw e;
            }
            finally
            {
                response.close();
            }
        }
        catch ( final ClientHandlerException e )
        {
            throw e;
        }
    }

}
