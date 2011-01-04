/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
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

    @SuppressWarnings( "unchecked" )
    public <D> D waitForPayload( Class<D> class1 )
    {
        synchronized ( lock )
        {
            while ( payload == null || !class1.isAssignableFrom( payload.getClass() ) )
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

            return (D) payload;
        }
    }
}
