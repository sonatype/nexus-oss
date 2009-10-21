package org.sonatype.nexus.mock;

import org.restlet.resource.ResourceException;

public class MockListener<E>
{

    private AssertionError assertionFailedError;

    private ResourceException error;

    private Object payload;

    private E result;

    protected boolean executed = false;

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

    public final E getResult()
    {
        return result;
    }

    protected void onError( ResourceException e, MockEvent evt )
    {
        // to be overwritten
    }

    protected void onPayload( Object payload, MockEvent evt )
    {
        // to be overwritten
    }

    protected void onResult( E result, MockEvent evt )
    {
        // to be overwritten
    }

    final void setError( ResourceException error, MockEvent evt )
    {
        this.error = error;

        try
        {
            onError( error, evt );
        }
        catch ( AssertionError e )
        {
            this.assertionFailedError = e;
        }
    }

    final void setPayload( Object payload, MockEvent evt )
    {
        this.payload = payload;

        try
        {
            onPayload( payload, evt );
        }
        catch ( AssertionError e )
        {
            this.assertionFailedError = e;
        }
    }

    final void setResult( E result, MockEvent evt )
    {
        synchronized ( lock )
        {
            try
            {
                onResult( result, evt );
            }
            catch ( AssertionError e )
            {
                this.assertionFailedError = e;
            }
            if ( !evt.isBlocked() )
            {
                this.result = result;
            }
            lock.notifyAll();
        }
    }

    public final boolean wasExecuted()
    {
        return executed;
    }

    private static final Object lock = new Object();

    public E waitForResult( Class<E> class1 )
    {
        synchronized ( lock )
        {
            while ( result == null || !class1.isAssignableFrom( result.getClass() ) )
            {
                try
                {
                    lock.wait( 1000 );
                }
                catch ( InterruptedException e )
                {
                    // is it recovereable?
                }
            }

            return result;
        }
    }
}
