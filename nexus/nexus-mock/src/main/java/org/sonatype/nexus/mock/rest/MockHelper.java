package org.sonatype.nexus.mock.rest;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.jsecurity.io.ResourceException;
import org.restlet.data.Request;
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

    public static HashMap<String, MockResponse> getResponseMap()
    {
        return mockResponses;
    }

    public static MockResponse getMockContentFor( String uri, Request request )
        throws ResourceException
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

    public static void expect(String uri, MockResponse mockResponse) {
        mockResponses.put(uri, mockResponse);

        List<MockResponse> list = responses.get();
        if (list == null) {
            list = new ArrayList<MockResponse>();
        }

        list.add(mockResponse);
        responses.set(list);
    }

    public static void checkAssertions() {
        List<MockResponse> list = responses.get();
        if (list != null) {
            for (MockResponse mockResponse : list) {
                mockResponse.checkAssertion();
            }
        }
    }

    public static void clearMocks() {
        List<MockResponse> list = responses.get();
        if (list != null) {
            list.clear();
        }
    }
}
