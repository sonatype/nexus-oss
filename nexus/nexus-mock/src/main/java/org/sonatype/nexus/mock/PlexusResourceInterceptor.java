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
            mr.executed = true;

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
