package org.sonatype.nexus.rest.configurations;

import java.io.IOException;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.NexusStreamResponse;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.global.GlobalConfigurationResourceHandler;
import org.sonatype.plexus.rest.representation.InputStreamRepresentation;

/**
 * A resource that is able to retrieve configurations as stream.
 * 
 * @author cstamas
 * @plexus.component role-hint="configuration"
 */
public class ConfigurationPlexusResource
    extends AbstractNexusPlexusResource
{
    /** The config key used in URI and request attributes */
    public static final String CONFIG_NAME_KEY = "configName";

    /** Name denoting current Nexus configuration */
    public static final String CURRENT_CONFIG_NAME = "current";

    /** Name denoting default Nexus configuration */
    public static final String DEFAULT_CONFIG_NAME = "default";

    @Override
    public Object getPayloadInstance()
    {
        // this is RO resource, and have no payload, it streams the file to client
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/configs/{" + CONFIG_NAME_KEY + "}";
    }

    @Override
    public List<Variant> getVariants()
    {
        List<Variant> result = super.getVariants();

        result.clear();

        result.add( new Variant( MediaType.APPLICATION_XML ) );

        return result;
    }

    @Override
    public Object get( Context context, Request request, Response response )
        throws ResourceException
    {
        String configurationName = request
            .getAttributes().get( GlobalConfigurationResourceHandler.CONFIG_NAME_KEY ).toString();

        try
        {
            NexusStreamResponse result;

            if ( DEFAULT_CONFIG_NAME.equals( configurationName ) )
            {
                result = getNexusInstance( request ).getDefaultConfigurationAsStream();
            }
            else if ( CURRENT_CONFIG_NAME.equals( configurationName ) )
            {
                result = getNexusInstance( request ).getConfigurationAsStream();
            }
            else
            {
                throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND, "No configuration named '"
                    + configurationName + "' found!" );
            }

            return new InputStreamRepresentation( MediaType.valueOf( result.getMimeType() ), result.getInputStream() );
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "IOException during configuration retrieval!", e );
        }
    }

}
