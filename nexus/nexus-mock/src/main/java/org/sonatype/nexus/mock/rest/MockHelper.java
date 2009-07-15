package org.sonatype.nexus.mock.rest;

import java.util.HashMap;

import org.junit.Assert;
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
        for ( MockResponse mockResponse : mockResponses.values() )
        {
            Assert.assertTrue( "Mock was never executed", mockResponse.wasExecuted() );
        }

        for ( MockListener mockListener : mockListeneres.values() )
        {
            Assert.assertTrue( "Listener was never executed", mockListener.wasExecuted() );
        }
    }

    public static void clearMocks()
    {
        mockResponses.clear();
        mockListeneres.clear();
    }
}
