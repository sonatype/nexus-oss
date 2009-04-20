package org.sonatype.nexus.mock;

import org.restlet.data.Status;

public class MockResponse
{
    private Status status;

    private Object response;

    public MockResponse( Status status, Object payload )
    {
        this.status = status;

        this.response = payload;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus( Status status )
    {
        this.status = status;
    }

    public Object getResponse()
    {
        return response;
    }

    public void setResponse( Object response )
    {
        this.response = response;
    }
}
