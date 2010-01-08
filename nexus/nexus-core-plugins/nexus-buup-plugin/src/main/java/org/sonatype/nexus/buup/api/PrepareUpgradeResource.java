package org.sonatype.nexus.buup.api;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.buup.UpgradeProcessStatus;
import org.sonatype.nexus.buup.api.dto.UpgradeFormDTO;
import org.sonatype.nexus.buup.api.dto.UpgradeFormRequestDTO;
import org.sonatype.nexus.buup.api.dto.UpgradeStatusDTO;
import org.sonatype.nexus.buup.api.dto.UpgradeStatusResponseDTO;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.thoughtworks.xstream.XStream;

/**
 * @author juven
 */
@Component( role = PlexusResource.class, hint = "PrepareUpgradeResource" )
public class PrepareUpgradeResource
    extends AbstractNexusPlexusResource
{
    public PrepareUpgradeResource()
    {
        this.setReadable( false );
        this.setModifiable( true );
    }

    @Override
    public void configureXStream( XStream xstream )
    {
        super.configureXStream( xstream );

        xstream.processAnnotations( UpgradeFormRequestDTO.class );
        xstream.processAnnotations( UpgradeFormDTO.class );
        xstream.processAnnotations( UpgradeStatusResponseDTO.class );
        xstream.processAnnotations( UpgradeStatusDTO.class );
    }

    @Override
    public Object getPayloadInstance()
    {
        return new UpgradeFormRequestDTO();
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:buup]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/buup/prepareUpgrade";
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        UpgradeFormDTO dto = ( (UpgradeFormRequestDTO) payload ).getData();

        if ( dto == null )
        {
            throw new ResourceException(
                Status.CLIENT_ERROR_PRECONDITION_FAILED,
                "You have to accept the license agreement." );
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug(
                "Preparing upgrade for user " + dto.getFirstName() + " " + dto.getLastName() + ", " + dto.getEmail()
                    + "." );
        }

        // TODO process the form

        UpgradeStatusResponseDTO result = new UpgradeStatusResponseDTO();

        UpgradeStatusDTO status = new UpgradeStatusDTO( UpgradeProcessStatus.WAIT_FOR_ACTIVATION.name() );

        result.setData( status );

        return result;
    }

}
