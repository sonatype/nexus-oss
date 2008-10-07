package org.sonatype.nexus.rest.attributes;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RepositoryOrGroupAttributesPlexusResource" )
public class RepositoryOrGroupAttributesPlexusResource
    extends AbstractAttributesPlexusResource
{

    @Override
    public String getResourceUri()
    {
        return "/attributes/{" + AttributesResourceHandler.DOMAIN + "}/{" + AttributesResourceHandler.TARGET_ID + "}";
    }

}
