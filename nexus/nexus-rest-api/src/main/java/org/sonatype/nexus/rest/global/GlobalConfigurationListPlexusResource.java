package org.sonatype.nexus.rest.global;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.model.GlobalConfigurationListResource;
import org.sonatype.nexus.rest.model.GlobalConfigurationListResourceResponse;

/**
 * The GlobalConfigurationList resource. This is a read only resource that simply returns a list of known
 * configuration resources.
 * 
 * @author cstamas
 * @author tstevens
 * @plexus.component role-hint="GlobalConfigurationListPlexusResource"
 */
public class GlobalConfigurationListPlexusResource
    extends AbstractGlobalConfigurationPlexusResource
{

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/global_settings";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        GlobalConfigurationListResourceResponse result = new GlobalConfigurationListResourceResponse();

        GlobalConfigurationListResource data = new GlobalConfigurationListResource();

        data.setName( GlobalConfigurationPlexusResource.DEFAULT_CONFIG_NAME );

        data.setResourceURI( createChildReference( request, data.getName() ).toString() );

        result.addData( data );

        data = new GlobalConfigurationListResource();

        data.setName( GlobalConfigurationPlexusResource.CURRENT_CONFIG_NAME );

        data.setResourceURI( createChildReference( request, data.getName() ).toString() );

        result.addData( data );

        return result;
    }

}
