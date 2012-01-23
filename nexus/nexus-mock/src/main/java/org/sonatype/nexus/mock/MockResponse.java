/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.mock;

import org.restlet.data.Method;
import org.restlet.data.Status;

public class MockResponse
{
    protected Status status;

    protected Object response;

    protected Object payload;

    private AssertionError assertionFailedError;

    private boolean executed = false;

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

    private static final Object lock = new Object();

    public void waitForExecution()
    {
        synchronized ( lock )
        {
            int i = 0;
            while ( !executed && i++ < 50 )
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

            if ( executed )
            {
                return;
            }

            throw new RuntimeException( "Not executed!" );
        }
    }

    protected void setExecuted( boolean exec )
    {
        this.executed = exec;
        synchronized ( lock )
        {
            lock.notifyAll();
        }
    }

}
