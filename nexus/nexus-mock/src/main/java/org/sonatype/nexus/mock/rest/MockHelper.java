package org.sonatype.nexus.mock.rest;

import java.util.ArrayList;
import java.util.List;

import org.mortbay.log.Log;
import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.MockResponse;

import com.google.common.collect.HashBiMap;

/**
 * This class is the gate to "mock"
 * 
 * @author cstamas
 */
public class MockHelper
{
    private static final Object lock = new Object();

    private static final HashBiMap<String, MockResponse> mockResponses = new HashBiMap<String, MockResponse>();

    private static final HashBiMap<String, MockListener<Object>> mockListeneres =
        new HashBiMap<String, MockListener<Object>>();;

    private static final ThreadLocal<List<MockResponse>> responses = new ThreadLocal<List<MockResponse>>();

    private static final ThreadLocal<List<MockListener<Object>>> listeners =
        new ThreadLocal<List<MockListener<Object>>>();

    public static MockResponse getMockContentFor( String uri )
    {
        synchronized ( lock )
        {
            if ( mockResponses.containsKey( uri ) )
            {
                return mockResponses.get( uri );
            }
            else
            {
                return null;
            }
        }
    }

    public static MockListener<Object> getListenerFor( String uri )
    {
        synchronized ( lock )
        {
            if ( mockListeneres.containsKey( uri ) )
            {
                return mockListeneres.get( uri );
            }
            else
            {
                return null;
            }
        }
    }

    public static MockResponse expect( String uri, MockResponse mockResponse )
    {
        synchronized ( lock )
        {
            while ( mockResponses.containsKey( uri ) || mockListeneres.containsKey( uri ) )
            {
                try
                {
                    lock.wait( 1000 );
                }
                catch ( InterruptedException e )
                {
                    Log.debug( e.getMessage(), e );
                }
            }

            mockResponses.put( uri, mockResponse );
            if ( responses.get() == null )
            {
                responses.set( new ArrayList<MockResponse>() );
            }
            responses.get().add( mockResponse );

            return mockResponse;
        }
    }

    @SuppressWarnings( "unchecked" )
    public static <E> MockListener<E> listen( String uri, MockListener<E> mockListener )
    {
        synchronized ( lock )
        {
            while ( mockResponses.containsKey( uri ) || mockListeneres.containsKey( uri ) )
            {
                try
                {
                    lock.wait( 1000 );
                }
                catch ( InterruptedException e )
                {
                    Log.debug( e.getMessage(), e );
                }
            }

            mockListeneres.put( uri, (MockListener<Object>) mockListener );
            if ( listeners.get() == null )
            {
                listeners.set( new ArrayList<MockListener<Object>>() );
            }
            listeners.get().add( (MockListener<Object>) mockListener );

            return mockListener;
        }
    }

    public static void checkAssertions()
    {
        if ( responses.get() != null )
        {
            for ( MockResponse mockResponse : responses.get() )
            {
                mockResponse.checkAssertion();
            }
        }

        if ( listeners.get() != null )
        {
            for ( MockListener<Object> mockListener : listeners.get() )
            {
                mockListener.checkAssertion();
            }
        }
    }

    public static void checkExecutions()
    {
        if ( responses.get() != null )
        {
            for ( MockResponse entry : responses.get() )
            {
                if ( !entry.wasExecuted() )
                {
                    String uri = mockResponses.inverse().get( entry );
                    throw new AssertionError( "Mock for '" + uri + "' was never executed" );
                }
            }
        }

        if ( listeners.get() != null )
        {
            for ( MockListener<Object> entry : listeners.get() )
            {
                if ( !entry.wasExecuted() )
                {
                    String uri = mockListeneres.inverse().get( entry );
                    throw new AssertionError( "Listener for '" + uri + "' was never executed" );
                }
            }
        }
    }

    public static void clearMocks()
    {
        synchronized ( lock )
        {
            if ( responses.get() != null )
            {
                for ( MockResponse entry : responses.get() )
                {
                    mockResponses.inverse().remove( entry );
                }
            }
            if ( listeners.get() != null )
            {
                for ( MockListener<Object> entry : listeners.get() )
                {
                    mockListeneres.inverse().remove( entry );
                }
            }

            lock.notifyAll();
        }
    }

    public static void checkAndClean()
    {
        try
        {
            checkExecutions();
            checkAssertions();
        }
        finally
        {
            clearMocks();
        }
    }
}
