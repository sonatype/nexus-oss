package org.sonatype.nexus.rest.attributes;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RepositoryOrGroupAttributesPlexusResource" )
public class RepositoryOrGroupAttributesPlexusResource
    extends AbstractAttributesPlexusResource
{

    @Override
    public String getResourceUri()
    {
        return "/attributes/{" + AbstractAttributesPlexusResource.DOMAIN + "}/{"
            + AbstractAttributesPlexusResource.TARGET_ID + "}/content";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/attributes/*/**", "authcBasic,perms[nexus:cache]" );
    }

}
