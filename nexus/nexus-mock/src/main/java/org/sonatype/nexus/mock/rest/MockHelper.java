package org.sonatype.nexus.mock.rest;

import java.util.HashMap;

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

}
