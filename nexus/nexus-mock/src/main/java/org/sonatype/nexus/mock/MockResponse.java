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
