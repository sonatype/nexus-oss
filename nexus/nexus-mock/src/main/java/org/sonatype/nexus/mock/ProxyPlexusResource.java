package org.sonatype.nexus.mock;

import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.thoughtworks.xstream.XStream;

public class ProxyPlexusResource
    implements ManagedPlexusResource
{
    private PlexusResource plexusResource;

    // ==
    private static final MockEvent GET = new MockEvent( Method.GET );

    private static final MockEvent POST = new MockEvent( Method.POST );

    private static final MockEvent DELETE = new MockEvent( Method.DELETE );

    private static final MockEvent PUT = new MockEvent( Method.PUT );

    private static final MockEvent UPLOAD = new MockEvent( new Method( "UPLOAD", "upload", "#METHOD_COPY" ) );

    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        MockResponse mr = MockHelper.getMockContentFor( getResourceUri() );

        if ( mr == null )
        {
            MockListener ml = MockHelper.getListenerFor( getResourceUri() );
            if ( ml == null )
            {
                return plexusResource.get( context, request, response, variant );
            }
            else
            {
                ml.executed = true;
                Object result;
                try
                {
                    result = plexusResource.get( context, request, response, variant );
                    ml.setResult( result, GET );
                }
                catch ( ResourceException e )
                {
                    ml.setError( e, GET );

                    throw e;
                }
                return result;
            }
        }
        else
        {
            if ( mr.getMethod() != null && !mr.getMethod().equals( Method.GET ) )
            {
                return plexusResource.get( context, request, response, variant );
            }
            mr.executed = true;

            response.setStatus( mr.getStatus() );

            return mr.getResponse();
        }
    }

    public void delete( Context context, Request request, Response response )
        throws ResourceException
    {
        MockResponse mr = MockHelper.getMockContentFor( getResourceUri() );

        if ( mr == null )
        {
            MockListener ml = MockHelper.getListenerFor( getResourceUri() );
            if ( ml == null )
            {
                plexusResource.delete( context, request, response );
            }
            else
            {
                ml.executed = true;
                try
                {
                    plexusResource.delete( context, request, response );
                    ml.setResult( null, DELETE );
                }
                catch ( ResourceException e )
                {
                    ml.setError( e, DELETE );

                    throw e;
                }
            }
        }
        else
        {
            if ( mr.getMethod() != null && !mr.getMethod().equals( Method.DELETE ) )
            {
                plexusResource.delete( context, request, response );
                return;
            }
            mr.executed = true;

            response.setStatus( mr.getStatus() );
        }
    }

    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        MockResponse mr = MockHelper.getMockContentFor( getResourceUri() );

        if ( mr == null )
        {
            MockListener ml = MockHelper.getListenerFor( getResourceUri() );
            if ( ml == null )
            {
                return plexusResource.post( context, request, response, payload );
            }
            else
            {
                ml.executed = true;
                ml.setPayload( payload, POST );

                Object result;
                try
                {
                    result = plexusResource.post( context, request, response, payload );
                    ml.setResult( result, POST );
                }
                catch ( ResourceException e )
                {
                    ml.setError( e, POST );

                    throw e;
                }
                return result;
            }
        }
        else
        {
            if ( mr.getMethod() != null && !mr.getMethod().equals( Method.POST ) )
            {
                return plexusResource.post( context, request, response, payload );
            }
            mr.executed = true;

            try
            {
                mr.setPayload( payload );
            }
            catch ( AssertionError assertionFailedError )
            {
                mr.setAssertionFailure( assertionFailedError );
            }
            response.setStatus( mr.getStatus() );

            return mr.getResponse();
        }
    }

    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        MockResponse mr = MockHelper.getMockContentFor( getResourceUri() );

        if ( mr == null )
        {
            MockListener ml = MockHelper.getListenerFor( getResourceUri() );
            if ( ml == null )
            {
                return plexusResource.put( context, request, response, payload );
            }
            else
            {
                ml.executed = true;
                ml.setPayload( payload, PUT );

                Object result;
                try
                {
                    result = plexusResource.put( context, request, response, payload );
                    ml.setResult( result, PUT );
                }
                catch ( ResourceException e )
                {
                    ml.setError( e, PUT );

                    throw e;
                }
                return result;
            }
        }
        else
        {
            if ( mr.getMethod() != null && !mr.getMethod().equals( Method.PUT ) )
            {
                return plexusResource.put( context, request, response, payload );
            }

            mr.executed = true;

            try
            {
                mr.setPayload( payload );
            }
            catch ( AssertionError assertionFailedError )
            {
                mr.setAssertionFailure( assertionFailedError );
            }
            response.setStatus( mr.getStatus() );

            return mr.getResponse();
        }
    }

    public Object upload( Context context, Request request, Response response, List<FileItem> files )
        throws ResourceException
    {
        MockResponse mr = MockHelper.getMockContentFor( getResourceUri() );

        if ( mr == null )
        {
            MockListener ml = MockHelper.getListenerFor( getResourceUri() );
            if ( ml == null )
            {
                return plexusResource.upload( context, request, response, files );
            }
            else
            {
                ml.executed = true;
                Object result;
                try
                {
                    result = plexusResource.upload( context, request, response, files );
                    ml.setResult( result, UPLOAD );
                }
                catch ( ResourceException e )
                {
                    ml.setError( e, UPLOAD );

                    throw e;
                }
                return result;
            }
        }
        else
        {
            mr.executed = true;

            response.setStatus( mr.getStatus() );

            return mr.getResponse();
        }
    }

    // == unodified stuff below

    public boolean acceptsUpload()
    {
        return plexusResource.acceptsUpload();
    }

    public void configureXStream( XStream xstream )
    {
        plexusResource.configureXStream( xstream );
    }

    public Object getPayloadInstance()
    {
        return plexusResource.getPayloadInstance();
    }

    public PathProtectionDescriptor getResourceProtection()
    {
        return plexusResource.getResourceProtection();
    }

    public String getResourceUri()
    {
        return plexusResource.getResourceUri();
    }

    public List<Variant> getVariants()
    {
        return plexusResource.getVariants();
    }

    public boolean isAvailable()
    {
        return plexusResource.isAvailable();
    }

    public boolean isModifiable()
    {
        return plexusResource.isModifiable();
    }

    public boolean isNegotiateContent()
    {
        return plexusResource.isNegotiateContent();
    }

    public boolean isReadable()
    {
        return plexusResource.isReadable();
    }

}
