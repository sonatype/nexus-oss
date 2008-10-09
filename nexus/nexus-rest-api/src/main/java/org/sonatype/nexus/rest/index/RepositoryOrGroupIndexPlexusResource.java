package org.sonatype.nexus.rest.index;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "RepositoryOrGroupIndexPlexusResource" )
public class RepositoryOrGroupIndexPlexusResource
    extends AbstractIndexPlexusResource
{

    @Override
    public String getResourceUri()
    {
        return "/data_index/{" + AbstractIndexPlexusResource.DOMAIN + "}/{" + AbstractIndexPlexusResource.TARGET_ID
            + "}";
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/data_index/*/**", "authcBasic,perms[nexus:index]" );
    }

}
