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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.plexus.rest.resource.PlexusResource;

final class PlexusResourceInterceptor
    implements MethodInterceptor
{
    public Object invoke( final MethodInvocation mi )
        throws Throwable
    {
        final Method restletMethod = getRestletMethod( mi.getMethod().getName() );
        final PlexusResource resource = (PlexusResource) mi.getThis();
        final String uri = resource.getResourceUri();

        final Object[] args = mi.getArguments();

        final MockResponse mr = MockHelper.getMockContentFor( uri );
        if ( mr == null )
        {
            final MockListener<Object> ml = MockHelper.getListenerFor( uri );
            if ( ml == null )
            {
                return mi.proceed();
            }

            ml.executed = true;

            final MockEvent mockEvent = new MockEvent( restletMethod );
            if ( restletMethod == Method.POST || restletMethod == Method.PUT )
            {
                ml.setPayload( args[3], mockEvent );
            }

            final Object result;
            try
            {
                result = mi.proceed();
                ml.setResult( result, mockEvent );
            }
            catch ( final ResourceException e )
            {
                ml.setError( e, mockEvent );
                throw e;
            }
            return result;
        }
        else if ( mr.getMethod() != null && !mr.getMethod().equals( restletMethod ) )
        {
            return mi.proceed();
        }
        else
        {
            mr.setExecuted( true );

            if ( restletMethod == Method.POST || restletMethod == Method.PUT )
            {
                try
                {
                    mr.setPayload( args[3] );
                }
                catch ( final AssertionError e )
                {
                    mr.setAssertionFailure( e );
                }
            }

            ( (Response) args[2] ).setStatus( mr.getStatus() );
            return mr.getResponse();
        }
    }

    private static Method getRestletMethod( final String name )
    {
        if ( "get".equals( name ) )
        {
            return Method.GET;
        }
        if ( "delete".equals( name ) )
        {
            return Method.DELETE;
        }
        if ( "post".equals( name ) )
        {
            return Method.POST;
        }
        if ( "put".equals( name ) )
        {
            return Method.PUT;
        }
        if ( "upload".equals( name ) )
        {
            return new Method( "UPLOAD", "upload", "#METHOD_COPY" );
        }
        throw new IllegalArgumentException( "Unknown Restlet method" );
    }
}
