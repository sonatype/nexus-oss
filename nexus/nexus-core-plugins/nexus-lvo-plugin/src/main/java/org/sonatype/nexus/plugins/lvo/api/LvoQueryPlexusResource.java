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

@Component( role = PlexusResource.class, hint = "LvoQueryPlexusResource" )
public class LvoQueryPlexusResource
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
        return new PathProtectionDescriptor( "/lvo/*/*", "anon" );
    }

    @Override
    public String getResourceUri()
    {
        return "/lvo/{key}/{currentVersion}";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String key = (String) request.getAttributes().get( "key" );

        String cv = (String) request.getAttributes().get( "currentVersion" );

        try
        {
            DiscoveryResponse dr = lvoPlugin.queryLatestVersionForKey( key, cv );

            if ( dr.isSuccessful() )
            {
                return dr;
            }
            else
            {
                // TODO: decide which one is appropriate

                // answer a) 404
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Newer than '" + cv + "' version for key='"
                    + key + "' not found." );

                // answer b) 304
                // response.setStatus( Status.REDIRECTION_NOT_MODIFIED, "No newer version than '" + cv + "' found." );

                // return null;
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
