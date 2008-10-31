package org.sonatype.nexus.rest.repositories;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.data.Request;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.rest.component.AbstractComponentListPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

@Component( role = PlexusResource.class, hint = "ShadowRepositoryTypesComponentListPlexusResource" )
public class ShadowRepositoryTypesComponentListPlexusResource
    extends AbstractComponentListPlexusResource
{

    @Override
    public String getResourceUri()
    {
        return "/components/shadow_repo_types";
    }

    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( getResourceUri(), "authcBasic,perms[nexus:componentsrepotypes]" );
    }

    @Override
    protected String getRole( Request request )
    {
        return ShadowRepository.class.getName();
    }

}
