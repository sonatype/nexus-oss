package org.sonatype.nexus.mock;

import org.restlet.data.Method;
import org.restlet.data.Status;

public class MockResponse
{
    protected Status status;

    protected Object response;

    protected Object payload;

    private AssertionError assertionFailedError;

    protected boolean executed = false;

    private Method method;

    public MockResponse( Status status, Object payload )
    {
        this.status = status;
        this.response = payload;
    }

    public MockResponse( Status status, Object payload, Method method )
    {
        this.status = status;
        this.response = payload;
        this.method = method;
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

    public void setPayload( Object payload )
        throws AssertionError
    {
        this.payload = payload;
    }

    public void setAssertionFailure( AssertionError assertionFailedError )
    {
        this.assertionFailedError = assertionFailedError;
    }

    public void checkAssertion()
    {
        if ( assertionFailedError != null )
        {
            AssertionError error = assertionFailedError;
            assertionFailedError = null; // reset so we don't KEEP throwing it on future checks
            throw error;
        }
    }

    public final boolean wasExecuted()
    {
        return executed;
    }

    public Method getMethod()
    {
        return method;
    }

}
