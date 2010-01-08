package org.sonatype.nexus.buup.api;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.buup.api.dto.UpgradeStatusDTO;
import org.sonatype.nexus.buup.api.dto.UpgradeStatusResponseDTO;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

/**
 * @author juven
 */
@Component( role = PlexusResource.class, hint = "UpgradeStatusResource" )
public class UpgradeStatusResource
    extends AbstractBuupPlexusResource
{
    public UpgradeStatusResource()
    {
        this.setReadable( true );

        this.setModifiable( false );
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:buup]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/buup/upgradeStatus";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        UpgradeStatusResponseDTO result = new UpgradeStatusResponseDTO();

        UpgradeStatusDTO data = new UpgradeStatusDTO();

        data.setUpgradeStatus( nexusBuupPlugin.getUpgradeProcessStatus().name() );

        result.setData( data );

        return result;
    }

}
