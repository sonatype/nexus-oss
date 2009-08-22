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
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.plugins.lvo.api.dto.LvoConfigDTO;
import org.sonatype.nexus.plugins.lvo.api.dto.LvoConfigRequest;
import org.sonatype.nexus.plugins.lvo.api.dto.LvoConfigResponse;
import org.sonatype.nexus.plugins.lvo.config.LvoPluginConfiguration;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "LvoConfigPlexusResource" )
public class LvoConfigPlexusResource
    extends AbstractPlexusResource
{
    @Requirement
    LvoPluginConfiguration config;

    public LvoConfigPlexusResource()
    {
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new LvoConfigRequest();
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/lvo_config", "authcBasic,perms[nexus:settings]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/lvo_config";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        config.isEnabled();

        LvoConfigResponse resp = new LvoConfigResponse();

        LvoConfigDTO dto = new LvoConfigDTO();
        dto.setEnabled( config.isEnabled() );

        resp.setData( dto );

        return resp;
    }

    @Override
    public Object put( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        LvoConfigRequest req = (LvoConfigRequest) payload;

        try
        {
            if ( req.getData().isEnabled() )
            {
                config.enable();
            }
            else
            {
                config.disable();
            }
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to store lvo configuration", e );
        }
        catch ( ConfigurationException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "Unable to store lvo configuration", e );
        }

        return null;
    }
}
