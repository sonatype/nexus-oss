package org.sonatype.nexus.mock.rest;

import java.util.HashMap;
import java.util.Map.Entry;

import org.sonatype.nexus.mock.MockListener;
import org.sonatype.nexus.mock.MockResponse;

/**
 * This class is the gate to "mock"
 *
 * @author cstamas
 */
public class MockHelper
{
    private static HashMap<String, MockResponse> mockResponses = new HashMap<String, MockResponse>();

    private static HashMap<String, MockListener> mockListeneres = new HashMap<String, MockListener>();;

    public static HashMap<String, MockResponse> getResponseMap()
    {
        return mockResponses;
    }

    public static MockResponse getMockContentFor( String uri )
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

    public static MockListener getListenerFor( String uri )
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

    public static MockResponse expect( String uri, MockResponse mockResponse )
    {
        mockResponses.put( uri, mockResponse );

        return mockResponse;
    }

    public static MockListener listen( String uri, MockListener mockListener )
    {
        mockListeneres.put( uri, mockListener );

        return mockListener;
    }

    public static void checkAssertions()
    {
        for ( MockResponse mockResponse : mockResponses.values() )
        {
            mockResponse.checkAssertion();
        }

        for ( MockListener mockListener : mockListeneres.values() )
        {
            mockListener.checkAssertion();
        }
    }

    public static void checkExecutions()
    {
        for ( Entry<String, MockResponse> entry : mockResponses.entrySet() )
        {
            if ( !entry.getValue().wasExecuted() )
            {
                throw new AssertionError( "Mock for '" + entry.getKey() + "' was never executed" );
            }
        }

        for ( Entry<String, MockListener> entry : mockListeneres.entrySet() )
        {
            if ( !entry.getValue().wasExecuted() )
            {
                throw new AssertionError( "Listener for '" + entry.getKey() + "' was never executed" );
            }
        }
    }

    public static void clearMocks()
    {
        mockResponses.clear();
        mockListeneres.clear();
    }

    public static void checkAndClean()
    {
        checkExecutions();
        checkAssertions();
        clearMocks();
    }
}
