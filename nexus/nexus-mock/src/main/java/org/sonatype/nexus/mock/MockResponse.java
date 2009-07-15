package org.sonatype.nexus.mock;

import junit.framework.AssertionFailedError;

import org.restlet.data.Status;

public class MockResponse
{
    protected Status status;

    protected Object response;

    protected Object payload;

    private AssertionError assertionFailedError;

    protected boolean executed = false;

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

    public void setPayload( Object payload )
        throws AssertionFailedError
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
}
