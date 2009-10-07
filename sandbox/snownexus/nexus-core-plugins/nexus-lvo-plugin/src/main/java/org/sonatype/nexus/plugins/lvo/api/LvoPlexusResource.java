package org.sonatype.nexus.plugins.lvo.api;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.plugins.lvo.DiscoveryResponse;
import org.sonatype.nexus.plugins.lvo.LvoPlugin;
import org.sonatype.nexus.plugins.lvo.NoSuchKeyException;
import org.sonatype.nexus.plugins.lvo.NoSuchStrategyException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.thoughtworks.xstream.XStream;

@Component( role = PlexusResource.class, hint = "LvoPlexusResource" )
public class LvoPlexusResource
    extends AbstractPlexusResource
{
    @Requirement
    private LvoPlugin lvoPlugin;

    @Override
    public Object getPayloadInstance()
    {
        // this happens to be RO resource
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        // unprotected resource
        return new PathProtectionDescriptor( "/lvo/*", "authcBasic,perms[nexus:status]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/lvo/{key}";
    }

    public void configureXStream( XStream x )
    {
        DiscoveryResponse.configureXStream( x );
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String key = (String) request.getAttributes().get( "key" );

        try
        {
            DiscoveryResponse dr = lvoPlugin.getLatestVersionForKey( key );

            if ( dr.isSuccessful() )
            {
                return dr;
            }
            else
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Latest version for key='" + key
                    + "' not found." );
            }
        }
        catch ( NoSuchKeyException e )
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, e.getMessage(), e );
        }
        catch ( NoSuchStrategyException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage(), e );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e.getMessage(), e );
        }
    }

}
