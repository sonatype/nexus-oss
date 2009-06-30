package org.sonatype.nexus.mock.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private static ThreadLocal<List<MockResponse>> responses = new ThreadLocal<List<MockResponse>>();

    private static HashMap<String, MockListener> mockListeneres = new HashMap<String, MockListener>();;

    private static ThreadLocal<List<MockListener>> listeners = new ThreadLocal<List<MockListener>>();

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

    public static void expect( String uri, MockResponse mockResponse )
    {
        mockResponses.put( uri, mockResponse );

        List<MockResponse> list = responses.get();
        if ( list == null )
        {
            list = new ArrayList<MockResponse>();
        }

        list.add( mockResponse );
        responses.set( list );
    }

    public static MockListener listen( String uri, MockListener mockListener )
    {
        mockListeneres.put( uri, mockListener );

        List<MockListener> list = listeners.get();
        if ( list == null )
        {
            list = new ArrayList<MockListener>();
        }

        list.add( mockListener );
        listeners.set( list );

        return mockListener;
    }

    public static void checkAssertions()
    {
        List<MockResponse> resps = responses.get();
        if ( resps != null )
        {
            for ( MockResponse mockResponse : resps )
            {
                mockResponse.checkAssertion();
            }
        }

        List<MockListener> list = listeners.get();
        if ( list != null )
        {
            for ( MockListener mockListeber : list )
            {
                mockListeber.checkAssertion();
            }
        }
    }

    public static void clearMocks()
    {
        List<MockResponse> list = responses.get();
        if ( list != null )
        {
            list.clear();
        }
    }
}
