package org.sonatype.nexus.rest.artifact;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.rest.restore.AbstractRestorePlexusResource;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "ArtifactContentPlexusResource" )
public class ArtifactContentPlexusResource
    extends AbstractArtifactPlexusResource
{
    public ArtifactContentPlexusResource()
    {
        this.setReadable( false );
        this.setModifiable( true );
    }

    @Override
    public Object getPayloadInstance()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/artifact/maven/content";
    }

    @Override
    public boolean acceptsUpload()
    {
        return true;
    }

}
