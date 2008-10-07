package org.sonatype.nexus.rest;

import java.io.IOException;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.Resource;
import org.sonatype.plexus.rest.representation.InputStreamRepresentation;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;

@Component( role = ManagedPlexusResource.class, hint = "pluginResources" )
public class PluginResourcesPlexusResource
    extends AbstractPlexusResource
    implements ManagedPlexusResource
{
    private static final String BUNDLE_ID_KEY = "bundleId";

    @Requirement( role = NexusResourceBundle.class )
    private Map<String, NexusResourceBundle> bundles;

    @Override
    public Object getPayloadInstance()
    {
        // RO resource
        return null;
    }

    @Override
    public String getPermissionPrefix()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/plugin_resources/{" + BUNDLE_ID_KEY + "}";
    }

    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        String bundleId = (String) request.getAttributes().get( BUNDLE_ID_KEY );

        String resourcePath = request.getResourceRef().getRemainingPart( true );

        NexusResourceBundle bundle = null;

        if ( bundles.containsKey( bundleId ) )
        {
            bundle = bundles.get( bundleId );
        }
        else
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Nexus resource bundle with ID='" + bundleId
                + "' not found!" );
        }

        Resource resource = bundle.getResource( resourcePath );

        if ( resource != null )
        {
            try
            {
                InputStreamRepresentation result = new InputStreamRepresentation( MediaType.valueOf( resource
                    .getContentType() ), resource.getInputStream() );

                result.setSize( resource.getSize() );

                return result;
            }
            catch ( IOException e )
            {
                throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Could not fetch the resource from bundle '"
                    + bundleId + "' and path '" + resourcePath + "'!", e );
            }
        }
        else
        {
            throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "Could not find the resource from bundle '"
                + bundleId + "' on path '" + resourcePath + "'!" );
        }
    }
}
