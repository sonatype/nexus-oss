package org.sonatype.nexus.mock;

import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.restlet.Context;
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
                Object result;
                try
                {
                    result = plexusResource.get( context, request, response, variant );
                    ml.setResult( result );
                }
                catch ( ResourceException e )
                {
                    ml.setError( e );

                    throw e;
                }
                return result;
            }
        }
        else
        {
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
                try
                {
                    plexusResource.delete( context, request, response );
                    ml.setResult( null );
                }
                catch ( ResourceException e )
                {
                    ml.setError( e );

                    throw e;
                }
            }
        }
        else
        {
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
                ml.setPayload( payload );

                Object result;
                try
                {
                    result = plexusResource.post( context, request, response, payload );
                    ml.setResult( result );
                }
                catch ( ResourceException e )
                {
                    ml.setError( e );

                    throw e;
                }
                return result;
            }
        }
        else
        {
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
                ml.setPayload( payload );

                Object result;
                try
                {
                    result = plexusResource.put( context, request, response, payload );
                    ml.setResult( result );
                }
                catch ( ResourceException e )
                {
                    ml.setError( e );

                    throw e;
                }
                return result;
            }
        }
        else
        {
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
                Object result;
                try
                {
                    result = plexusResource.upload( context, request, response, files );
                    ml.setResult( result );
                }
                catch ( ResourceException e )
                {
                    ml.setError( e );

                    throw e;
                }
                return result;
            }
        }
        else
        {
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
