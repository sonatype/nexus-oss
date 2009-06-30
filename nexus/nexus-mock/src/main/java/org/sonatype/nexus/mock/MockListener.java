package org.sonatype.nexus.mock;

import org.restlet.resource.ResourceException;

public abstract class MockListener
{

    private AssertionError assertionFailedError;

    private ResourceException error;

    private Object payload;

    private Object result;

    public final void checkAssertion()
    {
        if ( assertionFailedError != null )
        {
            AssertionError error = assertionFailedError;
            assertionFailedError = null; // reset so we don't KEEP throwing it on future checks
            throw error;
        }
    }

    public final ResourceException getError()
    {
        return error;
    }

    public final Object getPayload()
    {
        return payload;
    }

    public final Object getResult()
    {
        return result;
    }

    public void onError( ResourceException e )
    {
        // to be overwritten
    }

    public void onPayload( Object payload )
    {
        // to be overwritten
    }

    public void onResult( Object result )
    {
        // to be overwritten
    }

    final void setError( ResourceException error )
    {
        this.error = error;

        try
        {
            onError( error );
        }
        catch ( AssertionError e )
        {
            this.assertionFailedError = e;
        }
    }

    final void setPayload( Object payload )
    {
        this.payload = payload;

        try
        {
            onPayload( payload );
        }
        catch ( AssertionError e )
        {
            this.assertionFailedError = e;
        }
    }

    final void setResult( Object result )
    {
        this.result = result;

        try
        {
            onResult( result );
        }
        catch ( AssertionError e )
        {
            this.assertionFailedError = e;
        }
    }

}
