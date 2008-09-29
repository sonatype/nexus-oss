package org.sonatype.nexus.rest.configurations;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.ConfigurationsListResource;
import org.sonatype.nexus.rest.model.ConfigurationsListResourceResponse;

/**
 * A resource that is able to retrieve list of configurations.
 * 
 * @author cstamas
 * @plexus.component role-hint="configurationList"
 */
public class ConfigurationListPlexusResource
    extends AbstractNexusPlexusResource
{
    @Override
    public Object getPayloadInstance()
    {
        // RO resource, no payload
        return null;
    }

    @Override
    public String getResourceUri()
    {
        //return "/{" + NEXUS_INSTANCE_KEY + "}/configs";
        return "/configs";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        ConfigurationsListResourceResponse result = new ConfigurationsListResourceResponse();

        ConfigurationsListResource resource = new ConfigurationsListResource();

        resource.setResourceURI( createChildReference( request, ConfigurationPlexusResource.DEFAULT_CONFIG_NAME )
            .toString() );

        resource.setName( ConfigurationPlexusResource.DEFAULT_CONFIG_NAME );

        result.addData( resource );

        resource = new ConfigurationsListResource();

        resource.setResourceURI( createChildReference( request, ConfigurationPlexusResource.CURRENT_CONFIG_NAME )
            .toString() );

        resource.setName( ConfigurationPlexusResource.CURRENT_CONFIG_NAME );

        result.addData( resource );

        return result;
    }
}
