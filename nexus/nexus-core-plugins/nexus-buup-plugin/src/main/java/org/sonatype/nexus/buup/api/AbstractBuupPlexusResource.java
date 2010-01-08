package org.sonatype.nexus.buup.api;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.buup.NexusBuupPlugin;
import org.sonatype.nexus.buup.api.dto.UpgradeFormDTO;
import org.sonatype.nexus.buup.api.dto.UpgradeFormRequestDTO;
import org.sonatype.nexus.buup.api.dto.UpgradeStatusDTO;
import org.sonatype.nexus.buup.api.dto.UpgradeStatusResponseDTO;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;

import com.thoughtworks.xstream.XStream;

public abstract class AbstractBuupPlexusResource
    extends AbstractNexusPlexusResource
{

    @Requirement
    protected NexusBuupPlugin nexusBuupPlugin;

    @Override
    public Object getPayloadInstance()
    {
        return null;
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
}
