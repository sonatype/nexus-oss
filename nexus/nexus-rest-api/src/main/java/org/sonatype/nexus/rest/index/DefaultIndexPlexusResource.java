package org.sonatype.nexus.rest.index;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "DefaultIndexPlexusResource" )
public class DefaultIndexPlexusResource
    extends AbstractIndexPlexusResource
{
    @Override
    public String getResourceUri()
    {
        return "/data_index";
    }
}
